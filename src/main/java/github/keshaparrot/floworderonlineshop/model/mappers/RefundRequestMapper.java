package github.keshaparrot.floworderonlineshop.model.mappers;

import github.keshaparrot.floworderonlineshop.model.dto.RefundRequestDTO;
import github.keshaparrot.floworderonlineshop.model.entity.RefundRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RefundRequestMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "user.id", target = "userId")
    RefundRequestDTO toDto(RefundRequest refundRequest);

    @Mapping(target = "order", expression = "java(refundRequestDTO.getOrderId() == null ? null : github.keshaparrot.floworderonlineshop.model.entity.Order.builder().id(refundRequestDTO.getOrderId()).build())")
    @Mapping(target = "user", expression = "java(refundRequestDTO.getUserId() == null ? null : github.keshaparrot.floworderonlineshop.model.entity.User.builder().id(refundRequestDTO.getUserId()).build())")
    RefundRequest toEntity(RefundRequestDTO refundRequestDTO);
}
