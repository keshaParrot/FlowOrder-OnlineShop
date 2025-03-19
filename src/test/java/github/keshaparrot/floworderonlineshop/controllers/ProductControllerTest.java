package github.keshaparrot.floworderonlineshop.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import github.keshaparrot.floworderonlineshop.config.JwtUtil;
import github.keshaparrot.floworderonlineshop.model.dto.CreateProductRequest;
import github.keshaparrot.floworderonlineshop.model.dto.ProductDTO;
import github.keshaparrot.floworderonlineshop.services.interfaces.IProductService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IProductService productService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public IProductService productService() {
            return Mockito.mock(IProductService.class);
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
    public void testGetProductByIdSuccess() throws Exception {
        ProductDTO productDTO = new ProductDTO();
        Mockito.when(productService.getProductById(1L)).thenReturn(productDTO);

        mockMvc.perform(get("/api/v1/products/get/1")
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetProductByIdNotFound() throws Exception {
        Mockito.when(productService.getProductById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/products/get/1")
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetProductPhotoNotFound_ProductNotFound() throws Exception {
        Mockito.when(productService.getProductById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/products/get/1/photo")
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetProductPhotoNotFound_NoPhotoPath() throws Exception {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setPhotoPath(null);
        Mockito.when(productService.getProductById(1L)).thenReturn(productDTO);

        mockMvc.perform(get("/api/v1/products/get/1/photo")
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetProductPhotoNotFound_FileNotExists() throws Exception {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setPhotoPath("nonexistent.jpg");
        Mockito.when(productService.getProductById(1L)).thenReturn(productDTO);

        mockMvc.perform(get("/api/v1/products/get/1/photo")
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetProductPhotoSuccess() throws Exception {
        Path directory = Paths.get("src/main/resources/static/images");
        Files.createDirectories(directory);
        Path filePath = directory.resolve("test.jpg");
        Files.write(filePath, "dummy image content".getBytes());

        ProductDTO productDTO = new ProductDTO();
        productDTO.setPhotoPath("test.jpg");
        Mockito.when(productService.getProductById(1L)).thenReturn(productDTO);

        mockMvc.perform(get("/api/v1/products/get/1/photo")
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("image")));

        Files.deleteIfExists(filePath);
    }

    @Test
    public void testGetAllProducts() throws Exception {
        ProductDTO productDTO = new ProductDTO();
        Page<ProductDTO> page = new PageImpl<>(Collections.singletonList(productDTO));
        Mockito.when(productService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/products/get/all")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testCreateProductSuccess() throws Exception {
        CreateProductRequest createRequest = new CreateProductRequest();
        createRequest.setTitle("Test Product");
        createRequest.setDescription("Test Description");
        createRequest.setQuantity(10);
        createRequest.setPrice(BigDecimal.valueOf(99.99));
        createRequest.setCategory("Test Category");
        String requestJson = objectMapper.writeValueAsString(createRequest);

        MockMultipartFile jsonPart = new MockMultipartFile("createProductRequest", "", "application/json", requestJson.getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy image".getBytes());

        ProductDTO productDTO = new ProductDTO();
        productDTO.setTitle("Test Product");
        Mockito.when(productService.create(any(CreateProductRequest.class), any())).thenReturn(productDTO);

        mockMvc.perform(multipart("/api/v1/products/create")
                        .file(jsonPart)
                        .file(filePart)
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Test Product"));
    }

    @Test
    public void testUpdateProductSuccess() throws Exception {
        CreateProductRequest updateRequest = new CreateProductRequest();
        updateRequest.setTitle("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setQuantity(5);
        updateRequest.setPrice(BigDecimal.valueOf(49.99));
        updateRequest.setCategory("Updated Category");

        String requestJson = objectMapper.writeValueAsString(updateRequest);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setTitle("Updated Product");
        Mockito.when(productService.updateContentById(eq(1L), any(CreateProductRequest.class))).thenReturn(productDTO);

        mockMvc.perform(put("/api/v1/products/update/1")
                        .header("Authorization", "Bearer mocked-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Product"));
    }

    @Test
    public void testUpdatePhotoSuccess() throws Exception {
        MockMultipartFile filePart = new MockMultipartFile("file", "updated.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy image".getBytes());

        ProductDTO productDTO = new ProductDTO();
        productDTO.setTitle("Updated Product Photo");
        Mockito.when(productService.updatePhotoById(eq(1L), any())).thenReturn(productDTO);

        mockMvc.perform(multipart("/api/v1/products/update/1/photo")
                        .file(filePart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Product Photo"));
    }

    @Test
    public void testDeleteProductSuccess() throws Exception {
        Mockito.when(productService.deleteById(eq(1L))).thenReturn(true);

        mockMvc.perform(delete("/api/v1/products/delete/1")
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(containsString("Product was deleted successfully")));
    }

    @Test
    public void testDeleteProductNotFound() throws Exception {
        Mockito.when(productService.deleteById(eq(1L))).thenReturn(false);

        mockMvc.perform(delete("/api/v1/products/delete/1")
                        .header("Authorization", "Bearer mocked-jwt-token"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Product with id 1 was not found")));
    }
}
