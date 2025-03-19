package github.keshaparrot.floworderonlineshop.services.interfaces;

import github.keshaparrot.floworderonlineshop.model.dto.BillDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Bill;
import github.keshaparrot.floworderonlineshop.model.entity.Order;
import github.keshaparrot.floworderonlineshop.model.entity.User;
import github.keshaparrot.floworderonlineshop.model.enums.BillType;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPaymentService {

    BillDTO createTransaction(User user, Order order, BillType billType);
    BillDTO getTransaction(Long id) throws ChangeSetPersister.NotFoundException;
    Page<BillDTO> getAllTransactions(Long userId, Pageable pageable);
}
