package github.keshaparrot.floworderonlineshop.services;

import github.keshaparrot.floworderonlineshop.exceptions.ProductNotFoundException;
import github.keshaparrot.floworderonlineshop.model.dto.CreateProductRequest;
import github.keshaparrot.floworderonlineshop.model.dto.ProductDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Product;
import github.keshaparrot.floworderonlineshop.model.mappers.ProductMapper;
import github.keshaparrot.floworderonlineshop.repositories.ProductRepository;
import github.keshaparrot.floworderonlineshop.services.IProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private IProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productService, "uploadDir", "src/main/resources/static/images");
    }

    @Test
    public void testGetProductById_Success() {
        Long productId = (Long) 1L;
        Product product = new Product();
        product.setId(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        when(productMapper.toDTO(any(Product.class))).thenAnswer(invocation -> {
            Product emp = invocation.getArgument(0);
            return ProductDTO.builder()
                    .id(emp.getId())
                    .build();
        });

        ProductDTO result = productService.getProductById(productId);

        assertNotNull(result);
        verify(productRepository).findById(productId);
        verify(productMapper).toDTO(product);
    }

    @Test
    public void testGetProductById_NotFound() {
        Long productId = (Long) 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            productService.getProductById(productId);
        });
        verify(productRepository).findById(productId);
    }


    @Test
    public void testCreateProduct() throws IOException {


        CreateProductRequest createRequest = new CreateProductRequest();
        createRequest.setTitle("Test Product");
        createRequest.setDescription("Test Description");
        createRequest.setQuantity(10);
        createRequest.setPrice(BigDecimal.valueOf(100));
        createRequest.setCategory("Electronics");

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image data".getBytes());

        Product product = new Product();
        product.setId(1L);
        product.setTitle(createRequest.getTitle());
        product.setDescription(createRequest.getDescription());
        product.setQuantity(createRequest.getQuantity());
        product.setPrice(createRequest.getPrice());
        product.setCategory(createRequest.getCategory());

        when(productRepository.save(any(Product.class))).thenReturn(product);

        when(productMapper.toDTO(any(Product.class))).thenAnswer(invocation -> {
            Product prod = invocation.getArgument(0);
            return ProductDTO.builder()
                    .id(prod.getId())
                    .title(prod.getTitle())
                    .description(prod.getDescription())
                    .quantity(prod.getQuantity())
                    .price(prod.getPrice())
                    .category(prod.getCategory())
                    .build();
        });

        ProductDTO result = productService.create(createRequest, file);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
        verify(productMapper).toDTO(product);
    }

    @Test
    public void testUpdateContentById_Success() throws ChangeSetPersister.NotFoundException {
        Long productId = 1L;
        CreateProductRequest updateRequest = new CreateProductRequest();
        updateRequest.setTitle("Updated Product");

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setTitle("Updated Product");
        when(productRepository.save(existingProduct)).thenReturn(updatedProduct);

        when(productMapper.toDTO(any(Product.class))).thenAnswer(invocation -> {
            Product prod = invocation.getArgument(0);
            return ProductDTO.builder()
                    .id(prod.getId())
                    .title(prod.getTitle())
                    .description(prod.getDescription())
                    .quantity(prod.getQuantity())
                    .price(prod.getPrice())
                    .category(prod.getCategory())
                    .build();
        });

        ProductDTO result = productService.updateContentById(productId, updateRequest);

        assertNotNull(result);
        verify(productRepository).findById(productId);
        verify(productRepository).save(existingProduct);
    }

    @Test
    public void testUpdateContentById_NotFound() {
        Long productId = 1L;
        CreateProductRequest updateRequest = new CreateProductRequest();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            productService.updateContentById(productId, updateRequest);
        });
        verify(productRepository).findById(productId);
    }



    @Test
    public void testDeleteById_Success() {
        Long productId = 1L;
        when(productRepository.deleteProductById(productId)).thenReturn(true);

        boolean result = productService.deleteById(productId);

        assertTrue(result);
        verify(productRepository).deleteProductById(productId);
    }

    @Test
    public void testDeleteById_Failure() {
        Long productId = 1L;
        when(productRepository.deleteProductById(productId)).thenReturn(false);

        boolean result = productService.deleteById(productId);

        assertFalse(result);
        verify(productRepository).deleteProductById(productId);
    }

    @Test
    public void testSort() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        String category = "Electronics";
        BigDecimal priceFrom = BigDecimal.valueOf(50);
        BigDecimal priceTo = BigDecimal.valueOf(200);
        String searchQuery = "Test";

        Page<Product> productPage = new PageImpl<>(List.of(Product.builder()
                        .id(1L)
                        .title(searchQuery)
                        .price(BigDecimal.valueOf(150))
                        .category(category)
                        .build()));

        when(productRepository.sort(category, priceFrom, priceTo, searchQuery, pageRequest)).thenReturn(productPage);
        when(productMapper.toDTO(any(Product.class))).thenAnswer(invocation -> {
            Product prod = invocation.getArgument(0);
            return ProductDTO.builder()
                    .id(prod.getId())
                    .title(prod.getTitle())
                    .price(prod.getPrice())
                    .category(prod.getCategory())
                    .build();
        });

        Page<ProductDTO> result = productService.sort(category, priceFrom, priceTo, searchQuery, pageRequest);

        assert(result.getTotalElements()!=0);
        verify(productRepository).sort(category, priceFrom, priceTo, searchQuery, pageRequest);
    }
}