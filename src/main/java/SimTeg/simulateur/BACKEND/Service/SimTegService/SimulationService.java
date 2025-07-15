package SimTeg.simulateur.BACKEND.Service.SimTegService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import SimTeg.simulateur.BACKEND.Dto.DataType.Assurance;
import SimTeg.simulateur.BACKEND.Dto.DataType.Frais;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.Frequence;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.DonneesPret;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationDtoRequest;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationDtoResponse;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationRequestGlobal;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.CategorieCredit;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.Simulation;
import SimTeg.simulateur.BACKEND.Repository.SimTegRepository.CategorieCreditRepository;
import SimTeg.simulateur.BACKEND.Repository.SimTegRepository.SimulationRepository;
import SimTeg.simulateur.BACKEND.Repository.UserRepository;
import SimTeg.simulateur.BACKEND.User.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SimulationService {
    private static final Logger logger = LoggerFactory.getLogger(SimulationService.class);

    @Autowired
    private CalculTEGService calculerTEG;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private SimulationRepository simulationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategorieCreditRepository categorieCreditRepository;

    public SimulationDtoResponse doSimulation(SimulationDtoRequest simulationDtoRequest) {
        logger.debug("Starting simulation for category ID: {}", simulationDtoRequest.getCategorieCreditId());
        CategorieCredit categorie = categorieCreditRepository.findById(simulationDtoRequest.getCategorieCreditId())
                .orElseThrow(() -> {
                    logger.error("Category not found: {}", simulationDtoRequest.getCategorieCreditId());
                    return new RuntimeException("Catégorie de crédit introuvable");
                });
        List<String> fraisObligatoires = categorie.getFraisObligatoire();
        List<String> assurancesObligatoires = categorie.getAssurances();

        simulationDtoRequest.getFraisList().forEach(f -> {
            if (!fraisObligatoires.contains(f.getNom())) {
                logger.error("Invalid frais: {} for category ID: {}", f.getNom(), simulationDtoRequest.getCategorieCreditId());
                throw new RuntimeException("Frais non valide pour cette catégorie : " + f.getNom());
            }
        });
        simulationDtoRequest.getAssuranceList().forEach(a -> {
            if (!assurancesObligatoires.contains(a.getNom())) {
                logger.error("Invalid assurance: {} for category ID: {}", a.getNom(), simulationDtoRequest.getCategorieCreditId());
                throw new RuntimeException("Assurance non valide pour cette catégorie : " + a.getNom());
            }
        });
        double totalFrais = simulationDtoRequest.getFraisList().stream().mapToDouble(Frais::getMontant).sum();
        double montantAssuranceParEcheance = simulationDtoRequest.getAssuranceList().stream().mapToDouble(Assurance::getMontant).sum();
        DonneesPret donneesPret = new DonneesPret();
        donneesPret.setDuree(simulationDtoRequest.getDuree());
        donneesPret.setMontantEmprunte(simulationDtoRequest.getMontantEmprunte());
        donneesPret.setFrais(totalFrais);
        donneesPret.setFrequence(simulationDtoRequest.getFrequence());
        donneesPret.setTauxInteretNominal(simulationDtoRequest.getTauxInteretNominal());
        donneesPret.setAssurance(montantAssuranceParEcheance);
        donneesPret.setDeblocages(simulationDtoRequest.getTableauDeblocages());
        donneesPret.setDatePremiereEcheance(simulationDtoRequest.getDatePremiereEcheance());
        SimulationDtoResponse response = calculerTEG.calculerTEG(donneesPret);
        logger.debug("Simulation completed with TEG: {}", response.getTegAnnuel());
        return response;
    }

    @Transactional
    public Simulation EnregistrerSimulation(SimulationRequestGlobal simulationRequestGlobal, String email) {
        logger.debug("Saving simulation for user: {}", email);
        if (simulationRequestGlobal == null || simulationRequestGlobal.getRequest() == null || simulationRequestGlobal.getResponse() == null) {
            logger.error("Incomplete simulation data");
            throw new IllegalArgumentException("Données de simulation incomplètes.");
        }

        SimulationDtoRequest simulationDtoRequest = simulationRequestGlobal.getRequest();
        SimulationDtoResponse simulationDtoResponse = simulationRequestGlobal.getResponse();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", email);
                    return new RuntimeException("Utilisateur introuvable avec l'email : " + email);
                });

        CategorieCredit categorieCredit = categorieCreditRepository.findById(simulationDtoRequest.getCategorieCreditId())
                .orElseThrow(() -> {
                    logger.error("Category not found: {}", simulationDtoRequest.getCategorieCreditId());
                    return new RuntimeException("Catégorie de crédit introuvable");
                });

        Simulation simulation = new Simulation();
        simulation.setUser(user);
        simulation.setCategorieCredit(categorieCredit);
        simulation.setTableauDeblocages(simulationDtoRequest.getTableauDeblocages());
        simulation.setDatePremiereEcheance(simulationDtoRequest.getDatePremiereEcheance());
        simulation.setDateDebut(simulationDtoRequest.getDatePremiereEcheance());
        simulation.setTauxNominal(simulationDtoRequest.getTauxInteretNominal());
        simulation.setMontant(simulationDtoRequest.getMontantEmprunte());
        simulation.setFrequence(simulationDtoRequest.getFrequence());
        simulation.setFraisJson(simulationDtoRequest.getFraisList());
        simulation.setDuree(simulationDtoRequest.getDuree());
        simulation.setTypeEmprunteur(simulationDtoRequest.getTypeEprunteur());
        simulation.setAssuranceJson(simulationDtoRequest.getAssuranceList());
        simulation.setTeg(simulationDtoResponse.getTegAnnuel());
        simulation.setEcheance(simulationDtoResponse.getEcheance());
        simulation.setTableauAmortisement(simulationDtoResponse.getTableauAmortissement());
        simulation.setCoutTotal(simulationDtoResponse.getCoutTotal());

        try {
            Simulation saved = simulationRepository.save(simulation);
            logger.debug("Simulation saved with ID: {}", saved.getId());
            notificationService.verifierEtNotifier(saved);
            logger.debug("Notification processed for simulation ID: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            logger.error("Error saving simulation for user: {}", email, e);
            throw new RuntimeException("Erreur lors de l'enregistrement de la simulation : " + e.getMessage(), e);
        }
    }

    @Transactional
    public boolean deleteSimulation(Integer id) {
        logger.debug("Deleting simulation ID: {}", id);
        Optional<Simulation> simulationOpt = simulationRepository.findById(id);
        if (simulationOpt.isPresent()) {
            Simulation simul = simulationOpt.get();

            // Vide les listes ElementCollection avant suppression pour éviter erreurs SQL
            if (simul.getTableauAmortisement() != null) {
                simul.getTableauAmortisement().clear();
            }
            if (simul.getTableauDeblocages() != null) {
                simul.getTableauDeblocages().clear();
            }
            if (simul.getFraisJson() != null) {
                simul.getFraisJson().clear();
            }
            if (simul.getAssuranceJson() != null) {
                simul.getAssuranceJson().clear();
            }

            // Supprime la simulation : cascade supprimera aussi les notifications liées
            simulationRepository.delete(simul);

            logger.debug("Simulation ID: {} deleted successfully", id);
            return true;
        }
        logger.warn("Simulation ID: {} not found", id);
        return false;
    }

    @Transactional
    public Simulation UptdateSimulation(SimulationDtoRequest simulationDtoRequest, Integer id) {
        logger.debug("Updating simulation ID: {}", id);
        Optional<Simulation> simulatio = simulationRepository.findById(id);
        if (simulatio.isPresent()) {
            SimulationDtoResponse simulationDtoResponse = doSimulation(simulationDtoRequest);
            Simulation simulation = simulatio.get();
            simulation.setTypeEmprunteur(simulationDtoRequest.getTypeEprunteur());
            simulation.setMontant(simulationDtoRequest.getMontantEmprunte());
            simulation.setDuree(simulationDtoRequest.getDuree());
            simulation.setFrequence(simulationDtoRequest.getFrequence());
            simulation.setTauxNominal(simulationDtoRequest.getTauxInteretNominal());
            simulation.setFraisJson(simulationDtoRequest.getFraisList());
            simulation.setAssuranceJson(simulationDtoRequest.getAssuranceList());
            simulation.setTeg(simulationDtoResponse.getTegAnnuel());
            simulation.setEcheance(simulationDtoResponse.getEcheance());
            simulation.setTableauAmortisement(simulationDtoResponse.getTableauAmortissement());
            simulation.setDateDebut(simulationDtoRequest.getDatePremiereEcheance());

            try {
                Simulation simulation1 = simulationRepository.save(simulation);
                logger.debug("Simulation ID: {} updated successfully", id);
                notificationService.verifierEtNotifier(simulation1);
                logger.debug("Notification processed for updated simulation ID: {}", id);
                return simulation1;
            } catch (Exception e) {
                logger.error("Error updating simulation ID: {}", id, e);
                throw new RuntimeException("Erreur lors de la mise à jour de la simulation : " + e.getMessage(), e);
            }
        }
        logger.warn("Simulation ID: {} not found", id);
        return null;
    }

    @Transactional
    public List<Simulation> simulationParUserID(Integer id) {
        logger.debug("Fetching simulations for user ID: {}", id);
        if (userRepository.existsById(id)) {
            List<Simulation> simulations = simulationRepository.findByUserId(id);
            logger.debug("Found {} simulations for user ID: {}", simulations.size(), id);
            return simulations;
        }
        logger.warn("User ID: {} not found", id);
        return null;
    }

    public List<Simulation> getAllSimulationUserByCategorieId(Integer categorieId, Integer userId) {
        logger.debug("Fetching simulations for user ID: {} and category ID: {}", userId, categorieId);
        List<Simulation> simulations = simulationParUserID(userId);
        if (simulations == null || simulations.isEmpty()) {
            logger.warn("No simulations found for user ID: {}", userId);
            return new ArrayList<>(); // Return an empty list instead of null
        }
        List<Simulation> simulationResult = new ArrayList<>();
        for (Simulation simulation : simulations) {
            CategorieCredit categorieCredit = simulation.getCategorieCredit();
            if (categorieCredit != null && categorieCredit.getId().equals(categorieId)) {
                simulationResult.add(simulation);
            }
        }
        logger.debug("Found {} simulations for user ID: {} and category ID: {}", simulationResult.size(), userId, categorieId);
        return simulationResult; // Return the filtered list, which may be empty
    }
}