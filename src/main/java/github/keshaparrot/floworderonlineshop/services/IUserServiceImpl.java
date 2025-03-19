package github.keshaparrot.floworderonlineshop.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import github.keshaparrot.floworderonlineshop.config.JwtUtil;
import github.keshaparrot.floworderonlineshop.exceptions.UserNotFoundException;
import github.keshaparrot.floworderonlineshop.model.dto.CreateUserRequest;
import github.keshaparrot.floworderonlineshop.model.dto.LoginRequest;
import github.keshaparrot.floworderonlineshop.model.dto.UserDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Address;
import github.keshaparrot.floworderonlineshop.model.entity.User;
import github.keshaparrot.floworderonlineshop.model.enums.UserRole;
import github.keshaparrot.floworderonlineshop.model.mappers.UserMapper;
import github.keshaparrot.floworderonlineshop.repositories.UserRepository;
import github.keshaparrot.floworderonlineshop.services.interfaces.IUserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class IUserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FirebaseAuth firebaseAuth;
    private final JwtUtil jwtUtil;


    @Override
    public boolean register(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return false;
        }

        userRepository.save(
                User.builder()
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .role(UserRole.USER)
                        .build()
        );

        return true;
    }

    @Override
    public String login(LoginRequest request) throws FirebaseAuthException {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        Map<String, Object> claims = Map.of("role", user.getRole().toString());

        return jwtUtil.generateToken(user.getEmail(), claims);
    }

    @Override
    public void setUserRole(String email, UserRole role) throws FirebaseAuthException {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setRole(role);
            userRepository.save(user);

            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(user.getEmail())
                    .setCustomClaims(Map.of("role", role));

            firebaseAuth.updateUser(request);
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public UserDTO getDTOById(Long id) {
        User user = userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(id));
        return toDto(user);
    }

    @Override
    public User getEntityById(Long id) {
        return userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(id));
    }

    @Override
    public boolean addAddressById(Long userId, Address address) {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(userId));

        user.setAddress(address);
        userRepository.save(user);
        return true;
    }

    @Override
    public String updateById(UserDTO userDTO) {
        Optional<User> optionalUser = userRepository.findById(userDTO.getId());
        if (optionalUser.isEmpty()) {
            return "User not found";
        }

        User user = optionalUser.get();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());

        userRepository.save(user);

        return "User updated successfully";
    }

    private UserDTO toDto(User user) {
        return userMapper.toDto(user);
    }
}
