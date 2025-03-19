package github.keshaparrot.floworderonlineshop.model.dto;


import github.keshaparrot.floworderonlineshop.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import github.keshaparrot.floworderonlineshop.model.entity.Address;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private UserRole role;
    private boolean verified;
    private Address address;
}
