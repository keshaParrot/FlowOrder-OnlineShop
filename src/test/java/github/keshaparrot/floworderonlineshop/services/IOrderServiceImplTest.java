package github.keshaparrot.floworderonlineshop.services;

import github.keshaparrot.floworderonlineshop.exceptions.OrderNotFoundException;
import github.keshaparrot.floworderonlineshop.exceptions.UserAddressNotExistException;
import github.keshaparrot.floworderonlineshop.model.dto.OrderDTO;
import github.keshaparrot.floworderonlineshop.model.dto.OrderItemDTO;
import github.keshaparrot.floworderonlineshop.model.dto.ProductDTO;
import github.keshaparrot.floworderonlineshop.model.entity.*;
import github.keshaparrot.floworderonlineshop.model.enums.OrderStatus;
import github.keshaparrot.floworderonlineshop.model.mappers.OrderMapper;
import github.keshaparrot.floworderonlineshop.model.mappers.ProductMapper;
import github.keshaparrot.floworderonlineshop.repositories.OrderRepository;
import github.keshaparrot.floworderonlineshop.repositories.ProductRepository;
import github.keshaparrot.floworderonlineshop.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.matchers.NotNull;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IOrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private IOrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        lenient().when(orderMapper.toDto(any(Order.class))).thenAnswer(invocation -> {
            Order lOrder = invocation.getArgument(0);
            return OrderDTO.builder()
                    .id(lOrder.getId())
                    .userId(lOrder.getUser()==null?null:lOrder.getUser().getId())
                    .orderItems(lOrder.getOrderItems() != null ?
                            lOrder.getOrderItems().stream()
                                    .map(this::OrderItemToDto)
                                    .collect(Collectors.toList())
                            : Collections.emptyList())
                    .orderDate(lOrder.getOrderDate())
                    .status(lOrder.getStatus())
                    .build();
        });

        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(invocation-> invocation.getArgument(0));
        lenient().when(productRepository.save(any(Product.class))).thenAnswer(invocation-> invocation.getArgument(0));
    }

    private OrderItemDTO OrderItemToDto(OrderItem orderItem){
        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProductName())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();
    }

    @Test
    void makeOrder_Success() throws ChangeSetPersister.NotFoundException {
        User user = User.builder()
                .id(1L)
                .address(Address.builder()
                        .city("Warsaw")
                        .country("Poland")
                        .street("Biala 36")
                        .postalCode("23-556")
                        .build())
                .build();

        Product product1 = Product.builder().id(1L).quantity(20).build();
        Product product2 = Product.builder().id(2L).quantity(10).build();
        Product product3 = Product.builder().id(3L).quantity(5).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return Stream.of(product1, product2, product3)
                    .filter(o -> Objects.equals(o.getId(), id))
                    .findFirst();
        });


        boolean result = orderService.makeOrder(1L, Map.of(1L,10,2L,5,3L,5));

        verify(orderRepository).save(any(Order.class));
        assertTrue(result);
    }

    @Test
    void makeOrder_ProductOutOfStock() throws ChangeSetPersister.NotFoundException {
        User user = User.builder()
                .id(1L)
                .address(Address.builder()
                        .city("Warsaw")
                        .country("Poland")
                        .street("Biala 36")
                        .postalCode("23-556")
                        .build())
                .build();

        Product product1 = Product.builder().id(1L).quantity(20).build();
        Product product2 = Product.builder().id(2L).quantity(10).build();
        Product product3 = Product.builder().id(3L).quantity(5).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return Stream.of(product1, product2, product3)
                    .filter(o -> Objects.equals(o.getId(), id))
                    .findFirst();
        });

        boolean result = orderService.makeOrder(1L, Map.of(1L,60,2L,60,3L,60));

        assertFalse(result);
    }

    @Test
    void makeOrder_NotFoundAddress(){
        User user = User.builder()
                .id(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(UserAddressNotExistException.class, () -> {
            orderService.makeOrder(1L, Map.of(1L,10,2L,5,3L,5));
        });
    }

    @Test
    void addItemToOrder_Success_WhenStatusPending() {
        Order order = Order.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .build())
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now().minusMinutes(40))
                .orderItems(new ArrayList<>(List.of(
                        OrderItem.builder()
                                .id(1L)
                                .quantity(5)
                                .product(
                                        Product.builder()
                                                .id(1L)
                                                .quantity(10)
                                                .build()
                        ).build()
                ))).build();

        Product product = Product.builder().id(1L).quantity(20).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        boolean result = orderService.addItemToOrder(1L,Map.of(1L,10));

        assertTrue(result);
    }

    @Test
    void addItemToOrder_Success_WhenStatusPaid() {
        Order order = Order.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .build())
                .status(OrderStatus.PAID)
                .orderDate(LocalDateTime.now().minusMinutes(40))
                .orderItems(new ArrayList<>(List.of(
                        OrderItem.builder()
                                .id(1L)
                                .quantity(5)
                                .product(
                                        Product.builder()
                                                .id(1L)
                                                .quantity(10)
                                                .build()
                                ).build()
                ))).build();

        Product product = Product.builder().id(1L).quantity(20).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        boolean result = orderService.addItemToOrder(1L,Map.of(1L,10));

        assertTrue(result);
        assertEquals(2, order.getOrderItems().size());

    }

    @Test
    void addItemToOrder_Failure_WhenStatusPaid() {
        Order order = Order.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .build())
                .status(OrderStatus.PAID)
                .orderDate(LocalDateTime.now().minusHours(3))
                .orderItems(List.of(
                        OrderItem.builder()
                                .id(1L)
                                .quantity(5)
                                .product(
                                        Product.builder()
                                                .id(1L)
                                                .quantity(10)
                                                .build()
                                ).build()
                )).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        boolean result = orderService.addItemToOrder(1L,Map.of(1L,10));

        assertFalse(result);
    }

    @Test
    void addItemToOrder_Failure_WhenStatusShipped() {
        Order order = Order.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .build())
                .status(OrderStatus.SHIPPED)
                .orderDate(LocalDateTime.now().minusHours(3))
                .orderItems(List.of(
                        OrderItem.builder()
                                .id(1L)
                                .quantity(5)
                                .product(
                                        Product.builder()
                                                .id(1L)
                                                .quantity(10)
                                                .build()
                                ).build()
                )).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        boolean result = orderService.addItemToOrder(1L,Map.of(1L,10));

        assertFalse(result);
    }



    @Test
    void removeItemFromOrder_Success() {
        Order order = Order.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .build())
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now().minusHours(3))
                .orderItems(new ArrayList<>(List.of(
                        OrderItem.builder()
                                .id(1L)
                                .quantity(6)
                                .product(
                                        Product.builder()
                                                .id(1L)
                                                .quantity(10)
                                                .build()
                                ).build(),
                        OrderItem.builder()
                                .id(2L)
                                .quantity(6)
                                .product(
                                        Product.builder()
                                                .id(2L)
                                                .quantity(10)
                                                .build()
                                ).build()
                ))).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        boolean result = orderService.removeItemFromOrder(1L,new Long[]{1L});

        assertTrue(result);
        assertEquals(1, order.getOrderItems().size());
    }

    @Test
    void removeItemFromOrder_Success_OrderDeleted() {
        Order order = Order.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .build())
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now().minusHours(3))
                .orderItems(new ArrayList<>(List.of(
                        OrderItem.builder()
                                .id(1L)
                                .quantity(6)
                                .product(
                                        Product.builder()
                                                .id(2L)
                                                .quantity(10)
                                                .build()
                                ).build()
                ))).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        boolean result = orderService.removeItemFromOrder(1L,new Long[]{1L});

        assertTrue(result);
        verify(orderRepository).delete(order);
    }

    @Test
    void removeItemFromOrder_Failure_orderShipped() {
        Product product = Product.builder()
                .id(1L)
                .quantity(10)
                .build();

        Order order = Order.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .build())
                .status(OrderStatus.SHIPPED)
                .orderDate(LocalDateTime.now().minusHours(3))
                .orderItems(List.of(
                        OrderItem.builder()
                                .id(2L)
                                .quantity(6)
                                .product(product).build()
                        ,
                        OrderItem.builder()
                                .id(2L)
                                .quantity(6)
                                .product(
                                        Product.builder()
                                                .id(2L)
                                                .quantity(10)
                                                .build()
                                ).build()
                )).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        boolean result = orderService.removeItemFromOrder(1L,new Long[]{1L});

        assertFalse(result);
    }

    @Test
    void ChangeItemQuantities_Success() {
        Product product = Product.builder()
                .id(1L)
                .quantity(10)
                .build();

        Order order = Order.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .build())
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now().minusHours(3))
                .orderItems(List.of(
                        OrderItem.builder()
                                .id(1L)
                                .quantity(6)
                                .product(product).build()
                )).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        boolean result = orderService.changeItemQuantities(1L,Map.of(1L,5));

        assertTrue(result);
        assertEquals(5, order.getOrderItems().getFirst().getQuantity());
        verify(orderRepository).save(order);
        verify(productRepository).save(product);
    }

    @Test
    void ChangeItemQuantities_Failure_orderShipped() {
        Product product = Product.builder()
                .id(1L)
                .quantity(10)
                .build();

        Order order = Order.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .build())
                .status(OrderStatus.SHIPPED)
                .orderDate(LocalDateTime.now().minusHours(3))
                .orderItems(List.of(
                        OrderItem.builder()
                                .id(1L)
                                .quantity(6)
                                .product(product).build()
                )).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        boolean result = orderService.changeItemQuantities(1L,Map.of(1L,5));

        assertFalse(result);
    }

    @Test
    void ChangeItemQuantities_Failure_ProductOutOfStock() {
        Product product = Product.builder()
                .id(1L)
                .quantity(10)
                .build();

        Order order = Order.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .build())
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now().minusHours(3))
                .orderItems(new ArrayList<>(List.of(
                        OrderItem.builder()
                                .id(1L)
                                .quantity(6)
                                .product(product).build()
                ))).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        boolean result = orderService.changeItemQuantities(1L,Map.of(1L,50));

        assertFalse(result);
        assertEquals(6, order.getOrderItems().getFirst().getQuantity());
    }

    @Test
    void cancelOrder_Success() {
        Order order = Order.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .build())
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now().minusHours(3))
                .orderItems(List.of(
                        OrderItem.builder()
                                .id(1L)
                                .quantity(6)
                                .product(
                                        Product.builder()
                                                .id(2L)
                                                .quantity(10)
                                                .build()
                                ).build()
                )).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        boolean result = orderService.cancelOrder(1L,1L);

        assertTrue(result);
        verify(orderRepository).delete(order);
    }

    @Test
    void cancelOrder_Failure() {
        Order order = Order.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .build())
                .status(OrderStatus.SHIPPED)
                .orderDate(LocalDateTime.now().minusHours(3))
                .orderItems(List.of(
                        OrderItem.builder()
                                .id(1L)
                                .quantity(6)
                                .product(
                                        Product.builder()
                                                .id(2L)
                                                .quantity(10)
                                                .build()
                                ).build()
                )).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        boolean result = orderService.cancelOrder(1L,1L);

        assertFalse(result);
    }

    @Test
    void cancelOrder_Failure_goneMoreThanThoHours() {
        
    }

    @Test
    void getById_Success() throws ChangeSetPersister.NotFoundException {
        Order order = Order.builder().id(1L).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDTO queriedOrder = orderService.getById(1L);

        assertNotNull(queriedOrder);
        assert(queriedOrder.getId() == 1L);
    }

    @Test
    void getById_NotFoundUser() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> {
            orderService.getById(1L);
        });

    }

    @Test
    void getAllOrdersByUserId() {
        Order order = Order.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .build())
                .build();
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(orderRepository.findAllByUser_Id(1L,pageRequest)).thenReturn(new PageImpl<>(List.of(order)));

        Page<OrderDTO> queriedOrders = orderService.getAllOrdersByUserId(1L,pageRequest);

        assertNotNull(queriedOrders);
        assert(queriedOrders.getTotalElements() != 0);
    }

    @Test
    void getAllOrdersReadyShip() {
        Order order = Order.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .build())
                .orderItems(
                        List.of(
                                OrderItem.builder()
                                        .id(1L)
                                        .product(
                                                Product.builder()
                                                        .id(2L)
                                                        .title("ABC")
                                                        .build()
                                        )
                                        .build()
                        )
                )
                .build();
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(orderRepository.findAllByStatus(OrderStatus.PAID, pageRequest)).thenReturn(new PageImpl<>(List.of(order)));

        Page<OrderDTO> queriedOrders = orderService.getAllOrdersReadyShip(pageRequest);

        assertNotNull(queriedOrders);
        assert(queriedOrders.getTotalElements() != 0);
    }

    @Test
    void getAll() {
        Order order = Order.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .build())
                .orderItems(
                        List.of(
                                OrderItem.builder()
                                        .product(
                                                Product.builder()
                                                        .id(2L)
                                                        .title("ABC")
                                                        .build()
                                        )
                                        .id(1L)
                                        .build()

                    )
                )
                .build();
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(orderRepository.findAll(pageRequest)).thenReturn(new PageImpl<>(List.of(order)));

        Page<OrderDTO> queriedOrders = orderService.getAll(pageRequest);

        assertNotNull(queriedOrders);
        assert(queriedOrders.getTotalElements() != 0);
    }

    @Test
    void changeOrderStatus() {
        OrderStatus newStatus = OrderStatus.PAID;
        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.PENDING)
                .user(User.builder()
                        .id(1L)
                        .build())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        boolean result = orderService.changeOrderStatus(1L,newStatus);

        assertTrue(result);
    }
}