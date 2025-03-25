package github.keshaparrot.floworderonlineshop.controllers;

import github.keshaparrot.floworderonlineshop.model.dto.BillDTO;
import github.keshaparrot.floworderonlineshop.services.interfaces.IPaymentService;
import github.keshaparrot.floworderonlineshop.services.interfaces.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final ITransactionService transactionService;
    private final IPaymentService paymentService;

    @GetMapping("/transaction/get/{id}")
    public ResponseEntity<BillDTO> getTransaction(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        BillDTO billDTO = transactionService.getTransaction(id);
        return ResponseEntity.ok(billDTO);
    }

    @GetMapping("/transaction/get/all")
    public ResponseEntity<Page<BillDTO>> getAllTransactions(@RequestParam Long userId, Pageable pageable) {
        Page<BillDTO> bills = transactionService.getAllTransactions(userId, pageable);
        return ResponseEntity.ok(bills);
    }
    @PostMapping("/pay/{orderId}")
    public ResponseEntity<String> payOrder(@PathVariable Long orderId,
                                           @RequestParam Long userId,
                                           @RequestParam String blickCode) throws ChangeSetPersister.NotFoundException {
        boolean result = paymentService.payOrder(userId, orderId,blickCode);
        return result
                ? ResponseEntity.ok().body("the order was successfully paid successfully")
                : ResponseEntity.badRequest().body("error attempting while paying the order");
    }
}
