package github.keshaparrot.floworderonlineshop.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import github.keshaparrot.floworderonlineshop.config.JwtUtil;
import github.keshaparrot.floworderonlineshop.model.dto.OrderDTO;
import github.keshaparrot.floworderonlineshop.services.interfaces.IRefundService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RefundController.class)
public class RefundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IRefundService refundService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public IRefundService refundService() {
            return Mockito.mock(IRefundService.class);
        }
        @Bean
        public JwtUtil jwtUtil() {
            JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);

            Mockito.when(jwtUtil.generateToken(Mockito.anyString(), Mockito.anyMap()))
                    .thenReturn("mocked-jwt-token");

            Mockito.when(jwtUtil.validateToken(Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(true);

            Claims dummyClaims = Jwts.claims();
            dummyClaims.setSubject("test@example.com");
            dummyClaims.put("role", "ADMIN");
            //dummyClaims.put("role", "USER");

            Mockito.when(jwtUtil.extractClaims(Mockito.eq("mocked-jwt-token")))
                    .thenReturn(dummyClaims);

            return jwtUtil;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @Test
    public void testRefundOrderSuccess() throws Exception {
        Mockito.when(refundService.refundOrder(eq(1L), any(String.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/refunds/1/refund")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .param("reason", "Defective product"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("refund request accepted")));
    }

    @Test
    public void testRefundOrderFailure() throws Exception {
        Mockito.when(refundService.refundOrder(eq(1L), any(String.class))).thenReturn(false);

        mockMvc.perform(post("/api/v1/refunds/1/refund")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .param("reason", "Defective product"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("error attempting while creating refund request")));
    }

    @Test
    public void testGetAllRefundedOrders() throws Exception {
        OrderDTO orderDTO = new OrderDTO();
        Page<OrderDTO> page = new PageImpl<>(Collections.singletonList(orderDTO));
        Mockito.when(refundService.getAllRefundedOrders(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/refunds")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testApproveRefundSuccess() throws Exception {
        Mockito.when(refundService.approveOrderRefund(eq(1L), anyBoolean())).thenReturn(true);

        mockMvc.perform(put("/api/v1/refunds/1/approve")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .param("returnItemToShop", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("refund request accepted")));
    }

    @Test
    public void testApproveRefundFailure() throws Exception {
        Mockito.when(refundService.approveOrderRefund(eq(1L), anyBoolean())).thenReturn(false);

        mockMvc.perform(put("/api/v1/refunds/1/approve")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .param("returnItemToShop", "false"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeclineRefundSuccess() throws Exception {
        Mockito.when(refundService.declineOrderRefund(eq(1L))).thenReturn(true);

        mockMvc.perform(put("/api/v1/refunds/1/decline")
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("refund request rejected")));
    }

    @Test
    public void testDeclineRefundFailure() throws Exception {
        Mockito.when(refundService.declineOrderRefund(eq(1L))).thenReturn(false);

        mockMvc.perform(put("/api/v1/refunds/1/decline")
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isBadRequest());
    }
}
