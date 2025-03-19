package github.keshaparrot.floworderonlineshop.model.entity;

import github.keshaparrot.floworderonlineshop.model.enums.BillType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime issueDate;

    private String seller;
    private Long buyer;

    @ElementCollection
    @CollectionTable(name = "bill_products", joinColumns = @JoinColumn(name = "bill_id"))
    @MapKeyColumn(name = "product_name")
    @Column(name = "quantity")
    private Map<String, Integer> products;

    private BigDecimal totalAmount;
    private BigDecimal vat;
    private String paymentMethod;
    private String status;
    private BillType Type;
    private UUID transactionNumber;
    private String salesAddress;
    private String currency;
}

