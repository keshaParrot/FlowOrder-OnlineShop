package github.keshaparrot.floworderonlineshop.repositories;

import github.keshaparrot.floworderonlineshop.model.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface BillRepository extends JpaRepository<Bill, Long> {

    Page<Bill> findAllByBuyer(Long buyer, Pageable pageable);

}
