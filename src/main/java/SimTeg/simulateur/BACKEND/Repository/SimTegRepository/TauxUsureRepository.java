package SimTeg.simulateur.BACKEND.Repository.SimTegRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.TypeEprunteur;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.CategorieCredit;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.TauxUsure;

import java.util.List;
import java.util.Optional;

public interface TauxUsureRepository extends JpaRepository<TauxUsure,Integer> {
    List<TauxUsure> findByCategorieCredit(CategorieCredit categorieCredit);
    List<TauxUsure> findByCategorieCreditId(Integer id);
    Optional<TauxUsure> findByCategorieCreditAndTypeEmprunteurAndAnnee(CategorieCredit categorie, TypeEprunteur type, int annee);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM taux_usure WHERE id = :id", nativeQuery = true)
    void deleteTauxUsureById(@Param("id") Integer id);

}
