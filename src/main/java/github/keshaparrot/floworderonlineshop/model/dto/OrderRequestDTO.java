package github.keshaparrot.floworderonlineshop.model.dto;

import lombok.*;

import java.util.Map;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequestDTO {

    private Long userId;
    private Map<Long, Integer> productQuantities;
}
