package SimTeg.simulateur.BACKEND.Repository.SimTegRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.Notification;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.Simulation;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    Optional<Notification> findBySimulation(Simulation simulation);

    List<Notification> findBySimulationId(Integer id);
}