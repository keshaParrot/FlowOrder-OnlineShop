package github.keshaparrot.floworderonlineshop.services;

import github.keshaparrot.floworderonlineshop.exceptions.ProductNotFoundException;
import github.keshaparrot.floworderonlineshop.model.dto.CreateProductRequest;
import github.keshaparrot.floworderonlineshop.model.dto.ProductDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Order;
import github.keshaparrot.floworderonlineshop.model.entity.OrderItem;
import github.keshaparrot.floworderonlineshop.model.entity.Product;
import github.keshaparrot.floworderonlineshop.model.mappers.ProductMapper;
import github.keshaparrot.floworderonlineshop.repositories.ProductRepository;
import github.keshaparrot.floworderonlineshop.services.interfaces.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IProductServiceImpl implements IProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id).map(this::toDTO).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public Page<ProductDTO> getAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    public Page<ProductDTO> sort(String category, BigDecimal priceFrom, BigDecimal priceTo, String searchQuery, Pageable pageable) {
        return productRepository.sort(category, priceFrom, priceTo, searchQuery, pageable).map(this::toDTO);
    }

    @Override
    public ProductDTO create(CreateProductRequest createRequest, MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + File.separator + fileName);
        Files.copy(file.getInputStream(), filePath);

        Product createProduct = Product
                .builder()
                .title(createRequest.getTitle())
                .description(createRequest.getDescription())
                .quantity(createRequest.getQuantity())
                .photoPath(fileName)
                .price(createRequest.getPrice())
                .category(createRequest.getCategory())
                .addTime(LocalDateTime.now())
                .modifyTime(LocalDateTime.now())
                .build();

        Product savedProduct = productRepository.save(createProduct);
        return toDTO(savedProduct);
    }

    @Override
    public void returnItemsToShop(Order order){
        for(OrderItem orderItem:order.getOrderItems()){
            if(orderItem.getProduct().isRefundable()){
                Product product = productRepository.findById(orderItem.getProduct().getId()).get();
                product.setQuantity(product.getQuantity() + orderItem.getQuantity());
                productRepository.save(product);
            }
        }
    }

    @Override
    public ProductDTO updateContentById(Long id, CreateProductRequest updateRequest) throws ChangeSetPersister.NotFoundException {
        Product updateProduct = productRepository.findById(id).orElseThrow(()-> new ProductNotFoundException(id));

        if (updateRequest.getTitle() != null) {
            updateProduct.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getDescription() != null) {
            updateProduct.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getQuantity() != null) {
            updateProduct.setQuantity(updateRequest.getQuantity());
        }
        if (updateRequest.getPrice() != null) {
            updateProduct.setPrice(updateRequest.getPrice());
        }
        if (updateRequest.getCategory() != null) {
            updateProduct.setCategory(updateRequest.getCategory());
        }

        Product savedProduct = productRepository.save(updateProduct);
        return toDTO(savedProduct);
    }

    @Override
    public ProductDTO updatePhotoById(Long id, MultipartFile file) throws IOException, ChangeSetPersister.NotFoundException {
        Product updateProduct = productRepository.findById(id).orElseThrow(()-> new ProductNotFoundException(id));

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + File.separator + fileName);
        Files.copy(file.getInputStream(), filePath);

        updateProduct.setPhotoPath(fileName);
        Product savedProduct = productRepository.save(updateProduct);
        return toDTO(savedProduct);
    }
    @Override
    public boolean deleteById(Long id) {
        return productRepository.deleteProductById(id);
    }

    ProductDTO toDTO(Product product) {
        return productMapper.toDTO(product);
    }
    Product toEntity(ProductDTO productDTO) {
        return productMapper.toEntity(productDTO);
    }
}
