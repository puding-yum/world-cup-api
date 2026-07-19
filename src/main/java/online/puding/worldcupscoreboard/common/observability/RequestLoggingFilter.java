package online.puding.worldcupscoreboard.common.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Mencatat tiap request yang masuk (method + path) sedini mungkin di rantai
 * filter. Berguna untuk observability saat load testing: dari jumlah baris
 * {@code REQ} bisa dibandingkan dengan jumlah baris {@code DB} (query nyata) dan
 * {@code REDIS} untuk memvalidasi cache & request coalescing. Logger dipisah agar
 * gampang dimatikan lewat konfigurasi level log.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String query = request.getQueryString();
        log.info("REQ {} {}{}", request.getMethod(), request.getRequestURI(),
                query == null ? "" : "?" + query);
        filterChain.doFilter(request, response);
    }
}
