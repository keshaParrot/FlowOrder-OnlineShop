package github.keshaparrot.floworderonlineshop.services;

import github.keshaparrot.floworderonlineshop.exceptions.OrderNotFoundException;
import github.keshaparrot.floworderonlineshop.exceptions.ProductNotFoundException;
import github.keshaparrot.floworderonlineshop.exceptions.ProductOutOfStockException;
import github.keshaparrot.floworderonlineshop.exceptions.UserNotFoundException;
import github.keshaparrot.floworderonlineshop.model.entity.*;
import github.keshaparrot.floworderonlineshop.model.enums.BillType;
import github.keshaparrot.floworderonlineshop.model.enums.OrderStatus;
import github.keshaparrot.floworderonlineshop.repositories.OrderRepository;
import github.keshaparrot.floworderonlineshop.repositories.ProductRepository;
import github.keshaparrot.floworderonlineshop.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class IPaymentServiceImplTest {

    @Mock
    private ITransactionServiceImpl transactionService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private IPaymentServiceImpl paymentService;

    private User user;
    private Order order;
    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(1L);
        product.setQuantity(10);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(Collections.singletonList(orderItem));
    }

    @Test
    void testPayOrderSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        boolean result = paymentService.payOrder(1L, 1L, "valid-blik-code");

        assertTrue(result);
        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(productRepository).save(product);
        verify(transactionService).createTransaction(any(User.class), any(Order.class), eq(BillType.BUYING));
    }

    @Test
    void testPayOrderWrongUser() {
        User otherUser = new User();
        otherUser.setId(2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(UserNotFoundException.class, () -> paymentService.payOrder(2L, 1L, "valid-blik-code"));
    }

    @Test
    void testPayOrderOrderNotPending() {
        order.setStatus(OrderStatus.PAID);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        boolean result = paymentService.payOrder(1L, 1L, "valid-blik-code");

        assertFalse(result);
    }

    @Test
    void testPayOrderProductOutOfStock() {
        Product outOfStockProduct = new Product();
        outOfStockProduct.setId(1L);
        outOfStockProduct.setQuantity(0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.of(outOfStockProduct));

        assertThrows(ProductOutOfStockException.class, () -> paymentService.payOrder(1L, 1L, "valid-blik-code"));
    }

    @Test
    void testPayOrderOrderNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = paymentService.payOrder(1L, 1L, "valid-blik-code");

        assertFalse(result);
    }

    @Test
    void testPayOrderUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> paymentService.payOrder(1L, 1L, "valid-blik-code"));
    }
}
