package github.keshaparrot.floworderonlineshop.model.entity;

import github.keshaparrot.floworderonlineshop.model.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private BigDecimal refundAmount;
    private LocalDateTime requestDate;
    private String reason;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    public RefundRequest() {
        this.requestDate = LocalDateTime.now();
        this.status = RefundStatus.PENDING;
    }
}