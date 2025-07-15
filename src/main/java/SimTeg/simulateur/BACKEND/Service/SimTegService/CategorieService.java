package SimTeg.simulateur.BACKEND.Service.SimTegService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.CategorieDto;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.CategorieCredit;
import SimTeg.simulateur.BACKEND.Repository.SimTegRepository.CategorieCreditRepository;
import SimTeg.simulateur.BACKEND.Dto.DataType.FrAss;

import java.util.List;
import java.util.Optional;

@Service
public class CategorieService {

    @Autowired
    private CategorieCreditRepository categorieCreditRepository;

    public CategorieCredit CreateCategorieCredit(CategorieDto categorieDto) {
        CategorieCredit categorieCredit = new CategorieCredit();
        categorieCredit.setNomCategorie(categorieDto.getNomCategorie());
        categorieCredit.setDescription(categorieDto.getDescription());
        categorieCredit.setFraisObligatoire(categorieDto.getFraisObligatoire());
        categorieCredit.setAssurances(categorieDto.getAssurances());
        return categorieCreditRepository.save(categorieCredit);
    }

    public CategorieDto GetCategorieCredit(Integer id) {
        Optional<CategorieCredit> categorieCredit = categorieCreditRepository.findById(id);
        return categorieCredit.map(this::convertToDto)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie de crédit avec ID " + id + " introuvable."));
    }

    public boolean deleteCategorieCredit(Integer id) {
        if (categorieCreditRepository.existsById(id)) {
            categorieCreditRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<CategorieCredit> listCategorie() {
        List<CategorieCredit> categories = categorieCreditRepository.findAll();
        if (categories.isEmpty()) {
            throw new RuntimeException("La liste de catégories est vide.");
        }
        return categories;
    }

    public CategorieCredit UpdateCategorieCredit(Integer id, CategorieDto categorieDto) {
        Optional<CategorieCredit> categorieCreditOptional = categorieCreditRepository.findById(id);
        if (categorieCreditOptional.isEmpty()) {
            throw new EntityNotFoundException("Catégorie de crédit avec ID " + id + " introuvable.");
        }
        CategorieCredit categorieCredit = categorieCreditOptional.get();
        categorieCredit.setNomCategorie(categorieDto.getNomCategorie());
        categorieCredit.setDescription(categorieDto.getDescription());
        categorieCredit.setFraisObligatoire(categorieDto.getFraisObligatoire());
        categorieCredit.setAssurances(categorieDto.getAssurances());
        return categorieCreditRepository.save(categorieCredit);
    }

    public CategorieDto convertToDto(CategorieCredit categorieCredit) {
        CategorieDto dto = new CategorieDto();
        dto.setId(categorieCredit.getId());
        dto.setNomCategorie(categorieCredit.getNomCategorie());
        dto.setDescription(categorieCredit.getDescription());
        dto.setFraisObligatoire(categorieCredit.getFraisObligatoire());
        dto.setAssurances(categorieCredit.getAssurances());
        return dto;
    }

    public FrAss getChampCategorie(Integer categorieId) {
        Optional<CategorieCredit> categorieTest = categorieCreditRepository.findById(categorieId);
        if (categorieTest.isEmpty()) {
            throw new EntityNotFoundException("Catégorie de crédit avec ID " + categorieId + " introuvable.");
        }

        CategorieCredit categorieCredit = categorieTest.get();

        FrAss fraisAssurance = new FrAss();
        fraisAssurance.setFrais(categorieCredit.getFraisObligatoire());
        fraisAssurance.setAssurance(categorieCredit.getAssurances());

        return fraisAssurance;
    }
}
