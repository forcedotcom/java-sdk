package com.force.sdk.oauth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.connector.ForceServiceConnector;
import com.force.sdk.oauth.context.ForceSecurityContext;
import com.force.sdk.oauth.context.ForceSecurityContextHolder;
import com.force.sdk.oauth.context.SecurityContext;
import com.force.sdk.oauth.context.SecurityContextUtil;
import com.force.sdk.oauth.context.store.SecurityContextCookieStore;
import com.force.sdk.oauth.context.store.SecurityContextSessionStore;
import com.sforce.ws.ConnectionException;

public class BasicLogoutFilterTest extends BaseOAuthTest {
	
    @DataProvider
    protected Object[][] logoutFilterDataProvider() {
        return new Object[][] {
                {true, false, null},
                {true, true, null},
                {true, false, "/test.jsp"},
                {true, true, "/test.jsp"},
                {false, false, null},
                {false, true, null},
                {false, false, "/test.jsp"},
                {false, true, "/test.jsp"},
        };
    }

    @Test(dataProvider = "logoutFilterDataProvider")
	public void testLogoutFilter(boolean isSessionStorage, boolean logoutFromSFDC, String logoutUrl) throws ConnectionException, ServletException, IOException {    	
		// Set up logged in state
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		ForceServiceConnector connector = new ForceServiceConnector("userInfo");
        // Add session id and endpoint cookies to the request
        Cookie sidCookie =
            new Cookie(SecurityContextUtil.FORCE_FORCE_SESSION, connector.getConnection().getSessionHeader().getSessionId());
        Cookie endpointCookie =
            new Cookie(SecurityContextUtil.FORCE_FORCE_ENDPOINT, connector.getConnection().getConfig().getServiceEndpoint());
        request.setCookies(sidCookie, endpointCookie);
		
        // Store the security context in the appropriate location
        SecurityContext sc = new ForceSecurityContext();
        sc.setEndPoint(connector.getConnection().getConfig().getServiceEndpoint());
        sc.setSessionId(connector.getConnection().getSessionHeader().getSessionId());
        if(isSessionStorage) {
        	request.getSession().setAttribute(SecurityContextSessionStore.SECURITY_CONTEXT_SESSION_KEY, sc);
        } else {
        	Cookie scCookie = new Cookie(SecurityContextCookieStore.SECURITY_CONTEXT_COOKIE_NAME, sc.toString());
        	request.setCookies(scCookie);
        }
        ForceSecurityContextHolder.set(sc);
        
        // Create the LogoutFilter
        LogoutFilter filter = new LogoutFilter();
        MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter("securityContextStorageMethod", isSessionStorage ? "session" : "cookie");
        filterConfig.addInitParameter("logoutFromForceDotCom", String.valueOf(logoutFromSFDC));
        filterConfig.addInitParameter("logoutSuccessUrl", logoutUrl);
        filter.init(filterConfig);
        
        filter.doFilter(request, response, new VerifyLogoutFilterChain(isSessionStorage, logoutFromSFDC, logoutUrl));
	}
	
    /**
     * Mock filter chain for verifying logout results.
     * 
     * @author John Simone
     */
    private class VerifyLogoutFilterChain implements FilterChain {

    	private boolean isSessionStorage;
    	private boolean logoutFromSFDC;
    	private String logoutUrl = null;
    	
    	public VerifyLogoutFilterChain(boolean isSessionStorage, boolean logoutFromSFDC, String logoutUrl) {
    		this.isSessionStorage = isSessionStorage;
    		this.logoutFromSFDC = logoutFromSFDC;
    		this.logoutUrl = logoutUrl;
    	}
    	
		@Override
		public void doFilter(ServletRequest request, ServletResponse response)
				throws IOException, ServletException {

			HttpServletRequest req = (HttpServletRequest) request;
			MockHttpServletResponse res = (MockHttpServletResponse) response;
			
			//verify that the security context holder no longer holds a security context
			Assert.assertNull(ForceSecurityContextHolder.get(false), "Security context should be cleared after logout");
			
			//verify that security context cookie or session value is cleared depending on storage method
			if(isSessionStorage) {
				Assert.assertNull(req.getSession(false), "Session should be null");
			} else {
				Cookie scCookie = res.getCookie(SecurityContextCookieStore.SECURITY_CONTEXT_COOKIE_NAME);
				Assert.assertNotNull(scCookie, "Security context cookie should not be null");
				Assert.assertEquals(scCookie.getMaxAge(), 0, "Max age of security context cookie should be 0");
			}
			
			//verify that endpoint and session id cookies are cleared
			Cookie endpointCookie = res.getCookie(SecurityContextUtil.FORCE_FORCE_ENDPOINT);
			Assert.assertNotNull(endpointCookie, "Endpoint cookie should not be null");
			Assert.assertEquals(endpointCookie.getMaxAge(), 0, "Max age of endpoint cookie should be 0");
			Cookie sessionIdCookie = res.getCookie(SecurityContextUtil.FORCE_FORCE_SESSION);
			Assert.assertNotNull(sessionIdCookie, "Session Id cookie should not be null");
			Assert.assertEquals(sessionIdCookie.getMaxAge(), 0, "Max age of session id cookie should be 0");
			
			//verify that the correct redirect has taken place
			if(logoutFromSFDC) {
				Assert.assertTrue(res.getRedirectedUrl().contains("secur/logout.jsp"), 
                    "When logging out from SFDC the user must be redirected to the logout page");
			} else {
				if(logoutUrl == null) {
					logoutUrl = "/";
				}
				Assert.assertTrue(res.getRedirectedUrl().contains(logoutUrl), 
                    "After logging out the user should be sent to the specified logout url");
			}
			
		}
    	
    }


}
