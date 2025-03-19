package github.keshaparrot.floworderonlineshop.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Set;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/products/get/all",
            "/api/v1/orders/ready-to-ship",
            "/api/v1/products/get/.*"
    );

    private static final Set<String> ADMIN_ONLY_ENDPOINTS = Set.of(
            "/api/v1/orders/{orderId}/set-status",
            "/api/v1/products/create",
            "/api/v1/products/update/{id}",
            "/api/v1/products/update/{id}/photo",
            "/api/v1/products/delete/{id}",
            "/api/v1/refunds/{orderId}/approve",
            "/api/v1/refunds/{orderId}/decline"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String role = (String) request.getAttribute("role");

        if (PUBLIC_ENDPOINTS.stream().anyMatch(path::matches)) {
            return true;
        }

        for (String endpoint : ADMIN_ONLY_ENDPOINTS) {
            if (path.matches(endpoint.replace("{orderId}", "\\d+").replace("{id}", "\\d+"))) {
                if (role == null || !role.equals("ADMIN")) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Admins only");
                    return false;
                }
                return true;
            }
        }

        return true;
    }
}
