package com.bytelightning.oss.lib.shiro;

import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.web.filter.authc.PassThruAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * A <code>PassThruAuthenticationFilter</code> that allows the login url to be configured (different login url's within the same app).
 * Something like this:
		new ShiroWebModule(servletCtx) {
			protected void configureShiroWeb() {
				Key<BetterPassThruAuthFilter> key = Key.get(BetterPassThruAuthFilter.class);
				addFilterChain("/vip/foo**", key, config(key, "/vip/login-foo.jsp"));
			}
		}
 */
public class BetterPassThruAuthFilter extends PassThruAuthenticationFilter {
	@Override
	protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
		// Get the default login url.
		String loginUrl = getLoginUrl();
		// Check to see if an alternate login url has been configured for this specific path.
        for (String path : appliedPaths.keySet())
            if (pathsMatch(path, request)) {
                Object config = this.appliedPaths.get(path);
                if (config instanceof String[]) {
                	String[] strings = (String[])config;
                	if ((strings.length == 1) && (strings[0].trim().length() > 0))
                		loginUrl = strings[0].trim();
                }
            }
        // Append any query that was associated with the request to our target login url.
		String q = ((HttpServletRequest) request).getQueryString();
		if ((q != null) && (q.trim().length() > 0)) {
			if (! q.startsWith("?"))
				q = "?" + q;
			loginUrl = loginUrl + q;
		}
		// Route them to the target login url.
		WebUtils.issueRedirect(request, response, loginUrl);
	}
}
