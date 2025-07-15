package SimTeg.simulateur.BACKEND.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import SimTeg.simulateur.BACKEND.User.Token;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token,Integer> {
    Optional<Token> findByToken(String token);
} 