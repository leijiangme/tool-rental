package team28.handyman.controllers;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

@Component
@WebFilter(urlPatterns="*")
public class MissingUserFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}
	
	private boolean allow(HttpServletRequest req) {
		String servletPath = req.getServletPath();
		return 	"/".equals(servletPath) || 
				"/customers/new".equals(servletPath) ||
				"/site.css".equals(servletPath) ||
				req.getSession(true).getAttribute("user") != null ;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		if(allow(req)) {
			chain.doFilter(request, response);
		} else {
			((HttpServletResponse)response).sendRedirect("/");
		}
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
