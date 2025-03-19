package github.keshaparrot.floworderonlineshop.model.mappers;

import github.keshaparrot.floworderonlineshop.model.dto.ProductDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(ProductDTO productDTO);

    ProductDTO toDTO(Product product);
}
