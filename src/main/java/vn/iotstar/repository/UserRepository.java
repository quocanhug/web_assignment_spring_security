package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
        SELECT u FROM User u
        JOIN FETCH u.role
        WHERE LOWER(u.email) = LOWER(:login) OR LOWER(u.username) = LOWER(:login)
    """)
    Optional<User> findByUsernameOrEmailWithRole(@Param("login") String login);

    @Query("""
        SELECT u FROM User u
        JOIN FETCH u.role
        WHERE LOWER(u.email) = LOWER(:email)
    """)
    Optional<User> findByEmailWithRole(@Param("email") String email);

    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
            String username, String email, String fullName, Pageable pageable
    );
}
