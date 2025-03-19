package github.keshaparrot.floworderonlineshop.controllers;

import github.keshaparrot.floworderonlineshop.model.dto.UserDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Address;
import github.keshaparrot.floworderonlineshop.services.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getDTOById(id));
    }

    @PostMapping("/{id}/address")
    public ResponseEntity<String> addAddress(@PathVariable Long id, @RequestBody Address address) {
        boolean success = userService.addAddressById(id, address);
        return success ? ResponseEntity.ok("Address added successfully") : ResponseEntity.badRequest().body("Failed to add address");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        userDTO.setId(id);
        String result = userService.updateById(userDTO);
        return result.equals("User updated successfully") ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }
}
