package SimTeg.simulateur.BACKEND.Controller.SimTegController;

import SimTeg.simulateur.BACKEND.Service.SimTegService.ExportSimulationService;
import SimTeg.simulateur.BACKEND.Service.SimTegService.ImportationFichierExcel;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationDtoRequest;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationDtoResponse;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationRequestGlobal;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.Simulation;
import SimTeg.simulateur.BACKEND.Service.SimTegService.SimulationService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("api/simulation")
public class SimulationController {

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private ImportationFichierExcel importationFichierExcel;

    @Autowired
    private ExportSimulationService pdfExportService;


    @PostMapping("calculer")
    public ResponseEntity<SimulationDtoResponse> calculerTeg(
            @RequestBody SimulationDtoRequest simulationDtoRequest) {
        SimulationDtoResponse response = simulationService.doSimulation(simulationDtoRequest);
        return ResponseEntity.ok(response);
    }


    // Ici, plus de email en paramètre, récupération depuis le contexte Spring Security
    @PostMapping("save")
    public ResponseEntity<Simulation> saveSimulation(
            @RequestBody SimulationRequestGlobal simulationRequestGlobal,
            Authentication authentication) {

        String email = authentication.getName();  // Récupère l'email (ou username) de l'utilisateur connecté

        Simulation savedSimulation = simulationService.EnregistrerSimulation(simulationRequestGlobal, email);

        if (savedSimulation != null) {
            return ResponseEntity.ok(savedSimulation);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


    @PutMapping("mettrejour/{idSimulation}")
    public ResponseEntity<Simulation> mettreAjour(
            @RequestBody SimulationDtoRequest simulationDtoRequest,
            @PathVariable Integer idSimulation) {

        Simulation updated = simulationService.UptdateSimulation(simulationDtoRequest, idSimulation);

        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("supprimer/{idSimulation}")
    public ResponseEntity<Boolean> supprimerSimulation(@PathVariable Integer idSimulation) {
        boolean deleted = simulationService.deleteSimulation(idSimulation);
        return ResponseEntity.ok(deleted);
    }


    @GetMapping("simulationUser/{id}")
    public ResponseEntity<List<Simulation>> listeSimulationUser(@PathVariable Integer id) {
        List<Simulation> simulations = simulationService.simulationParUserID(id);
        return ResponseEntity.ok(simulations);
    }


    @GetMapping("simulationParCategorieId/{userId}/{categorieId}")
    public ResponseEntity<List<Simulation>> listeSimulationCategorie(
            @PathVariable Integer userId,
            @PathVariable Integer categorieId) {

        List<Simulation> simulations = simulationService.getAllSimulationUserByCategorieId(categorieId, userId);
        return ResponseEntity.ok(simulations);
    }


    @PostMapping("/import-excel")
    public ResponseEntity<?> importSimulationExcel(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            SimulationDtoRequest dto = importationFichierExcel.LireSimulation(inputStream);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'importation : " + e.getMessage());
        }
    }

    @PostMapping("/export-pdf")
    public void exportSimulationAsPdf(
            @RequestBody SimulationRequestGlobal simulation,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=simulation.pdf");
        pdfExportService.exportToPdf(simulation, response.getOutputStream());
    }
}
