package github.keshaparrot.floworderonlineshop.model.mappers;

import github.keshaparrot.floworderonlineshop.model.dto.UserDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Address;
import github.keshaparrot.floworderonlineshop.model.entity.User;
import github.keshaparrot.floworderonlineshop.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

public class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    public void testToDto() {
        Address address = Address.builder()
                .street("123 Main St")
                .city("Kyiv")
                .country("Ukraine")
                .postalCode("01001")
                .build();
        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .role(UserRole.USER)
                .verified(true)
                .address(address)
                .build();
        UserDTO dto = userMapper.toDto(user);
        assertNotNull(dto);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getFirstName(), dto.getFirstName());
        assertEquals(user.getLastName(), dto.getLastName());
        assertEquals("John Doe", dto.getFullName());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getRole(), dto.getRole());
        assertEquals(user.isVerified(), dto.isVerified());
        assertEquals(user.getAddress(), dto.getAddress());
    }

    @Test
    public void testToEntity() {
        Address address = Address.builder()
                .street("123 Main St")
                .city("Kyiv")
                .country("Ukraine")
                .postalCode("01001")
                .build();
        UserDTO dto = UserDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .role(UserRole.USER)
                .verified(true)
                .address(address)
                .build();
        User user = userMapper.toEntity(dto);
        assertNotNull(user);
        assertEquals(dto.getId(), user.getId());
        assertEquals(dto.getFirstName(), user.getFirstName());
        assertEquals(dto.getLastName(), user.getLastName());
        assertEquals(dto.getEmail(), user.getEmail());
        assertEquals(dto.getRole(), user.getRole());
        assertEquals(dto.isVerified(), user.isVerified());
        assertEquals(dto.getAddress(), user.getAddress());
        assertEquals(dto.getFirstName() + " " + dto.getLastName(), user.getFullName());
    }
}
