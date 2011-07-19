/**
 * Copyright (c) 2011, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.force.sdk.qa.util;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.installer.Installer;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.container.internal.util.HttpUtils;
import org.codehaus.cargo.container.internal.util.HttpUtils.HttpResult;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.util.log.SimpleLogger;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * Use this class if you need to deploy an app to a container for an integration test.
 *
 * @author Jeff Lai
 */
public abstract class BaseContainerTest {
    
    private InstalledLocalContainer container;
    
    @BeforeSuite
    public void suiteSetup() throws Exception {
        setupContainer();
    }
    
    @AfterSuite(alwaysRun = true)
    public void suiteTeardown() {
        System.out.println("stopping container " + getContainerId() + " please wait...");
        container.stop();
    }
    
    /**
     * Downloads and installs container into target directory, and then starts up container with specified configuration.
     * @throws IOException 
     */
    public void setupContainer() throws IOException {
        Installer installer = new ZipURLInstaller(new URL(getZipInstallerUrl()), "target/install/" + getContainerId());
        installer.install();
        ContainerFactory containerFac = new DefaultContainerFactory();
        ConfigurationFactory configFac = new DefaultConfigurationFactory();
        Configuration configuration =
            configFac.createConfiguration(getContainerId(), ContainerType.INSTALLED, ConfigurationType.STANDALONE);
        setConfigProps(configuration, getConfigProps());
        container =
            (InstalledLocalContainer) containerFac.createContainer(getContainerId(), ContainerType.INSTALLED, configuration);
        container.setLogger(new SimpleLogger());
        container.setHome(installer.getHome());
        Map<String, String> props = getContainerProps();
        if (props != null) {
            container.setSystemProperties(props);
        }

        System.out.println("starting container " + getContainerId() + " please wait...");
        container.start();
    }
    
    private void setConfigProps(Configuration configuration, Map<String, String> props) {
        if (props != null) {
            for (Map.Entry<String, String> entry : props.entrySet()) {
                configuration.setProperty(entry.getKey(), entry.getValue());
            }
        }
    }
    
    public InstalledLocalContainer getContainer() {
        return container;
    }
    
    /**
     * Hot deploys a war to the running container.
     * 
     * @param warPath path to the war file you want to deploy
     * @throws InterruptedException 
     * @throws MalformedURLException 
     */
    public void deployWar(String warPath) throws InterruptedException, MalformedURLException {
        System.out.println("deploying war from " + warPath + " please wait...");
        DeployableFactory deployableFac = new DefaultDeployableFactory();
        WAR war = (WAR) deployableFac.createDeployable(container.getId(), warPath, DeployableType.WAR);
        DeployerFactory deployerFac = new DefaultDeployerFactory();
        Deployer deployer = deployerFac.createDeployer(getContainer(), DeployerType.INSTALLED);
        deployer.deploy(war);
        // verify that application has started before exiting method
        int cycles = 0;
        int increment = 5;
        while (true) {
            if (isDeployedWarRunning(warPath)) {
                break;
            } else if (cycles >= 6) {
                Assert.fail("we waited " + cycles * increment + " seconds, but application has not started successfully");
            }
            cycles++;
            Thread.sleep(increment * 1000);
        }
    }
    
    private boolean isDeployedWarRunning(String warPath) throws MalformedURLException {
        Pattern pat = Pattern.compile("^.+/(.+)\\.war$");
        Matcher mat = pat.matcher(warPath);
        Assert.assertTrue(mat.matches(), "could not grab file name from war path");
        String appName = mat.group(1);
        String port = container.getConfiguration().getPropertyValue(ServletPropertySet.PORT);
        HttpResult result = new HttpResult();
        return new HttpUtils().ping(new URL("http://localhost:" + port + "/" + appName), result);
    }
    
    /**
     * Override this method to return the url of where to download the container zip installer.
     */
    public abstract String getZipInstallerUrl();
    
    /**
     * Override this method with the id of the container you are installed.  For example, tomcat6x or jetty7x
     */
    public abstract String getContainerId();
    
    /**
     * Override this method to set the container configuration properties in a map.
     * <p>
     * If you don't want to set any properties, just override this method to return null
     */
    public abstract Map<String, String> getConfigProps();

    /**
     * Override this method to set container system properties in a map.
     * <p>
     * If you don't want to set any properties, just override this method to return null
     * @throws IOException 
     */
    public abstract Map<String, String> getContainerProps() throws IOException;
    
}
