package github.keshaparrot.floworderonlineshop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {

    private Long id;

    private String title;
    private String description;
    private int quantity;
    private String photoPath;

    private BigDecimal price;
    private String category;

    private boolean refundable;
}
