package github.keshaparrot.floworderonlineshop.model.dto;

import github.keshaparrot.floworderonlineshop.model.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestDTO {
    private Long id;
    private Long orderId;
    private Long userId;
    private BigDecimal refundAmount;
    private LocalDateTime requestDate;
    private String reason;
    private RefundStatus status;
}
