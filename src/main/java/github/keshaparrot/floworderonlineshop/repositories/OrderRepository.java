package github.keshaparrot.floworderonlineshop.repositories;

import github.keshaparrot.floworderonlineshop.model.entity.Order;
import github.keshaparrot.floworderonlineshop.model.entity.Product;
import github.keshaparrot.floworderonlineshop.model.enums.OrderStatus;
import github.keshaparrot.floworderonlineshop.model.enums.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    List<Order> findByStatus(OrderStatus status);
    Page<Order> findAllByUser_Id(Long userId, Pageable pageable);
    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findAllByRefundRequestsStatus(RefundStatus status, Pageable pageable);
}
