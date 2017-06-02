package com.kuryshee.safehome.rpiserver;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

/**
 * Servlet Filter implementation class AuthFilter
 * Filters users upon their authenticity.
 * @author Ekaterina Kurysheva
 */
@WebFilter("/AuthFilter")
public class AuthFilter implements Filter {
	
	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * This method checks whether the session has the specified attribute for user authentication. 
	 * In case the attribute is missing, the filter redirects request to the index page.
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (((HttpServletRequest) request).getSession()
				.getAttribute(IndexPage.AUTH_KEY) == null) {
			
		    ((HttpServletResponse) response).sendRedirect("../index.xhtml");
		} 
		else {
		      chain.doFilter(request, response);
		}
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

}
