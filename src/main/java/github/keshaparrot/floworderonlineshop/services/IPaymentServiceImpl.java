package github.keshaparrot.floworderonlineshop.services;

import github.keshaparrot.floworderonlineshop.exceptions.OrderNotFoundException;
import github.keshaparrot.floworderonlineshop.exceptions.ProductNotFoundException;
import github.keshaparrot.floworderonlineshop.exceptions.ProductOutOfStockException;
import github.keshaparrot.floworderonlineshop.exceptions.UserNotFoundException;
import github.keshaparrot.floworderonlineshop.model.entity.Order;
import github.keshaparrot.floworderonlineshop.model.entity.OrderItem;
import github.keshaparrot.floworderonlineshop.model.entity.Product;
import github.keshaparrot.floworderonlineshop.model.entity.User;
import github.keshaparrot.floworderonlineshop.model.enums.BillType;
import github.keshaparrot.floworderonlineshop.model.enums.OrderStatus;
import github.keshaparrot.floworderonlineshop.repositories.OrderRepository;
import github.keshaparrot.floworderonlineshop.repositories.ProductRepository;
import github.keshaparrot.floworderonlineshop.repositories.UserRepository;
import github.keshaparrot.floworderonlineshop.services.interfaces.IPaymentService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class IPaymentServiceImpl implements IPaymentService {

    private final ITransactionServiceImpl transactionService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public boolean payOrder(Long userId, Long orderId, String blikCode) {
        if (!validateBlikCode(blikCode)) return false;

        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(userId));
        if (optionalOrder.isEmpty()) return false;

        Order order = optionalOrder.get();
        if (!order.getUser().getId().equals(userId) || order.getStatus() != OrderStatus.PENDING) {
            return false;
        }

        for (OrderItem item : order.getOrderItems()) {
            Long productId = item.getProduct().getId();
            Product product = productRepository.findById(productId).orElseThrow(()-> new ProductNotFoundException(productId));
            if (product.getQuantity() < item.getQuantity()) {
                throw new ProductOutOfStockException(productId,product.getQuantity(),item.getQuantity());
            }
            else{
                product.setQuantity(product.getQuantity() - item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(OrderStatus.PAID);
        transactionService.createTransaction(user,order, BillType.BUYING);
        orderRepository.save(order);
        return true;
    }

    private boolean validateBlikCode(String blikCode) {
        return true; //Later, it will be possible to integrate a real mechanism for checking the code of a blik
    }

}
