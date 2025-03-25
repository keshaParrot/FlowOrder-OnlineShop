package github.keshaparrot.floworderonlineshop.exceptions;

public class ProductOutOfStockException extends EntityNotFoundException {
    public ProductOutOfStockException(Long productId, int currentStock, int requestedQuantity) {
        super(String.format("The product with ID %s is out of stock in the requested quantity. " +
                        "Current stock: %d, Requested quantity: %d.",
                productId, currentStock, requestedQuantity));
    }
}