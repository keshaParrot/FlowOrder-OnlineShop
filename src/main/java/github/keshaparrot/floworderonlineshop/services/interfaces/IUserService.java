package github.keshaparrot.floworderonlineshop.services.interfaces;

import com.google.firebase.auth.FirebaseAuthException;
import github.keshaparrot.floworderonlineshop.model.dto.CreateUserRequest;
import github.keshaparrot.floworderonlineshop.model.dto.LoginRequest;
import github.keshaparrot.floworderonlineshop.model.dto.UserDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Address;
import github.keshaparrot.floworderonlineshop.model.entity.User;
import github.keshaparrot.floworderonlineshop.model.enums.UserRole;

public interface IUserService {
    boolean register(CreateUserRequest request);
    String login(LoginRequest request) throws FirebaseAuthException;
    void setUserRole(String email, UserRole role) throws FirebaseAuthException;
    UserDTO getDTOById(Long id);
    User getEntityById(Long id);
    boolean addAddressById(Long userId, Address address);
    String updateById(UserDTO userDTO);
}
