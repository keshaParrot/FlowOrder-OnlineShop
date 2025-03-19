package github.keshaparrot.floworderonlineshop.repositories;

import github.keshaparrot.floworderonlineshop.model.entity.RefundRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

}
