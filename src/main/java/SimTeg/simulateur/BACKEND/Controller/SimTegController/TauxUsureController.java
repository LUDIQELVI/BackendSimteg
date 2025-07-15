package SimTeg.simulateur.BACKEND.Controller.SimTegController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.TypeEprunteur;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.TauxUsureDto;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.HistoriqueTauxUsure;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.TauxUsure;
import SimTeg.simulateur.BACKEND.Service.SimTegService.TauxUsureService;
import SimTeg.simulateur.BACKEND.Service.SimTegService.TauxUsureStatistiqueService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/tauxUsure")

public class TauxUsureController {
    @Autowired
    private TauxUsureService tauxUsureService;
    @Autowired
    private TauxUsureStatistiqueService tauxUsureStatistiqueService;
    @PostMapping("creer")
    public ResponseEntity<TauxUsure> creerTauxUsure(@RequestBody TauxUsureDto tauxUsureDto) {
        try {
            TauxUsure result = tauxUsureService.CreerTauxUsure(tauxUsureDto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
@GetMapping("listeParCategorie/{idCategorie}")
    public List<TauxUsure> getListeByCategorieId(@PathVariable Integer idCategorie){
        return tauxUsureService.getTauxUsureByCategorieId(idCategorie);
}
@PutMapping("update/{id}")
    public TauxUsure updateTauxUsure(@PathVariable Integer id, @io.swagger.v3.oas.annotations.parameters.RequestBody @RequestBody TauxUsureDto tauxUsureDto){
        return tauxUsureService.updateTauxUsure(id,tauxUsureDto);
}
@DeleteMapping("delete/{id}")
    public boolean deleteTauxUsure(@PathVariable Integer id){
        return tauxUsureService.deleteTauxUsure(id);
}
@GetMapping("listeTaux")
    public List<TauxUsure> listTauxUsure(){  return tauxUsureService.listTauxUsure();}
    @GetMapping("donneStatistique")
    public Map<TypeEprunteur, Map<String, List<TauxUsureStatistiqueService.TauxUsureStatistiqueDto>>> donneeStatistique(){
        return tauxUsureStatistiqueService.getStatistiquesTousTypesEmprunteur();
    }
@GetMapping("historiqueUpdate/{idTauxUsure}")
    public List<HistoriqueTauxUsure> HistoriqueMiseAJour(@PathVariable Integer idTauxUsure){return tauxUsureService.listHistorique(idTauxUsure);}

    }
