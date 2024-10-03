package com.rays.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.rays.config.JwtTokenUtil;
import com.rays.service.JwtUserDetailsService;

import io.jsonwebtoken.ExpiredJwtException;

/**
 * Front controller verifies if user id logged in
 * 
 * @author Ashish Kushwah
 * 
 */
@Component
public class FrontCtl extends HandlerInterceptorAdapter {
	@Autowired
	private JwtUserDetailsService jwtUserDetailsService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		/* HttpSession session = request.getSession(); */
		String path = request.getServletPath();

		System.out.println(" Front Ctl Called " + path);
			boolean pass = false;
		if (!path.startsWith("/Auth/")) {
			// System.out.println("inside if condition");

			System.out.println("Inside Forntctl JWTRequestFilter run success");
			final String requestTokenHeader = request.getHeader("Authorization");
			System.out.println(requestTokenHeader + "Inside Forntctl.......");
			String username = null;
			String jwtToken = null;
			// JWT Token is in the form "Bearer token". Remove Bearer word and get only the
			// Token
			if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
				System.out.println("Inside Forntctltoken != null");
				jwtToken = requestTokenHeader.substring(7);
				try {
					username = jwtTokenUtil.getUsernameFromToken(jwtToken);
					System.out.println(username + "Inside Forntctl user.......");
				} catch (IllegalArgumentException e) {
					System.out.println("Inside Forntctl Unable to get JWT Token........");
				} catch (ExpiredJwtException e) {
					System.out.println("Inside Forntctl JWT Token has expired........");
				}
			} else {
				System.out.println("Inside Forntctl JWT Token does not begin with Bearer String");

			}

			// Once we get the token validate it.
			if (username != null) {
				System.out.println("inside user != null");
				UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);

				// if token is valid configure Spring Security to manually set authentication
				if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {

					UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					usernamePasswordAuthenticationToken
							.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					// After setting the Authentication in the context, we specify
					// that the current user is authenticated. So it passes the Spring Security
					// Configurations successfully.
					SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
				}
				pass = true;
			}
		}
		return pass;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		System.out.println("inside post handler");
		response.setHeader("Access-Control-Allow-Origin", "");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT");
	}
}
