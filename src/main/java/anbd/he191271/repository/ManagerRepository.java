package anbd.he191271.repository;

import anbd.he191271.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManagerRepository  extends JpaRepository<Manager, Integer> {
    Optional<Manager> findByUsername(String username);
    Optional<Manager> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}
