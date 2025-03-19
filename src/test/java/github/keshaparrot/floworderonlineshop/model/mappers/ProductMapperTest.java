package github.keshaparrot.floworderonlineshop.model.mappers;

import github.keshaparrot.floworderonlineshop.model.dto.ProductDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ProductMapperTest {

    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Test
    public void testToDTO() {
        Product product = Product.builder()
                .id(1L)
                .title("Test Product")
                .description("Test Description")
                .quantity(10)
                .photoPath("/path/to/photo.jpg")
                .price(new BigDecimal("99.99"))
                .category("Test Category")
                .refundable(true)
                .addTime(LocalDateTime.of(2025, 3, 17, 10, 0))
                .modifyTime(LocalDateTime.of(2025, 3, 18, 10, 0))
                .build();

        ProductDTO dto = productMapper.toDTO(product);

        assertNotNull(dto);
        assertEquals(product.getId(), dto.getId());
        assertEquals(product.getTitle(), dto.getTitle());
        assertEquals(product.getDescription(), dto.getDescription());
        assertEquals(product.getQuantity(), dto.getQuantity());
        assertEquals(product.getPhotoPath(), dto.getPhotoPath());
        assertEquals(product.getPrice(), dto.getPrice());
        assertEquals(product.getCategory(), dto.getCategory());
        assertEquals(product.isRefundable(), dto.isRefundable());
    }

    @Test
    public void testToEntity() {
        ProductDTO dto = ProductDTO.builder()
                .id(1L)
                .title("Test Product")
                .description("Test Description")
                .quantity(10)
                .photoPath("/path/to/photo.jpg")
                .price(new BigDecimal("99.99"))
                .category("Test Category")
                .refundable(true)
                .build();

        Product product = productMapper.toEntity(dto);

        assertNotNull(product);
        assertEquals(dto.getId(), product.getId());
        assertEquals(dto.getTitle(), product.getTitle());
        assertEquals(dto.getDescription(), product.getDescription());
        assertEquals(dto.getQuantity(), product.getQuantity());
        assertEquals(dto.getPhotoPath(), product.getPhotoPath());
        assertEquals(dto.getPrice(), product.getPrice());
        assertEquals(dto.getCategory(), product.getCategory());
        assertEquals(dto.isRefundable(), product.isRefundable());
    }
}
