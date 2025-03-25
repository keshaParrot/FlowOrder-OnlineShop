package github.keshaparrot.floworderonlineshop.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import github.keshaparrot.floworderonlineshop.security.JwtUtil;
import github.keshaparrot.floworderonlineshop.model.dto.OrderDTO;
import github.keshaparrot.floworderonlineshop.model.dto.OrderRequestDTO;
import github.keshaparrot.floworderonlineshop.model.enums.OrderStatus;
import github.keshaparrot.floworderonlineshop.services.interfaces.IOrderService;
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
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IOrderService orderService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public IOrderService orderService() {
            return Mockito.mock(IOrderService.class);
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
    public void testMakeOrderSuccess() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(1L);
        Map<Long, Integer> productQuantities = new HashMap<>();
        productQuantities.put(101L, 2);
        productQuantities.put(102L, 1);
        request.setProductQuantities(productQuantities);

        Mockito.when(orderService.makeOrder(anyLong(), anyMap())).thenReturn(true);

        mockMvc.perform(post("/api/v1/orders/create")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("the order was placed successfully")));
    }

    @Test
    public void testMakeOrderFailure() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(1L);
        Map<Long, Integer> productQuantities = new HashMap<>();
        productQuantities.put(101L, 2);
        request.setProductQuantities(productQuantities);

        Mockito.when(orderService.makeOrder(anyLong(), anyMap())).thenReturn(false);

        mockMvc.perform(post("/api/v1/orders/create")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("error attempting while creating the order")));
    }

    @Test
    public void testAddItemToOrderSuccess() throws Exception {
        Map<Long, Integer> productQuantities = new HashMap<>();
        productQuantities.put(101L, 1);

        Mockito.when(orderService.addItemToOrder(eq(1L), anyMap())).thenReturn(true);

        mockMvc.perform(post("/api/v1/orders/1/add-items")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productQuantities)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("the product was successfully added to the order")));
    }

    @Test
    public void testAddItemToOrderFailure() throws Exception {
        Map<Long, Integer> productQuantities = new HashMap<>();
        productQuantities.put(101L, 1);

        Mockito.when(orderService.addItemToOrder(eq(1L), anyMap())).thenReturn(false);

        mockMvc.perform(post("/api/v1/orders/1/add-items")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productQuantities)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("error attempting while adding the product to the order")));
    }

    @Test
    public void testChangeItemQuantitiesSuccess() throws Exception {
        Map<Long, Integer> productQuantities = new HashMap<>();
        productQuantities.put(101L, 3);

        Mockito.when(orderService.changeItemQuantities(eq(1L), anyMap())).thenReturn(true);

        mockMvc.perform(put("/api/v1/orders/1/change-quantities")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productQuantities)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("the product was successfully changed to the order")));
    }

    @Test
    public void testChangeItemQuantitiesFailure() throws Exception {
        Map<Long, Integer> productQuantities = new HashMap<>();
        productQuantities.put(101L, 3);

        Mockito.when(orderService.changeItemQuantities(eq(1L), anyMap())).thenReturn(false);

        mockMvc.perform(put("/api/v1/orders/1/change-quantities")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productQuantities)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("error attempting while changing the product to the order")));
    }

    @Test
    public void testRemoveItemsFromOrderSuccess() throws Exception {
        Long[] productIds = {101L, 102L};

        Mockito.when(orderService.removeItemFromOrder(eq(1L), any(Long[].class))).thenReturn(true);

        mockMvc.perform(delete("/api/v1/orders/1/remove-items")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productIds)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(containsString("the product was successfully removed from the order")));
    }

    @Test
    public void testRemoveItemsFromOrderFailure() throws Exception {
        Long[] productIds = {101L};

        Mockito.when(orderService.removeItemFromOrder(eq(1L), any(Long[].class))).thenReturn(false);

        mockMvc.perform(delete("/api/v1/orders/1/remove-items")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productIds)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("error attempting while removing the product from the order")));
    }

    @Test
    public void testCancelOrderSuccess() throws Exception {
        Mockito.when(orderService.cancelOrder(eq(1L), eq(1L))).thenReturn(true);

        mockMvc.perform(delete("/api/v1/orders/1/cancel")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .param("userId", "1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(containsString("the order was successfully cancelled")));
    }

    @Test
    public void testCancelOrderFailure() throws Exception {
        Mockito.when(orderService.cancelOrder(eq(1L), eq(1L))).thenReturn(false);

        mockMvc.perform(delete("/api/v1/orders/1/cancel")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .param("userId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("error attempting while cancelling the order")));
    }

    @Test
    public void testGetOrderByIdSuccess() throws Exception {
        OrderDTO orderDTO = new OrderDTO();
        Mockito.when(orderService.getById(eq(1L))).thenReturn(orderDTO);

        mockMvc.perform(get("/api/v1/orders/1")
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetOrdersByUserId() throws Exception {
        OrderDTO orderDTO = new OrderDTO();
        Page<OrderDTO> page = new PageImpl<>(Collections.singletonList(orderDTO));
        Mockito.when(orderService.getAllOrdersByUserId(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/orders/user/1")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetOrdersReadyToShip() throws Exception {
        OrderDTO orderDTO = new OrderDTO();
        Page<OrderDTO> page = new PageImpl<>(Collections.singletonList(orderDTO));
        Mockito.when(orderService.getAllOrdersReadyShip(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/orders/ready-to-ship")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetAllOrders() throws Exception {
        OrderDTO orderDTO = new OrderDTO();
        Page<OrderDTO> page = new PageImpl<>(Collections.singletonList(orderDTO));
        Mockito.when(orderService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/orders/getAll")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testChangeOrderStatusSuccess() throws Exception {
        Mockito.when(orderService.changeOrderStatus(eq(1L), any(OrderStatus.class))).thenReturn(true);

        mockMvc.perform(put("/api/v1/orders/1/set-status")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .param("orderStatus", OrderStatus.PAID.name()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("the order was successfully changed to the order")));
    }

    @Test
    public void testChangeOrderStatusFailure() throws Exception {
        Mockito.when(orderService.changeOrderStatus(eq(1L), any(OrderStatus.class))).thenReturn(false);

        mockMvc.perform(put("/api/v1/orders/1/set-status")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .param("orderStatus", OrderStatus.PENDING.name()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("error attempting while changing the order to the order")));
    }
}
