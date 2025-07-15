package SimTeg.simulateur.BACKEND.Repository.SimTegRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.CategorieCredit;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.Simulation;

import java.util.List;

public interface SimulationRepository extends JpaRepository<Simulation, Integer> {

    List<Simulation> findByUserId(Integer id);

    @Query("SELECT s FROM Simulation s WHERE s.categorieCredit = :categorie AND s.typeEmprunteurLabel = :typeLabel AND FUNCTION('YEAR', s.dateDebut) = :annee")
    List<Simulation> findByCategorieCreditAndTypeEmprunteurLabelAndDateDebutYear(
            @Param("categorie") CategorieCredit categorie,
            @Param("typeLabel") String typeEmprunteurLabel,
            @Param("annee") int annee
    );

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM notification WHERE simulation_id = :simulationId", nativeQuery = true)
    void deleteNotificationsBySimulationId(@Param("simulationId") Integer simulationId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM simulation WHERE id = :simulationId", nativeQuery = true)
    void deleteSimulationByIdNative(@Param("simulationId") Integer simulationId);

}
