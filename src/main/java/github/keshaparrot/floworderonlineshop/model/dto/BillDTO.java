package github.keshaparrot.floworderonlineshop.model.dto;

import github.keshaparrot.floworderonlineshop.model.enums.BillType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillDTO {
    private Long id;
    private LocalDateTime issueDate;
    private String seller;
    private String buyer;
    private Map<String, Integer> products;
    private BigDecimal totalAmount;
    private BigDecimal vat;
    private String paymentMethod;
    private String status;
    private BillType type;
    private UUID transactionNumber;
    private String salesAddress;
    private String currency;

}