package github.keshaparrot.floworderonlineshop.controllers;

import github.keshaparrot.floworderonlineshop.model.dto.OrderDTO;
import github.keshaparrot.floworderonlineshop.services.interfaces.IRefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final IRefundService refundService;

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<String> refundOrder(@PathVariable Long orderId,
                                            @RequestParam String reason) {
        boolean result = refundService.refundOrder(orderId, reason);
        return result
                ? ResponseEntity.ok().body("refund request accepted")
                : ResponseEntity.badRequest().body("error attempting while creating refund request");
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getAllRefundedOrders(Pageable pageable) {
        Page<OrderDTO> orders = refundService.getAllRefundedOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/approve")
    public ResponseEntity<String> approveRefund(@PathVariable Long orderId,
                                              @RequestParam(defaultValue = "false") boolean returnItemToShop) {
        boolean result = refundService.approveOrderRefund(orderId, returnItemToShop);
        return result
                ? ResponseEntity.ok().body("refund request accepted")
                : ResponseEntity.badRequest().build();
    }

    @PutMapping("/{orderId}/decline")
    public ResponseEntity<String> declineRefund(@PathVariable Long orderId) {
        boolean result = refundService.declineOrderRefund(orderId);
        return result
                ? ResponseEntity.ok().body("refund request rejected")
                : ResponseEntity.badRequest().build();
    }
}

