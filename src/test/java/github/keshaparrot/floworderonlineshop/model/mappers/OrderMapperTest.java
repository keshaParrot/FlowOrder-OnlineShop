package github.keshaparrot.floworderonlineshop.model.mappers;

import github.keshaparrot.floworderonlineshop.model.dto.OrderDTO;
import github.keshaparrot.floworderonlineshop.model.dto.OrderItemDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Order;
import github.keshaparrot.floworderonlineshop.model.entity.OrderItem;
import github.keshaparrot.floworderonlineshop.model.entity.Product;
import github.keshaparrot.floworderonlineshop.model.entity.User;
import github.keshaparrot.floworderonlineshop.model.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderMapperTest {

    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    @Test
    public void testOrderItemToDto() {
        Product product = Product.builder()
                .id(200L)
                .title("Product Title")
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(300L)
                .product(product)
                .quantity(2)
                .price(new BigDecimal("50.00"))
                .build();

        OrderItemDTO orderItemDTO = orderMapper.toDto(orderItem);

        assertNotNull(orderItemDTO);
        assertEquals(orderItem.getId(), orderItemDTO.getId());
        assertEquals(product.getId(), orderItemDTO.getProductId());
        assertEquals(product.getTitle(), orderItemDTO.getProductName());
        assertEquals(orderItem.getQuantity(), orderItemDTO.getQuantity());
        assertEquals(orderItem.getPrice(), orderItemDTO.getPrice());
    }

    @Test
    public void testOrderToDto() {
        User user = new User();
        user.setId(100L);

        Product product = Product.builder()
                .id(200L)
                .title("Product Title")
                .description("Product Desc")
                .quantity(10)
                .photoPath("/img/photo.jpg")
                .price(new BigDecimal("99.99"))
                .category("Category")
                .refundable(true)
                .addTime(LocalDateTime.of(2025, 3, 17, 10, 0))
                .modifyTime(LocalDateTime.of(2025, 3, 18, 10, 0))
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(300L)
                .product(product)
                .quantity(3)
                .price(new BigDecimal("299.97"))
                .build();

        Order order = Order.builder()
                .id(400L)
                .user(user)
                .orderDate(LocalDateTime.of(2025, 3, 19, 12, 0))
                .status(OrderStatus.PENDING)
                .build();

        order.setOrderItems(List.of(orderItem));
        orderItem.setOrder(order);

        OrderDTO orderDTO = orderMapper.toDto(order);

        assertNotNull(orderDTO);
        assertEquals(order.getId(), orderDTO.getId());
        assertEquals(user.getId(), orderDTO.getUserId());
        assertEquals(order.getOrderDate(), orderDTO.getOrderDate());
        assertEquals(order.getStatus(), orderDTO.getStatus());
        assertNotNull(orderDTO.getOrderItems());
        assertEquals(1, orderDTO.getOrderItems().size());

        OrderItemDTO itemDTO = orderDTO.getOrderItems().get(0);
        assertEquals(orderItem.getId(), itemDTO.getId());
        assertEquals(product.getId(), itemDTO.getProductId());
        assertEquals(product.getTitle(), itemDTO.getProductName());
        assertEquals(orderItem.getQuantity(), itemDTO.getQuantity());
        assertEquals(orderItem.getPrice(), itemDTO.getPrice());
    }
}
