package github.keshaparrot.floworderonlineshop.repositories;

import github.keshaparrot.floworderonlineshop.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findAll(Specification<Product> specification, Pageable pageable);

    default Page<Product> sort(String category, BigDecimal priceFrom, BigDecimal priceTo, String searchQuery, Pageable pageable) {
        Specification<Product> specification = Specification.where(categorySpec(category))
                .and(priceRangeSpec(priceFrom, priceTo))
                .and(searchQuerySpec(searchQuery));

        return findAll(specification, pageable);
    }

    private Specification<Product> categorySpec(String category) {
        return (root, query, criteriaBuilder) -> {
            if (category != null && !category.isEmpty()) {
                return criteriaBuilder.equal(root.get("category"), category);
            }
            return criteriaBuilder.conjunction();
        };
    }

    private Specification<Product> priceRangeSpec(BigDecimal priceFrom, BigDecimal priceTo) {
        return (root, query, criteriaBuilder) -> {
            if (priceFrom != null && priceTo != null) {
                return criteriaBuilder.between(root.get("price"), priceFrom, priceTo);
            } else if (priceFrom != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), priceFrom);
            } else if (priceTo != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("price"), priceTo);
            }
            return criteriaBuilder.conjunction();
        };
    }

    private Specification<Product> searchQuerySpec(String searchQuery) {
        return (root, query, criteriaBuilder) -> {
            if (searchQuery != null && !searchQuery.isEmpty()) {
                String searchPattern = "%" + searchQuery.toLowerCase() + "%";
                return criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)
                );
            }
            return criteriaBuilder.conjunction();
        };
    }

    boolean deleteProductById(Long id);
}
