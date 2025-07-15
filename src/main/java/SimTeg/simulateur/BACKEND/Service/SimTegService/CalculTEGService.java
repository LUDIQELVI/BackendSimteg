package SimTeg.simulateur.BACKEND.Service.SimTegService;

import SimTeg.simulateur.BACKEND.Dto.DataType.AmortisementLigne;
import SimTeg.simulateur.BACKEND.Dto.DataType.DeblocageLigne;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.Frequence;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.DonneesPret;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationDtoResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

@Service
public class CalculTEGService {
    // bon JE VAIS Aller doucement avec des variables  logique comme dans la formule et de commentaire cohérent.
// Variable pour le Debogage
    private static final Logger LOGGER = Logger.getLogger(CalculTEGService.class.getName());
    //Tolérance pour que le résultat  se rapproche le plus possible de ZERO pour les calculs
    private static final double TOLERANCE_RELATIVE = 1e-10; //
    private static final int MAX_ITERATIONS = 100; // Nombre raisonnable d'itérations pour l'instant ça pourrais increase jusqu'a 300

    public SimulationDtoResponse calculerTEG(DonneesPret donnees) {
        // Validation des données d'entrée
        if (donnees.getMontantEmprunte() <= 0 || donnees.getTauxInteretNominal() <= 0 || donnees.getDuree() <= 0) {
            throw new IllegalArgumentException("Données invalides : montant=" + donnees.getMontantEmprunte() +
                    ", taux=" + donnees.getTauxInteretNominal() + ", durée=" + donnees.getDuree());
        }
        if (donnees.getDeblocages() == null || donnees.getDeblocages().isEmpty()) {
            throw new IllegalArgumentException("Aucun déblocage spécifié.");
        }
        if (donnees.getFrais() < 0 || donnees.getAssurance() < 0) {
            throw new IllegalArgumentException("Frais (" + donnees.getFrais() + ") ou assurance (" +
                    donnees.getAssurance() + ") négatifs.");
        }

        // Vérifie que la somme des déblocages correspond au montant emprunté
        double sommeDeblocages = donnees.getDeblocages().stream().mapToDouble(DeblocageLigne::getMontant).sum();
        if (Math.abs(sommeDeblocages - donnees.getMontantEmprunte()) > 0.01) {
            throw new IllegalArgumentException("Somme des déblocages (" + sommeDeblocages +
                    ") ne correspond pas au montant emprunté (" + donnees.getMontantEmprunte() + ")");
        }

        // Log des données d'entrée
        LOGGER.info("Données : montant=" + donnees.getMontantEmprunte() +
                ", tauxNominal=" + donnees.getTauxInteretNominal() +
                ", durée=" + donnees.getDuree() +
                ", frais=" + donnees.getFrais() +
                ", assurance=" + donnees.getAssurance() +
                ", fréquence=" + donnees.getFrequence() +
                ", première échéance=" + donnees.getDatePremiereEcheance());

        // Extraction et évaluation de la fréquence des échéances
        int frequence = switch (donnees.getFrequence()) {
            case MENSUELLE -> 12;
            case TRIMESTRIALITE -> 4;
            case ANNUELLE -> 1;
            default -> throw new IllegalArgumentException("Fréquence non supportée : " + donnees.getFrequence());
        };

        // Calcul du taux périodique
        double tauxNominalAnnuel = donnees.getTauxInteretNominal() / 100.0; // Convertir en décimal
        double tauxPeriodique = tauxNominalAnnuel / frequence;
        int duree = (int) (donnees.getDuree() * frequence);

        LOGGER.info("Taux nominal annuel : " + tauxNominalAnnuel +
                ", Taux périodique : " + tauxPeriodique +
                ", Durée : " + duree + " périodes");

        // Calculer l'échéance
        double montant = donnees.getMontantEmprunte();
        double echeance;
        try {
            double denominateur = 1 - Math.pow(1 + tauxPeriodique, -duree);
            if (denominateur <= 0) {
                throw new ArithmeticException("Dénominateur invalide : " + denominateur);
            }
            echeance = (montant * tauxPeriodique) / denominateur;
            if (Double.isNaN(echeance) || Double.isInfinite(echeance) || echeance <= 0) {
                throw new ArithmeticException("Échéance invalide : " + echeance);
            }
        } catch (Exception e) {
            LOGGER.severe("Erreur calcul échéance : montant=" + montant +
                    ", tauxPeriodique=" + tauxPeriodique + ", duree=" + duree +
                    ", erreur=" + e.getMessage());
            throw new IllegalStateException("Impossible de calculer l'échéance", e);
        }
        double echeanceTotale = echeance + donnees.getAssurance();

        LOGGER.info("Échéance (capital + intérêts) : " + echeance +
                ", Échéance totale (avec assurance) : " + echeanceTotale);

        // Date de référence
        LocalDate dateReference = donnees.getDeblocages().stream()
                .map(DeblocageLigne::getDateDeblocage)
                .min(LocalDate::compareTo)
                .orElseThrow(() -> new IllegalArgumentException("Erreur dans les dates de déblocage."));

        // ici on valide les dates les dates
        for (DeblocageLigne deblocage : donnees.getDeblocages()) {
            if (deblocage.getDateDeblocage().isBefore(dateReference)) {
                throw new IllegalArgumentException("Date de déblocage antérieure à la date de référence : " +
                        deblocage.getDateDeblocage());
            }
        }
        if (donnees.getDatePremiereEcheance().isBefore(dateReference)) {
            throw new IllegalArgumentException("Date de première échéance antérieure à la date de référence : " +
                    donnees.getDatePremiereEcheance());
        }

        // Calcul de la magnitude des flux pour la tolérance relative
        double fluxMagnitude = sommeDeblocages + echeanceTotale * duree + donnees.getFrais();
        double tolerance = TOLERANCE_RELATIVE * fluxMagnitude;

        LOGGER.info("Magnitude des flux : " + fluxMagnitude + ", Tolérance absolue : " + tolerance);

        // Généretion le tableau d'amortissement
        List<AmortisementLigne> tableau = genererTableau(donnees, tauxPeriodique, echeance, dateReference);

        // Équation du TEG ou en realité on cherche un x ou un i telque f(i)=g(i)
        Function<Double, Double> equation = (taux) -> {
            double sommeGauche = 0.0; // Somme représentant les entrées (déblocages actualisés) soit g(x)
            double sommeDroite = 0.0; // Somme des sorties (échéances actualisées) soit f(x)

            // Déblocages (flux d'entrée)
            for (DeblocageLigne deblocage : donnees.getDeblocages()) {
                long jours = ChronoUnit.DAYS.between(dateReference, deblocage.getDateDeblocage());
                double t_k = jours / 365.0;
                if (t_k < 0) {
                    LOGGER.severe("Temps négatif pour déblocage : " + t_k);
                    throw new IllegalStateException("Temps négatif pour déblocage");
                }
                double flux = deblocage.getMontant() * Math.pow(1 + Math.max(taux, -0.9999), -t_k);
                sommeGauche += flux;
                LOGGER.info("Déblocage : " + deblocage.getMontant() + " à t=" + t_k +
                        ", flux actualisé=" + flux);
            }

            // Frais (flux de sortie à t=0)
            double frais = donnees.getFrais();
            if (frais > 0) {
                sommeDroite += frais;
                LOGGER.info("Frais initiaux : " + frais);
            }

            // Échéances (flux de sortie)
            for (int k = 0; k < duree; k++) {
                LocalDate dateEcheance = donnees.getDatePremiereEcheance();
                if (frequence == 12) {
                    dateEcheance = dateEcheance.plusMonths(k);
                } else if (frequence == 4) {
                    dateEcheance = dateEcheance.plusMonths(3 * k);
                } else {
                    dateEcheance = dateEcheance.plusYears(k);
                }

                long jours = ChronoUnit.DAYS.between(dateReference, dateEcheance);
                double t_p = jours / 365.0;
                if (t_p < 0) {
                    LOGGER.severe("Temps négatif pour échéance : " + t_p);
                    throw new IllegalStateException("Temps négatif pour échéance");
                }
                double flux = echeanceTotale * Math.pow(1 + Math.max(taux, -0.9999), -t_p);
                sommeDroite += flux;
                LOGGER.info("Échéance " + (k + 1) + " à t=" + t_p +
                        ", échéance=" + echeanceTotale + ", flux actualisé=" + flux);
            }

            double result = sommeGauche - sommeDroite;
            LOGGER.info("f(taux=" + taux + ") = " + result);
            return result;
        };

        // Calcul du TEG par recherche binaire
        double tegPeriodique = binarySearch(equation, tauxPeriodique, tolerance);
        //double tegAnnuel = (Math.pow(1 + tegPeriodique, frequence) - 1);
        double tegAnnuel =tegPeriodique;

        LOGGER.info("TEG périodique : " + tegPeriodique + ", TEG annuel : " + tegAnnuel);

        // Préparer la réponse
        SimulationDtoResponse response = new SimulationDtoResponse();
        //response.setTegAnnuel(Math.round(tegAnnuel * 10000.0) / 100.0); // Arrondi à 2 décimales
        response.setTegAnnuel(tegAnnuel*100);
        response.setTableauAmortissement(tableau);
        response.setEcheance(echeanceTotale);
        response.setTableauDeblocages(donnees.getDeblocages());
        response.setCoutTotal((echeanceTotale * duree) - montant);

        return response;
    }

    private double binarySearch(Function<Double, Double> f, double tauxNominalPeriodique, double tolerance) {
        double iBas = 0.0; // Début à 0% pour éviter les TEG négatifs sauf si nécessaire
        double iHaut = 1.0; // Élargir la borne initiale à 100% pour inclure des TEG élevés
        int iterations = 0;

        // Évaluer l'équation aux bornes
        double fBas = evaluateSafely(f, iBas);
        double fHaut = evaluateSafely(f, iHaut);
        LOGGER.info("f(iBas=" + iBas + ") = " + fBas + ", f(iHaut=" + iHaut + ") = " + fHaut);

        // Élargir la borne si nécessaire
        int maxBoundAttempts = 20;
        while (fBas * fHaut > 0 && maxBoundAttempts-- > 0) {
            if (Math.abs(fBas) < Math.abs(fHaut)) {
                iBas = iBas == 0.0 ? -0.01 : iBas / 2; // Test un TEG négatif si nécessaire
                fBas = evaluateSafely(f, iBas);
            } else {
                iHaut *= 2; // Élargir iHaut
                if (iHaut > 10.0) iHaut = 10.0; // Limiter à 1000%
                fHaut = evaluateSafely(f, iHaut);
            }
            LOGGER.info("Élargissement : iBas=" + iBas + ", fBas=" + fBas + ", iHaut=" + iHaut + ", fHaut=" + fHaut);
        }
        if (fBas * fHaut > 0) {
            LOGGER.severe("Échec : pas de changement de signe entre iBas=" + iBas + " et iHaut=" + iHaut);
            throw new IllegalStateException("Échec de la recherche binaire : pas de racine trouvée");
        }

        // Recherche binaire
        while (iterations < MAX_ITERATIONS) {
            double iMid = (iBas + iHaut) / 2;
            double fMid = evaluateSafely(f, iMid);

            if (Math.abs(fMid) < tolerance) {
                LOGGER.info("Convergence à i=" + iMid + ", f(i)=" + fMid + ", itérations=" + iterations);
                return iMid;
            }

            if (fMid > 0 == fBas > 0) {
                iBas = iMid;
                fBas = fMid;
            } else {
                iHaut = iMid;
                fHaut = fMid;
            }
            LOGGER.info("Itération " + iterations + ": iMid=" + iMid + ", fMid=" + fMid + ", iBas=" + iBas + ", iHaut=" + iHaut);
            iterations++;
        }

        double iMid = (iBas + iHaut) / 2;
        double fMid = evaluateSafely(f, iMid);
        LOGGER.warning("Maximum d'itérations atteint. Résultat approximatif : i=" + iMid + ", f(i)=" + fMid);
        return iMid;
    }

    //Evaluation sécuritaire (verifions si tout va bien si le resultat n'est pas incohrant ou encore lorque la soultion est introuvable )
    private double evaluateSafely(Function<Double, Double> f, double taux) {
        try {
            double result = f.apply(taux);
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                LOGGER.severe("Évaluation invalide pour taux=" + taux + ": résultat=" + result);
                throw new ArithmeticException("Évaluation numérique instable");
            }
            return result;
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de l'évaluation pour taux=" + taux + ": " + e.getMessage());
            throw new IllegalStateException("Échec de l'évaluation de l'équation", e);
        }
    }

    private List<AmortisementLigne> genererTableau(DonneesPret donnees, double tauxPeriodique,
                                                   double echeance, LocalDate dateReference) {
        List<AmortisementLigne> tableau = new ArrayList<>();
        double capitalRestant = 0.0;
        List<DeblocageLigne> deblocages = new ArrayList<>(donnees.getDeblocages());
        deblocages.sort((d1, d2) -> d1.getDateDeblocage().compareTo(d2.getDateDeblocage()));

        int frequence = switch (donnees.getFrequence()) {
            case MENSUELLE -> 12;
            case TRIMESTRIALITE -> 4;
            case ANNUELLE -> 1;
            default -> throw new IllegalArgumentException("Fréquence non supportée");
        };

        int duree = (int) (donnees.getDuree() * frequence);

        for (int periode = 1; periode <= duree; periode++) {
            LocalDate dateEcheance = donnees.getDatePremiereEcheance().plusMonths(periode - 1);

            // Ajout des déblocages
            for (DeblocageLigne deblocage : new ArrayList<>(deblocages)) {
                if (!deblocage.getDateDeblocage().isAfter(dateEcheance)) {
                    capitalRestant += deblocage.getMontant();
                    LOGGER.info("Déblocage : " + deblocage.getMontant() + " à période " + periode +
                            ", date=" + deblocage.getDateDeblocage());
                    deblocages.remove(deblocage);
                }
            }

            double interet = capitalRestant * tauxPeriodique;
            double principal = Math.min(echeance - interet, capitalRestant);
            if (principal < 0) {
                LOGGER.warning("Principal négatif à période " + periode + ": " + principal);
                principal = 0;
            }
            capitalRestant -= principal;

            AmortisementLigne ligne = new AmortisementLigne();
            ligne.setNumero(periode);
            ligne.setCapitalRestant(Math.max(capitalRestant, 0));
            ligne.setInteret(interet);
            ligne.setPrincipal(principal);
            ligne.setAssurance(donnees.getAssurance());
            ligne.setEcheanceTotale(echeance + donnees.getAssurance());

            tableau.add(ligne);
            LOGGER.info("Période " + periode + ": Capital=" + ligne.getCapitalRestant() +
                    ", Intérêt=" + interet + ", Principal=" + principal +
                    ", Échéance totale=" + ligne.getEcheanceTotale());
        }

        if (Math.abs(capitalRestant) > 0.01) {
            LOGGER.warning("Capital restant non nul : " + capitalRestant);
        }

        return tableau;
    }
}