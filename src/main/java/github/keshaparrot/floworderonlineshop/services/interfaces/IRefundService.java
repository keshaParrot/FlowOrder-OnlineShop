package github.keshaparrot.floworderonlineshop.services.interfaces;

import github.keshaparrot.floworderonlineshop.model.dto.OrderDTO;
import github.keshaparrot.floworderonlineshop.model.enums.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface IRefundService {


    @Transactional
    boolean refundOrder(Long orderId, String reason);

    Page<OrderDTO> getAllRefundedOrders(Pageable pageable);

    @Transactional
    boolean changeRefundStatus(Long orderId, RefundStatus refundStatus, boolean returnItemToShop);

    @Transactional
    boolean approveOrderRefund(Long orderId, boolean returnItemToShop);

    @Transactional
    boolean declineOrderRefund(Long orderId);
}
