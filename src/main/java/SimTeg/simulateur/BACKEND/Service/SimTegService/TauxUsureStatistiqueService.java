package SimTeg.simulateur.BACKEND.Service.SimTegService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.TypeEprunteur;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.TauxUsure;
import SimTeg.simulateur.BACKEND.Repository.SimTegRepository.TauxUsureRepository;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TauxUsureStatistiqueService {
    @Autowired
    private TauxUsureRepository tauxUsureRepository;

    public Map<TypeEprunteur, Map<String, List<TauxUsureStatistiqueDto>>> getStatistiquesTousTypesEmprunteur() {
        List<TauxUsure> tauxUsureList = tauxUsureRepository.findAll();

        return Arrays.stream(TypeEprunteur.values())
                .collect(Collectors.toMap(
                        type -> type,
                        type -> tauxUsureList.stream()
                                .filter(t -> t.getTypeEmprunteur() == type)
                                .collect(Collectors.groupingBy(
                                        t -> t.getCategorieCredit().getNomCategorie(),
                                        Collectors.collectingAndThen(
                                                Collectors.toList(),
                                                list -> list.stream()
                                                        .sorted(Comparator.comparingDouble(TauxUsure::getAnnee))
                                                        .map(tu -> new TauxUsureStatistiqueDto(tu.getAnnee(), tu.getTauxUsure()))
                                                        .collect(Collectors.toList())
                                        )
                                ))
                ));
    }

    public record TauxUsureStatistiqueDto(double annee, double tauxUsure) {
    }
}
