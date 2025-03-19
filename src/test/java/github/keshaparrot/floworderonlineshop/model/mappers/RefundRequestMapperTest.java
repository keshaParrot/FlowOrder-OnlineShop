package github.keshaparrot.floworderonlineshop.model.mappers;

import github.keshaparrot.floworderonlineshop.model.dto.RefundRequestDTO;
import github.keshaparrot.floworderonlineshop.model.mappers.RefundRequestMapper;
import github.keshaparrot.floworderonlineshop.model.entity.RefundRequest;
import github.keshaparrot.floworderonlineshop.model.entity.Order;
import github.keshaparrot.floworderonlineshop.model.entity.User;
import github.keshaparrot.floworderonlineshop.model.enums.RefundStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class RefundRequestMapperTest {

    private final RefundRequestMapper mapper = Mappers.getMapper(RefundRequestMapper.class);

    @Test
    public void testToDto() {
        Order order = Order.builder().id(10L).build();
        User user = User.builder().id(20L).build();
        RefundRequest refundRequest = RefundRequest.builder()
                .id(5L)
                .order(order)
                .user(user)
                .refundAmount(new BigDecimal("100.50"))
                .requestDate(LocalDateTime.of(2025, 3, 17, 10, 0))
                .reason("Product defect")
                .status(RefundStatus.APPROVED)
                .build();

        RefundRequestDTO dto = mapper.toDto(refundRequest);

        assertNotNull(dto);
        assertEquals(refundRequest.getId(), dto.getId());
        assertEquals(order.getId(), dto.getOrderId());
        assertEquals(user.getId(), dto.getUserId());
        assertEquals(refundRequest.getRefundAmount(), dto.getRefundAmount());
        assertEquals(refundRequest.getRequestDate(), dto.getRequestDate());
        assertEquals(refundRequest.getReason(), dto.getReason());
        assertEquals(refundRequest.getStatus(), dto.getStatus());
    }

    @Test
    public void testToEntity() {
        RefundRequestDTO dto = RefundRequestDTO.builder()
                .id(5L)
                .orderId(10L)
                .userId(20L)
                .refundAmount(new BigDecimal("100.50"))
                .requestDate(LocalDateTime.of(2025, 3, 17, 10, 0))
                .reason("Product defect")
                .status(RefundStatus.APPROVED)
                .build();

        RefundRequest entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertNotNull(entity.getOrder());
        assertEquals(dto.getOrderId(), entity.getOrder().getId());
        assertNotNull(entity.getUser());
        assertEquals(dto.getUserId(), entity.getUser().getId());
        assertEquals(dto.getRefundAmount(), entity.getRefundAmount());
        assertEquals(dto.getRequestDate(), entity.getRequestDate());
        assertEquals(dto.getReason(), entity.getReason());
        assertEquals(dto.getStatus(), entity.getStatus());
    }
}
