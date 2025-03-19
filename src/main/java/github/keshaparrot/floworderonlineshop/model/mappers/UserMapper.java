package github.keshaparrot.floworderonlineshop.model.mappers;

import github.keshaparrot.floworderonlineshop.model.dto.UserDTO;
import github.keshaparrot.floworderonlineshop.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserDTO toDto(User user);

    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "refundRequests", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserDTO userDTO);
}
