package github.keshaparrot.floworderonlineshop.services;

import github.keshaparrot.floworderonlineshop.exceptions.OrderNotFoundException;
import github.keshaparrot.floworderonlineshop.exceptions.ProductNotFoundException;
import github.keshaparrot.floworderonlineshop.exceptions.UserAddressNotExistException;
import github.keshaparrot.floworderonlineshop.exceptions.UserNotFoundException;
import github.keshaparrot.floworderonlineshop.model.dto.OrderDTO;
import github.keshaparrot.floworderonlineshop.model.entity.*;
import github.keshaparrot.floworderonlineshop.model.enums.BillType;
import github.keshaparrot.floworderonlineshop.model.enums.OrderStatus;
import github.keshaparrot.floworderonlineshop.model.mappers.OrderMapper;
import github.keshaparrot.floworderonlineshop.repositories.OrderRepository;
import github.keshaparrot.floworderonlineshop.repositories.ProductRepository;
import github.keshaparrot.floworderonlineshop.repositories.UserRepository;
import github.keshaparrot.floworderonlineshop.services.interfaces.IOrderService;
import github.keshaparrot.floworderonlineshop.services.interfaces.ITransactionService;
import lombok.AllArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class IOrderServiceImpl implements IOrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final ITransactionService paymentService;


    @Override
    @Transactional
    public boolean makeOrder(Long userId, Map<Long, Integer> productQuantities) throws ChangeSetPersister.NotFoundException {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (user.getAddress() == null) {
            throw new UserAddressNotExistException(userId);
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findById(entry.getKey()).orElseThrow(() -> new ProductNotFoundException(entry.getKey()));
            if (product == null || product.getQuantity() < entry.getValue()) {
                return false;
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setOrder(order);
            orderItem.setQuantity(entry.getValue());
            orderItem.setPrice(product.getPrice());
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);
        orderRepository.save(order);
        return true;
    }

    @Override
    @Transactional
    public boolean addItemToOrder(Long orderId, Map<Long, Integer> productQuantities) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) return false;

        Order order = optionalOrder.get();
        if (order.getStatus() == OrderStatus.SHIPPED) return false;
        if (order.getStatus() == OrderStatus.PAID && optionalOrder.get().getOrderDate().plusHours(2).isBefore(LocalDateTime.now())) {
            return false; // if the order is paid for, the customer can add products to the order only if 2 hours have not passed
        }

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findById(entry.getKey()).orElse(null);
            if (product == null || product.getQuantity() < entry.getValue()) {
                return false;
            }
            product.setQuantity(product.getQuantity() - entry.getValue());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setOrder(order);
            orderItem.setQuantity(entry.getValue());
            order.getOrderItems().add(orderItem);
        }
        orderRepository.save(order);
        return true;
    }

    @Override
    @Transactional
    public boolean changeItemQuantities(Long orderId, Map<Long, Integer> productQuantities) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) return false;

        Order order = optionalOrder.get();
        if (order.getStatus() == OrderStatus.SHIPPED) return false;
        if (order.getStatus() == OrderStatus.PAID && optionalOrder.get().getOrderDate().plusHours(2).isBefore(LocalDateTime.now())) {
            return false; // if the order is paid for, the customer can add products to the order only if 2 hours have not passed
        }

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            OrderItem orderItem = order.getOrderItems().stream()
                    .filter(item -> item.getProduct().getId().equals(entry.getKey()))
                    .findFirst()
                    .orElse(null);

            if (orderItem == null) return false;

            Product product = orderItem.getProduct();
            int newQuantity = entry.getValue();

            if (newQuantity > product.getQuantity() + orderItem.getQuantity()) {
                return false;
            }

            product.setQuantity(product.getQuantity() + orderItem.getQuantity() - newQuantity);
            orderItem.setQuantity(newQuantity);
            productRepository.save(product);
        }
        orderRepository.save(order);
        return true;
    }

    @Override
    @Transactional
    public boolean removeItemFromOrder(Long orderId, Long[] productIds) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) return false;

        Order order = optionalOrder.get();
        if (order.getStatus() == OrderStatus.SHIPPED) return false;
        if (order.getStatus() == OrderStatus.PAID && optionalOrder.get().getOrderDate().plusHours(2).isBefore(LocalDateTime.now())) {
            return false; // if the order is paid for, the customer can add products to the order only if 2 hours have not passed
        }

        order.getOrderItems().removeIf(item -> Arrays.asList(productIds).contains(item.getId()));
        if (order.getOrderItems().isEmpty()) {
            orderRepository.delete(order);
            return true;
        }
        orderRepository.save(order);
        return true;
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long userId, Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) return false;

        Order order = optionalOrder.get();
        if (order.getUser().getId().equals(userId) && order.getStatus() == OrderStatus.PENDING) {
            orderRepository.delete(order);
            return true;
        }
        return false;
    }

    @Override
    public OrderDTO getById(Long id) throws ChangeSetPersister.NotFoundException {
        Order order = orderRepository.findById(id).orElseThrow(()-> new OrderNotFoundException(id));
        return toDTO(order);
    }

    @Override
    public Page<OrderDTO> getAllOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findAllByUser_Id(userId, pageable).map(this::toDTO);
    }

    @Override
    public Page<OrderDTO> getAllOrdersReadyShip(Pageable pageable) {
        return orderRepository.findAllByStatus(OrderStatus.PAID, pageable).map(this::toDTO);
    }

    @Override
    public Page<OrderDTO> getAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    @Transactional
    public boolean changeOrderStatus(Long orderId, OrderStatus orderStatus) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) return false;

        Order order = optionalOrder.get();
        order.setStatus(orderStatus);
        orderRepository.save(order);
        return true;
    }
    OrderDTO toDTO(Order order){
        return orderMapper.toDto(order);
    }
}
