package github.keshaparrot.floworderonlineshop.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private int quantity;
    private String photoPath;

    private BigDecimal price;
    private String category;

    private boolean refundable;
    private LocalDateTime addTime;
    private LocalDateTime modifyTime;


}
