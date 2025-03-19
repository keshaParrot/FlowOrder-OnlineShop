package github.keshaparrot.floworderonlineshop.controllers;

import github.keshaparrot.floworderonlineshop.model.dto.BillDTO;
import github.keshaparrot.floworderonlineshop.services.interfaces.IPaymentService;
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

    private final IPaymentService paymentService;

    @GetMapping("/{id}")
    public ResponseEntity<BillDTO> getTransaction(@PathVariable Long id) throws ChangeSetPersister.NotFoundException {
        BillDTO billDTO = paymentService.getTransaction(id);
        return ResponseEntity.ok(billDTO);
    }

    @GetMapping
    public ResponseEntity<Page<BillDTO>> getAllTransactions(@RequestParam Long userId, Pageable pageable) {
        Page<BillDTO> bills = paymentService.getAllTransactions(userId, pageable);
        return ResponseEntity.ok(bills);
    }
}
