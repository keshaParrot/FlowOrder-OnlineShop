package github.keshaparrot.floworderonlineshop.controllers;

import github.keshaparrot.floworderonlineshop.model.dto.UserDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Address;
import github.keshaparrot.floworderonlineshop.services.interfaces.IUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private IUserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void getUserById_ShouldReturnUserDTO() {
        Long userId = 1L;
        UserDTO userDTO = UserDTO.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        when(userService.getDTOById(userId)).thenReturn(userDTO);

        ResponseEntity<UserDTO> response = userController.getUserById(userId);

        assertNotNull(response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("John", response.getBody().getFirstName());
    }

    @Test
    void addAddress_ShouldReturnSuccessMessage() {
        Long userId = 1L;
        Address address = new Address("123 Main St", "City", "12345", "Country");
        when(userService.addAddressById(userId, address)).thenReturn(true);

        ResponseEntity<String> response = userController.addAddress(userId, address);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Address added successfully", response.getBody());
    }

    @Test
    void updateUser_ShouldReturnSuccessMessage() {
        Long userId = 1L;
        UserDTO userDTO = UserDTO.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        when(userService.updateById(userDTO)).thenReturn("User updated successfully");

        ResponseEntity<String> response = userController.updateUser(userId, userDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User updated successfully", response.getBody());
    }

    @Test
    void updateUser_ShouldReturnNotFound() {
        Long userId = 1L;
        UserDTO userDTO = UserDTO.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        when(userService.updateById(userDTO)).thenReturn("User not found");

        ResponseEntity<String> response = userController.updateUser(userId, userDTO);

        assertEquals(404, response.getStatusCodeValue());
    }
}
