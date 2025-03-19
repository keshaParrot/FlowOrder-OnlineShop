package github.keshaparrot.floworderonlineshop.services.interfaces;

import github.keshaparrot.floworderonlineshop.model.dto.CreateProductRequest;
import github.keshaparrot.floworderonlineshop.model.dto.ProductDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Order;
import github.keshaparrot.floworderonlineshop.model.entity.Product;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

public interface IProductService {

    ProductDTO getProductById(Long id);
    Page<ProductDTO> getAll(Pageable pageable);
    Page<ProductDTO> sort(String category, BigDecimal priceFrom, BigDecimal priceTo,String searchQuery, Pageable pageable);
    ProductDTO create(CreateProductRequest createRequest, MultipartFile file) throws IOException;
    void returnItemsToShop(Order orders);
    ProductDTO updateContentById(Long id, CreateProductRequest updateRequest) throws ChangeSetPersister.NotFoundException;
    ProductDTO updatePhotoById(Long id, MultipartFile file) throws IOException, ChangeSetPersister.NotFoundException;
    boolean deleteById(Long id);
}
