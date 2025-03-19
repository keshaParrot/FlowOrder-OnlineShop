package github.keshaparrot.floworderonlineshop.model.dto;

import lombok.Data;
import lombok.Getter;

@Getter
public class LoginRequest {
    private String email;
    private String password;
}
