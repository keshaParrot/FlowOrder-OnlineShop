package github.keshaparrot.floworderonlineshop.controllers;

import github.keshaparrot.floworderonlineshop.model.dto.OrderDTO;
import github.keshaparrot.floworderonlineshop.model.dto.OrderRequestDTO;
import github.keshaparrot.floworderonlineshop.model.enums.OrderStatus;
import github.keshaparrot.floworderonlineshop.services.interfaces.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final IOrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<String> makeOrder(@RequestBody @Valid OrderRequestDTO request) throws ChangeSetPersister.NotFoundException {
        boolean result = orderService.makeOrder(request.getUserId(), request.getProductQuantities());
        return result
                ? ResponseEntity.status(HttpStatus.CREATED).body("the order was placed successfully")
                : ResponseEntity.badRequest().body("error attempting while creating the order");

    }

    @PostMapping("/{orderId}/add-items")
    public ResponseEntity<String> addItemToOrder(@PathVariable Long orderId,
                                               @RequestBody Map<Long, Integer> productQuantities) {
        boolean result = orderService.addItemToOrder(orderId, productQuantities);
        return result
                ? ResponseEntity.ok().body("the product was successfully added to the order")
                : ResponseEntity.badRequest().body("error attempting while adding the product to the order");
    }

    @PutMapping("/{orderId}/change-quantities")
    public ResponseEntity<String> changeItemQuantities(@PathVariable Long orderId,
                                                     @RequestBody Map<Long, Integer> productQuantities) {
        boolean result = orderService.changeItemQuantities(orderId, productQuantities);
        return result
                ? ResponseEntity.ok().body("the product was successfully changed to the order")
                : ResponseEntity.badRequest().body("error attempting while changing the product to the order");
    }

    @DeleteMapping("/{orderId}/remove-items")
    public ResponseEntity<String> removeItemsFromOrder(@PathVariable Long orderId,
                                                     @RequestBody Long[] productIds) {
        boolean result = orderService.removeItemFromOrder(orderId, productIds);
        return result
                ? ResponseEntity.status(HttpStatus.NO_CONTENT).body("the product was successfully removed from the order")
                : ResponseEntity.badRequest().body("error attempting while removing the product from the order");
    }

    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId,
                                            @RequestParam Long userId) {
        boolean result = orderService.cancelOrder(userId, orderId);
        return result
                ? ResponseEntity.status(HttpStatus.NO_CONTENT).body("the order was successfully cancelled")
                : ResponseEntity.badRequest().body("error attempting while cancelling the order");
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long orderId) throws ChangeSetPersister.NotFoundException {
        OrderDTO orderDTO = orderService.getById(orderId);
        return ResponseEntity.ok(orderDTO);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<OrderDTO>> getOrdersByUserId(@PathVariable Long userId, Pageable pageable) {
        Page<OrderDTO> orders = orderService.getAllOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/ready-to-ship")
    public ResponseEntity<Page<OrderDTO>> getOrdersReadyToShip(Pageable pageable) {
        Page<OrderDTO> orders = orderService.getAllOrdersReadyShip(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(Pageable pageable) {
        Page<OrderDTO> orders = orderService.getAll(pageable);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/set-status")
    public ResponseEntity<String> changeOrderStatus(@PathVariable Long orderId,
                                                  @RequestParam OrderStatus orderStatus) {
        boolean result = orderService.changeOrderStatus(orderId, orderStatus);
        return result
                ? ResponseEntity.ok().body("the order was successfully changed to the order")
                : ResponseEntity.badRequest().body("error attempting while changing the order to the order");
    }
}
