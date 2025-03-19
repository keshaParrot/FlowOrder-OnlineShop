package github.keshaparrot.floworderonlineshop.services;

import github.keshaparrot.floworderonlineshop.exceptions.OrderNotFoundException;
import github.keshaparrot.floworderonlineshop.model.dto.OrderDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Order;
import github.keshaparrot.floworderonlineshop.model.entity.RefundRequest;
import github.keshaparrot.floworderonlineshop.model.enums.BillType;
import github.keshaparrot.floworderonlineshop.model.enums.RefundStatus;
import github.keshaparrot.floworderonlineshop.model.mappers.OrderMapper;
import github.keshaparrot.floworderonlineshop.repositories.OrderRepository;
import github.keshaparrot.floworderonlineshop.repositories.RefundRequestRepository;
import github.keshaparrot.floworderonlineshop.services.interfaces.IPaymentService;
import github.keshaparrot.floworderonlineshop.services.interfaces.IProductService;
import github.keshaparrot.floworderonlineshop.services.interfaces.IRefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IRefundServiceImpl implements IRefundService {

    private final OrderRepository orderRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final OrderMapper orderMapper;
    private final IPaymentService paymentService;
    private final IProductService productService;

    @Override
    @Transactional
    public boolean refundOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getRefundRequests().isEmpty()) {
            return false;
        }

        if (order.getOrderDate().plusDays(30).isBefore(LocalDateTime.now())){
            BigDecimal totalRefundAmount = order.getOrderItems().stream()
                    .map(item -> item.getProduct().isRefundable() ?
                            item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())) :
                            BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            RefundRequest refundRequest = RefundRequest.builder()
                    .order(order)
                    .user(order.getUser())
                    .refundAmount(totalRefundAmount)
                    .status(RefundStatus.PENDING)
                    .build();

            refundRequestRepository.save(refundRequest);
            order.getRefundRequests().add(refundRequest);
            orderRepository.save(order);

            return true;
        }
        return false;
    }

    @Override
    public Page<OrderDTO> getAllRefundedOrders(Pageable pageable) {
        return orderRepository.findAllByRefundRequestsStatus(RefundStatus.APPROVED, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional
    public boolean changeRefundStatus(Long orderId, RefundStatus refundStatus, boolean returnItemToShop) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getRefundRequests().isEmpty()) {
            RefundRequest refundRequest = order.getRefundRequests().getFirst();
            refundRequest.setStatus(refundStatus);

            if(refundRequest.getStatus() == RefundStatus.APPROVED){
                paymentService.createTransaction(order.getUser(),order, BillType.REFUND);
            }
            refundRequestRepository.save(refundRequest);
            if(returnItemToShop){
                productService.returnItemsToShop(order);
            }
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean approveOrderRefund(Long orderId, boolean returnItemToShop) {
        return changeRefundStatus(orderId, RefundStatus.APPROVED,returnItemToShop);
    }

    @Override
    @Transactional
    public boolean declineOrderRefund(Long orderId){
        return changeRefundStatus(orderId, RefundStatus.DECLINED,false);
    }
    OrderDTO toDTO(Order order){
        return orderMapper.toDto(order);
    }
}
