package github.keshaparrot.floworderonlineshop.services.interfaces;

import github.keshaparrot.floworderonlineshop.model.dto.OrderDTO;
import github.keshaparrot.floworderonlineshop.model.enums.OrderStatus;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface IOrderService {


    boolean makeOrder(Long userId, Map<Long, Integer> productQuantities) throws ChangeSetPersister.NotFoundException;

    boolean addItemToOrder(Long orderId, Map<Long, Integer> productQuantities);

    boolean changeItemQuantities(Long orderId, Map<Long, Integer> productQuantities);

    boolean removeItemFromOrder(Long orderId, Long[] productIds);

    boolean cancelOrder(Long userId, Long orderId);

    OrderDTO getById(Long Id) throws ChangeSetPersister.NotFoundException;
    Page<OrderDTO> getAllOrdersByUserId(Long userId, Pageable pageable);
    Page<OrderDTO> getAllOrdersReadyShip(Pageable pageable);
    Page<OrderDTO> getAll(Pageable pageable);

    boolean changeOrderStatus(Long orderId, OrderStatus orderStatus);
}
