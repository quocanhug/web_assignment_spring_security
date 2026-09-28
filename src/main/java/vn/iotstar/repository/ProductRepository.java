package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    long countByUserId(Long userId);

    long countByUser(User user);

    @Query(value = """
        SELECT p FROM Product p
        JOIN FETCH p.user
        WHERE (:kw IS NULL OR :kw = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :kw, '%')))
    """,
    countQuery = """
        SELECT COUNT(p) FROM Product p
        WHERE (:kw IS NULL OR :kw = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :kw, '%')))
    """)
    Page<Product> searchAll(@Param("kw") String keyword, Pageable pageable);

    @Query(value = """
        SELECT p FROM Product p
        JOIN FETCH p.user
        WHERE p.user.id = :userId
          AND (:kw IS NULL OR :kw = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :kw, '%')))
    """,
    countQuery = """
        SELECT COUNT(p) FROM Product p
        WHERE p.user.id = :userId
          AND (:kw IS NULL OR :kw = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :kw, '%')))
    """)
    Page<Product> searchByUserId(@Param("userId") Long userId, @Param("kw") String keyword, Pageable pageable);
}
