package com.force.sample.springsecurity;

import com.force.sdk.oauth.context.SecurityContext;
import com.force.sdk.oauth.context.SecurityContextUtil;
import com.sforce.ws.ConnectionException;
import mockit.Instantiation;
import mockit.Mock;
import mockit.MockClass;

/**
 * This class mocks the {@code SecurityContextUtil}::initializeSecurityContextFromApi, when a mock oauth server is
 * used to replace actual SFDC url.
 *
 * @author Nawab Iqbal
 */

@MockClass(realClass = SecurityContextUtil.class, instantiation = Instantiation.PerMockSetup)
public final class MockSecurityContextUtil {

    private MockSecurityContextUtil() { }

    /**
     * Fills mocked values into security context object; so that we don't need to do an api call.
     * @param securityContext  The mocked values are added to this object.
     * @throws ConnectionException Not being used. It is only to keep the signature consistent with actual method.
     */
    @Mock
    public static void initializeSecurityContextFromApi(SecurityContext securityContext) throws ConnectionException  {
        System.out.println("Getting mocked security context.");
        securityContext.setOrgId("dummy Org");
        securityContext.setUserId("mock userId");
        securityContext.setUserName("mock un");
        securityContext.setLanguage("mock l");
        securityContext.setLocale("mock L");
        securityContext.setTimeZone("mock tz");
        securityContext.setRole(SecurityContextUtil.DEFAULT_ROLE);
    }
}
