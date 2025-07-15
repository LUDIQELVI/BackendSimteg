package SimTeg.simulateur.BACKEND.Dto.SimTegDto;

import SimTeg.simulateur.BACKEND.Dto.DataType.DeblocageLigne;
import lombok.Data;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.Frequence;
import SimTeg.simulateur.BACKEND.Service.SimTegService.CalculTEGService;

import java.time.LocalDate;
import java.util.List;

@Data
public class DonneesPret {

        public double montantEmprunte;
        public double assurance; // mensualité ou annuité selon la fréquence
        public double tauxInteretNominal;
        public double frais;
        public double duree;
        public Frequence frequence;
        public List<DeblocageLigne> deblocages;
        private LocalDate DatePremiereEcheance;



}
