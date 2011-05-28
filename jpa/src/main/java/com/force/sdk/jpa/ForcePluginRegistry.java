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

package com.force.sdk.jpa;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.datanucleus.plugin.*;

/**
 * 
 * This is a bit of a hack just to give us the ability to swap out the MetaDataManager,
 * since DataNucleus doesn't provide us with an easy way of specifying our own.
 *
 * @author Jill Wetzler
 */
public class ForcePluginRegistry implements PluginRegistry {

    private final PluginRegistry delegate;
    
    /**
     * Create a ForcePluginRegistry that can delegate to the existing DataNucleus
     * plugin registry.
     * 
     * @param pluginRegistry the existing plugin registry
     */
    public ForcePluginRegistry(PluginRegistry pluginRegistry) {
        delegate = pluginRegistry;
    }

    /**
     * @see PluginRegistry#getExtensionPoints()
     * {@inheritDoc}
     */
    @Override
    public ExtensionPoint[] getExtensionPoints() {
      return delegate.getExtensionPoints();
    }

    /**
     * @see PluginRegistry#registerExtensionPoints()
     * {@inheritDoc}
     */
    @Override
    public void registerExtensionPoints() {
      delegate.registerExtensionPoints();
    }

    /**
     * @see PluginRegistry#registerExtensions()
     * {@inheritDoc}
     */
    @Override
    public void registerExtensions() {
      delegate.registerExtensions();
    }

    /**
     * @see PluginRegistry#createExecutableExtension(ConfigurationElement, String, Class[], Object[])
     * {@inheritDoc}
     */
    @Override
    public Object createExecutableExtension(ConfigurationElement confElm, String name,
                                            Class[] argsClass, Object[] args)
        throws ClassNotFoundException, SecurityException, NoSuchMethodException,
               IllegalArgumentException, InstantiationException, IllegalAccessException,
               InvocationTargetException {
      return delegate.createExecutableExtension(confElm, name, argsClass, args);
    }

    /**
     * @see PluginRegistry#loadClass(String, String)
     * {@inheritDoc}
     */
    @Override
    public Class loadClass(String pluginId, String className) throws ClassNotFoundException {
      return delegate.loadClass(pluginId, className);
    }

    /**
     * @see PluginRegistry#resolveURLAsFileURL(URL)
     * {@inheritDoc}
     */
    @Override
    public URL resolveURLAsFileURL(URL url) throws IOException {
      return delegate.resolveURLAsFileURL(url);
    }

    /**
     * @see PluginRegistry#resolveConstraints()
     * {@inheritDoc}
     */
    @Override
    public void resolveConstraints() {
      delegate.resolveConstraints();
    }

    /**
     * @see PluginRegistry#getBundles()
     * {@inheritDoc}
     */
    @Override
    public Bundle[] getBundles() {
      return delegate.getBundles();
    }
    
    /**
     * @see PluginRegistry#getExtensionPoint(String)
     * {@inheritDoc}
     */
    @Override
    public ExtensionPoint getExtensionPoint(String id) {
        ExtensionPoint ep = delegate.getExtensionPoint(id);

        if (id.equals("org.datanucleus.metadata_manager")) {
            for (Extension ext : ep.getExtensions()) {
                for (ConfigurationElement cfg : ext.getConfigurationElements()) {
                    if (cfg.getAttribute("name").equals("JPA")) {
                        // override with our own metadata manager
                        threadsafePutAttribute(cfg, "class", ForceMetaDataManager.class.getName());
                    }
                }
            }
        }
        return ep;
    }

    private void threadsafePutAttribute(ConfigurationElement cfg, String attrName, String val) {
        if (!val.equals(cfg.getAttribute(attrName))) {
            synchronized (cfg) {
                cfg.putAttribute(attrName, val);
            }
        }
    }
    
}
