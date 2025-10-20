package anbd.he191271.repository;

import anbd.he191271.entity.Manager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ManagerRepository  extends JpaRepository<Manager, Integer> {
    Optional<Manager> findByUsername(String username);
    Optional<Manager> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Query("""
    SELECT m FROM Manager m
    WHERE (:username = '' OR :username IS NULL OR LOWER(m.username) LIKE LOWER(CONCAT('%', :username, '%')))
      AND (:email = '' OR :email IS NULL OR LOWER(m.email) LIKE LOWER(CONCAT('%', :email, '%')))
      AND (:status = '' OR :status IS NULL OR m.status = :status)
""")
    Page<Manager> searchManagers(@Param("username") String username,
                                 @Param("email") String email,
                                 @Param("status") String status,
                                 Pageable pageable);

}
