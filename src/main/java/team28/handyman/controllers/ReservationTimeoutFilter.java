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

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;


/**
 * Redirects the user
 * @author jdavenpo
 *
 */
@Component
@WebFilter(urlPatterns="/reservations/*")
public class ReservationTimeoutFilter implements Filter{

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		System.out.println("Started filter");

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			chain.doFilter(request, response);
		} catch (Throwable ex) {
			if(ex.getCause() instanceof DataIntegrityViolationException) {
				DataIntegrityViolationException dive = (DataIntegrityViolationException) ex.getCause();
				String message = dive.getMessage();
				if(message.contains(" is not present in table \"reservations\"")) {
					HttpServletResponse httpResponse = (HttpServletResponse) response;
					httpResponse.sendRedirect("/customers?system_error=101");
					((HttpServletRequest) request).getSession(true).setAttribute("reservationForm", null);
					return;
				}
			}
			throw ex;
		}

	}

	@Override
	public void destroy() {

	}

}
