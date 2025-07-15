package SimTeg.simulateur.BACKEND.Service.SimTegService;

import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationDtoRequest;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationDtoResponse;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationRequestGlobal;
import com.itextpdf.html2pdf.HtmlConverter;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;


import org.springframework.stereotype.Service;
// exportation de la simulation en pdf
@Service
public class ExportSimulationService {

    public void exportToPdf(SimulationRequestGlobal simulation, OutputStream outputStream) {
        String htmlContent = buildHtml(simulation);
        HtmlConverter.convertToPdf(htmlContent, outputStream);
    }

    private String buildHtml(SimulationRequestGlobal simulation) {
        SimulationDtoRequest request = simulation.getRequest();
        SimulationDtoResponse response = simulation.getResponse();

        StringBuilder html = new StringBuilder();
        html.append("<html><head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; }")
                .append("h1 { text-align: center; color: #2c3e50; }")
                .append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }")
                .append("th, td { border: 1px solid #ccc; padding: 8px; text-align: center; }")
                .append("th { background-color: #f0f0f0; }")
                .append("</style>")
                .append("</head><body>");

        html.append("<h1>Simulation de Crédit</h1>");

        // Données de la simulation
        html.append("<h3>Détails de la demande</h3>")
                .append("<p><strong>Montant Emprunté :</strong> ").append(request.getMontantEmprunte()).append(" FCFA</p>")
                .append("<p><strong>Taux Nominal :</strong> ").append(request.getTauxInteretNominal()).append(" %</p>")
                .append("<p><strong>Durée :</strong> ").append(request.getDuree()).append(" ").append(request.getFrequence()).append("</p>")
                .append("<p><strong>Type d'emprunteur :</strong> ").append(request.getTypeEprunteur()).append("</p>")
                .append("<p><strong>Date première échéance :</strong> ").append(request.getDatePremiereEcheance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</p>");
        //Tableau de dblocages
        html.append("<h3> Tableau de déblocages");
        html.append("<table><tr><th>DateDedblocages</th><th>Numero</th><th>MontantDébloqué</th></tr>");
        int indexe = 1;
        for (var ligne : request.getTableauDeblocages()) {
            html.append("<tr>")
                    //.append("<td>").append(indexe++).append("</td>")
                 .append("<td>").append(ligne.getDateDeblocage().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</td>")
                    .append("<td>").append(String.format("%d", ligne.getNumero())).append("</td>")
                    .append("<td>").append(String.format("%.2f", ligne.getMontant())).append("</td>")
                    .append("</tr>");
        }
        html.append("</table>");


        // Frais
        html.append("<h3>Frais</h3><ul>");
        for (var f : request.getFraisList()) {
            html.append("<li>").append(f.getNom()).append(" : ").append(f.getMontant()).append(" FCFA</li>");
        }
        html.append("</ul>");



        // Assurances
        html.append("<h3>Assurances</h3><ul>");
        for (var a : request.getAssuranceList()) {
            html.append("<li>").append(a.getNom()).append(" : ").append(a.getMontant()).append(" FCFA</li>");
        }
        html.append("</ul>");


        // Résultats
        html.append("<h3>Résultats</h3>")
                .append("<p><strong>TEG Annuel :</strong> ").append(String.format("%.2f", response.getTegAnnuel())).append(" %</p>")
                .append("<p><strong>Échéance :</strong> ").append(String.format("%.2f", response.getEcheance())).append(" FCFA</p>");

        // Amortissement
        html.append("<h3>Tableau d'Amortissement</h3>");
        html.append("<table><tr><th>NumeroParEcheance</th><th>Capital</th><th>Intérêt</th><th>Échéance</th><th>Capital Restant</th></tr>");
        int index = 1;
        for (var ligne : response.getTableauAmortissement()) {
            html.append("<tr>")
                    .append("<td>").append(index++).append("</td>")
                    //.append("<td>").append(ligne.get().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</td>")
                    .append("<td>").append(String.format("%.2f", ligne.getCapitalRestant())).append("</td>")
                    .append("<td>").append(String.format("%.2f", ligne.getInteret())).append("</td>")
                    .append("<td>").append(String.format("%.2f", ligne.getEcheanceTotale())).append("</td>")
                    .append("<td>").append(String.format("%.2f", ligne.getCapitalRestant())).append("</td>")
                    .append("</tr>");
        }
        html.append("</table>");

        html.append("</body></html>");

        return html.toString();
    }
}
