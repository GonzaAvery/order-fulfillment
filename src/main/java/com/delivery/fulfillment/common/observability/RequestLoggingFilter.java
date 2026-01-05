package com.delivery.fulfillment.common.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro para logging estructurado de requests.
 * Agrega correlationId a los logs para trazabilidad.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, 
	                                HttpServletResponse response, 
	                                FilterChain filterChain) throws ServletException, IOException {
		
		String correlationId = request.getHeader("X-Correlation-Id");
		if (correlationId == null || correlationId.isEmpty()) {
			correlationId = UUID.randomUUID().toString();
		}
		
		MDC.put("correlationId", correlationId);
		MDC.put("requestPath", request.getRequestURI());
		MDC.put("requestMethod", request.getMethod());
		
		try {
			long startTime = System.currentTimeMillis();
			filterChain.doFilter(request, response);
			long duration = System.currentTimeMillis() - startTime;
			
			logger.info("Request completed: method={}, path={}, status={}, duration={}ms", 
				request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
		} finally {
			MDC.clear();
		}
	}
}

