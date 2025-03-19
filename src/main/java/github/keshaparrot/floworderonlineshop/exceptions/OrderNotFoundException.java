package github.keshaparrot.floworderonlineshop.exceptions;

public class OrderNotFoundException extends EntityNotFoundException {
    public OrderNotFoundException(Long orderId) {
        super("Order with id " + orderId + " not found.");
    }
}

