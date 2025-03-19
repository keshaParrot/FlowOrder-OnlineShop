package github.keshaparrot.floworderonlineshop.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import github.keshaparrot.floworderonlineshop.config.JwtUtil;
import github.keshaparrot.floworderonlineshop.model.dto.BillDTO;
import github.keshaparrot.floworderonlineshop.services.interfaces.IPaymentService;
import github.keshaparrot.floworderonlineshop.services.interfaces.IProductService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IPaymentService paymentService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public IPaymentService productService() {
            return Mockito.mock(IPaymentService.class);
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
    public void testGetTransactionSuccess() throws Exception {
        BillDTO billDTO = new BillDTO();
        Mockito.when(paymentService.getTransaction(1L)).thenReturn(billDTO);

        mockMvc.perform(get("/api/v1/payments/1")
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetTransactionNotFound() throws Exception {
        Mockito.when(paymentService.getTransaction(1L))
                .thenThrow(ChangeSetPersister.NotFoundException.class);

        mockMvc.perform(get("/api/v1/payments/1")
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllTransactionsSuccess() throws Exception {
        BillDTO billDTO = new BillDTO();
        Page<BillDTO> page = new PageImpl<>(Collections.singletonList(billDTO));

        Mockito.when(paymentService.getAllTransactions(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/payments")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .param("userId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
