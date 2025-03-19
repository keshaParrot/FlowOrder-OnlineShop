package github.keshaparrot.floworderonlineshop.services;

import github.keshaparrot.floworderonlineshop.exceptions.OrderNotFoundException;
import github.keshaparrot.floworderonlineshop.model.dto.OrderDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Order;
import github.keshaparrot.floworderonlineshop.model.entity.OrderItem;
import github.keshaparrot.floworderonlineshop.model.entity.Product;
import github.keshaparrot.floworderonlineshop.model.entity.RefundRequest;
import github.keshaparrot.floworderonlineshop.model.entity.User;
import github.keshaparrot.floworderonlineshop.model.enums.BillType;
import github.keshaparrot.floworderonlineshop.model.enums.RefundStatus;
import github.keshaparrot.floworderonlineshop.model.mappers.OrderMapper;
import github.keshaparrot.floworderonlineshop.repositories.OrderRepository;
import github.keshaparrot.floworderonlineshop.repositories.RefundRequestRepository;
import github.keshaparrot.floworderonlineshop.services.interfaces.IPaymentService;
import github.keshaparrot.floworderonlineshop.services.interfaces.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class IRefundServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RefundRequestRepository refundRequestRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private IPaymentService paymentService;

    @Mock
    private IProductService productService;

    @InjectMocks
    private IRefundServiceImpl refundService;

    private Order order;
    private User user;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now().minusDays(40));
        order.setRefundRequests(new LinkedList<>());
        order.setOrderItems(new ArrayList<>());

        orderItem = new OrderItem();
        orderItem.setQuantity(2);
        orderItem.setPrice(BigDecimal.valueOf(50));
        Product product = new Product();
        product.setRefundable(true);
        product.setTitle("Test Product");
        orderItem.setProduct(product);

        order.getOrderItems().add(orderItem);
    }

    @Test
    void testRefundOrder_OrderNotFound() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());
        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> refundService.refundOrder(999L, "reason"));
        assertEquals("Order with id 999 not found.", exception.getMessage());
    }

    @Test
    void testRefundOrder_RefundRequestAlreadyExists() {
        RefundRequest existingRefund = RefundRequest.builder().build();
        order.getRefundRequests().add(existingRefund);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        boolean result = refundService.refundOrder(order.getId(), "reason");
        assertFalse(result);
    }

    @Test
    void testRefundOrder_OrderNotEligibleForRefund() {
        Order recentOrder = new Order();
        recentOrder.setId(101L);
        recentOrder.setUser(user);
        recentOrder.setOrderDate(LocalDateTime.now().minusDays(10));
        recentOrder.setRefundRequests(new LinkedList<>());
        recentOrder.setOrderItems(order.getOrderItems());

        when(orderRepository.findById(101L)).thenReturn(Optional.of(recentOrder));
        boolean result = refundService.refundOrder(101L, "reason");
        assertFalse(result);
    }

    @Test
    void testRefundOrder_SuccessfulRefund() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(refundRequestRepository.save(any(RefundRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = refundService.refundOrder(order.getId(), "reason");
        assertTrue(result);
        assertFalse(order.getRefundRequests().isEmpty());
        RefundRequest refundRequest = ((LinkedList<RefundRequest>) order.getRefundRequests()).getFirst();
        assertEquals(RefundStatus.PENDING, refundRequest.getStatus());
        assertEquals(BigDecimal.valueOf(100), refundRequest.getRefundAmount());
    }

    @Test
    void testGetAllRefundedOrders() {
        OrderDTO dto = new OrderDTO();
        dto.setId(100L);
        Page<Order> orderPage = new PageImpl<>(List.of(order));
        when(orderRepository.findAllByRefundRequestsStatus(eq(RefundStatus.APPROVED), any(Pageable.class)))
                .thenReturn(orderPage);
        when(orderMapper.toDto(any(Order.class))).thenReturn(dto);

        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderDTO> result = refundService.getAllRefundedOrders(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(100L, result.getContent().get(0).getId());
    }

    @Test
    void testChangeRefundStatus_OrderNotFound() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());
        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> refundService.changeRefundStatus(999L, RefundStatus.APPROVED, true));
        assertEquals("Order with id 999 not found.", exception.getMessage());
    }

    @Test
    void testChangeRefundStatus_NoRefundRequest() {
        order.setRefundRequests(new LinkedList<>());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        boolean result = refundService.changeRefundStatus(order.getId(), RefundStatus.APPROVED, true);
        assertFalse(result);
    }

    @Test
    void testChangeRefundStatus_ApprovedWithReturnItems() {
        RefundRequest refundRequest = RefundRequest.builder()
                .status(RefundStatus.PENDING)
                .build();
        LinkedList<RefundRequest> refundRequests = new LinkedList<>();
        refundRequests.add(refundRequest);
        order.setRefundRequests(refundRequests);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(refundRequestRepository.save(any(RefundRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = refundService.changeRefundStatus(order.getId(), RefundStatus.APPROVED, true);
        assertTrue(result);
        assertEquals(RefundStatus.APPROVED, refundRequest.getStatus());
        verify(paymentService, times(1))
                .createTransaction(eq(order.getUser()), eq(order), eq(BillType.REFUND));
        verify(productService, times(1))
                .returnItemsToShop(eq(order));
    }

    @Test
    void testChangeRefundStatus_DeclinedWithoutReturnItems() {
        RefundRequest refundRequest = RefundRequest.builder()
                .status(RefundStatus.PENDING)
                .build();
        LinkedList<RefundRequest> refundRequests = new LinkedList<>();
        refundRequests.add(refundRequest);
        order.setRefundRequests(refundRequests);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(refundRequestRepository.save(any(RefundRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = refundService.changeRefundStatus(order.getId(), RefundStatus.DECLINED, false);
        assertTrue(result);
        assertEquals(RefundStatus.DECLINED, refundRequest.getStatus());
        verify(paymentService, never()).createTransaction(any(), any(), any());
        verify(productService, never()).returnItemsToShop(any());
    }

    @Test
    void testApproveOrderRefund() {
        RefundRequest refundRequest = RefundRequest.builder()
                .status(RefundStatus.PENDING)
                .build();
        LinkedList<RefundRequest> refundRequests = new LinkedList<>();
        refundRequests.add(refundRequest);
        order.setRefundRequests(refundRequests);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(refundRequestRepository.save(any(RefundRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = refundService.approveOrderRefund(order.getId(), true);
        assertTrue(result);
        assertEquals(RefundStatus.APPROVED, refundRequest.getStatus());
        verify(paymentService, times(1))
                .createTransaction(eq(order.getUser()), eq(order), eq(BillType.REFUND));
        verify(productService, times(1))
                .returnItemsToShop(eq(order));
    }

    @Test
    void testDeclineOrderRefund() {
        RefundRequest refundRequest = RefundRequest.builder()
                .status(RefundStatus.PENDING)
                .build();
        LinkedList<RefundRequest> refundRequests = new LinkedList<>();
        refundRequests.add(refundRequest);
        order.setRefundRequests(refundRequests);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(refundRequestRepository.save(any(RefundRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = refundService.declineOrderRefund(order.getId());
        assertTrue(result);
        assertEquals(RefundStatus.DECLINED, refundRequest.getStatus());
        verify(paymentService, never()).createTransaction(any(), any(), any());
        verify(productService, never()).returnItemsToShop(any());
    }
}
