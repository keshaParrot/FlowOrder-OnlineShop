package github.keshaparrot.floworderonlineshop.services.interfaces;

import org.springframework.transaction.annotation.Transactional;

public interface IPaymentService {


    @Transactional
    boolean payOrder(Long userId, Long orderId, String blikCode);
}
