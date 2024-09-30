package jp.onehr.securitystarter.uaa.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class DemoFilter extends OncePerRequestFilter {

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        String tenantId = request.getHeader("X-Tenant-Id");
//        boolean hasAccess = isUserAllowed(tenantId);
//        if (hasAccess) {
            filterChain.doFilter(request, response);
//            return;
//        }
//        throw new AccessDeniedException("Access denied");
    }

}
