package SimTeg.simulateur.BACKEND.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import SimTeg.simulateur.BACKEND.User.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String role);
}
