package github.keshaparrot.floworderonlineshop.model.mappers;

import github.keshaparrot.floworderonlineshop.model.dto.BillDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Bill;
import github.keshaparrot.floworderonlineshop.services.interfaces.IUserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class BillMapper {

    protected IUserService userService;

    @Mapping(target = "buyer", expression = "java(userService.getEntityById(bill.getBuyer()).getFullName())")
    public abstract BillDTO toDto(Bill bill);

    @Autowired
    public void setUserService(IUserService userService) {
        this.userService = userService;
    }
}
