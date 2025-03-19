package github.keshaparrot.floworderonlineshop.services;

import github.keshaparrot.floworderonlineshop.model.dto.BillDTO;
import github.keshaparrot.floworderonlineshop.model.entity.*;
import github.keshaparrot.floworderonlineshop.model.enums.BillType;
import github.keshaparrot.floworderonlineshop.repositories.BillRepository;
import github.keshaparrot.floworderonlineshop.services.interfaces.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class IPaymentServiceImpl implements IPaymentService {
    private final BillRepository billRepository;
    @Value("${app.sellerName}")
    private String sellerName;
    @Value("${app.sellerAddress}")
    private String sellerAddress;

    @Override
    public BillDTO createTransaction(User user, Order order, BillType billType) {
        return toDTO(billRepository.save(
                Bill.builder()
                        .issueDate(LocalDateTime.now())
                        .seller(sellerName)
                        .buyer(user.getId())
                        .products(
                                order.getOrderItems().stream()
                                        .collect(Collectors.toMap(
                                                OrderItem::getProductName,
                                                OrderItem::getQuantity,
                                                Integer::sum
                                        ))
                        )
                        .totalAmount(
                                order.getOrderItems().stream()
                                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        )
                        .vat(BigDecimal.valueOf(23))
                        .paymentMethod("Card")
                        .status("PAID")
                        .transactionNumber(UUID.randomUUID())
                        .salesAddress(sellerAddress)
                        .Type(billType)
                        .currency("zl")
                        .build()
        ));
    }

    @Override
    public BillDTO getTransaction(Long id) throws ChangeSetPersister.NotFoundException {
        return toDTO(billRepository.findById(id).orElseThrow(ChangeSetPersister.NotFoundException::new));
    }

    @Override
    public Page<BillDTO> getAllTransactions(Long userId, Pageable pageable) {
        return billRepository.findAllByBuyer(userId,pageable).map(this::toDTO);
    }

    private BillDTO toDTO(Bill bill) {
        return null;
    }
}
