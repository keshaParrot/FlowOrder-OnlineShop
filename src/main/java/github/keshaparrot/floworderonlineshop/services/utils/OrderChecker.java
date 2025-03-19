package github.keshaparrot.floworderonlineshop.services.utils;

import github.keshaparrot.floworderonlineshop.model.entity.Order;
import github.keshaparrot.floworderonlineshop.model.enums.OrderStatus;
import github.keshaparrot.floworderonlineshop.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class OrderChecker {

    private final OrderRepository orderRepository;

    @Scheduled(fixedRate = 3600000)
    public void checkAndDeleteExpiredOrders() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);

        for (Order order : pendingOrders) {
            if (order.getOrderDate().isBefore(twentyFourHoursAgo)) {
                orderRepository.delete(order);
            }
        }
    }
}

