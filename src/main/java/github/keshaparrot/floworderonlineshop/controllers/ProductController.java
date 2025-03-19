package github.keshaparrot.floworderonlineshop.controllers;

import github.keshaparrot.floworderonlineshop.model.dto.CreateProductRequest;
import github.keshaparrot.floworderonlineshop.model.dto.ProductDTO;
import github.keshaparrot.floworderonlineshop.services.interfaces.IProductService;
import org.springframework.core.io.Resource;
import java.nio.file.Path;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/products")
public class ProductController {

    private final IProductService productService;

    @GetMapping("/get/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return (product != null) ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }

    @GetMapping("/get/{id}/photo")
    public ResponseEntity<Resource> getProductPhoto(@PathVariable Long id) {
        ProductDTO productDTO = productService.getProductById(id);
        if (productDTO == null || productDTO.getPhotoPath() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path imagePath = Paths.get("src/main/resources/static/images", productDTO.getPhotoPath());

            Resource resource = new UrlResource(imagePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(imagePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get/all")
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal priceFrom,
            @RequestParam(required = false) BigDecimal priceTo,
            @RequestParam(required = false) String searchQuery,
            Pageable pageable) {

        Page<ProductDTO> products;
        if (category != null || priceFrom != null || priceTo != null || searchQuery != null) {
            products = productService.sort(category, priceFrom, priceTo, searchQuery, pageable);
        } else {
            products = productService.getAll(pageable);
        }
        return ResponseEntity.ok(products);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,path ="/create")
    public ResponseEntity<ProductDTO> createProduct(
            @RequestPart("createProductRequest") @Valid CreateProductRequest request,
            @RequestPart("file") MultipartFile file) throws IOException {

        ProductDTO product = productService.create(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);

    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid CreateProductRequest request) throws ChangeSetPersister.NotFoundException {

        ProductDTO updatedProduct = productService.updateContentById(id, request);
        return ResponseEntity.ok(updatedProduct);
    }

    @PutMapping(value = "/update/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> updatePhoto(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) throws ChangeSetPersister.NotFoundException, IOException {

        ProductDTO updatedProduct = productService.updatePhotoById(id, file);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteById(id);
        return deleted
                ? ResponseEntity.status(HttpStatus.NO_CONTENT).body("Product was deleted successfully")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product with id "+id+" was not found");
    }
}
