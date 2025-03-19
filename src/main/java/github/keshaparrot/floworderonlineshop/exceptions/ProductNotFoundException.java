package github.keshaparrot.floworderonlineshop.exceptions;

public class ProductNotFoundException extends EntityNotFoundException {
    public ProductNotFoundException(Long productId) {
        super("Product with id " + productId + " not found.");
    }
}



