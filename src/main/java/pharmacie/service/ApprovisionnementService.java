package pharmacie.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pharmacie.dao.MedicamentRepository;
import pharmacie.entity.Fournisseur;
import pharmacie.entity.Medicament;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApprovisionnementService {

    @Autowired
    private MedicamentRepository medicamentRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String mailFrom;

    public void traiterReapprovisionnement() {
        // 1. Médicaments en sous-stock
        List<Medicament> aReappro = medicamentRepository.findAll().stream()
                .filter(m -> m.getUnitesEnStock() < m.getNiveauDeReappro())
                .collect(Collectors.toList());

        if (aReappro.isEmpty()) return;

        // 2. Grouper par fournisseur
        Map<Fournisseur, List<Medicament>> envoisParFournisseur = new HashMap<>();
        for (Medicament m : aReappro) {
            for (Fournisseur f : m.getCategorie().getFournisseurs()) {
                envoisParFournisseur.computeIfAbsent(f, k -> new ArrayList<>()).add(m);
            }
        }

        // 3. Envoyer les mails via le SMTP Mailgun
        for (Map.Entry<Fournisseur, List<Medicament>> entry : envoisParFournisseur.entrySet()) {
            envoyerMailAppro(entry.getKey(), entry.getValue());
        }
    }

    private void envoyerMailAppro(Fournisseur f, List<Medicament> medicaments) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(f.getEmail()); // Pour le sandbox, cet email doit être "Authorized" dans Mailgun
        message.setSubject("Demande de devis réapprovisionnement - Pharmacie");

        StringBuilder corps = new StringBuilder("Bonjour " + f.getNom() + ",\n\n");
        corps.append("Voici la liste des produits à réapprovisionner pour notre pharmacie :\n");

        // Groupement par catégorie pour la lisibilité
        Map<String, List<Medicament>> parCat = medicaments.stream()
                .collect(Collectors.groupingBy(m -> m.getCategorie().getLibelle()));

        parCat.forEach((cat, meds) -> {
            corps.append("\nCatégorie : ").append(cat).append("\n");
            for (Medicament m : meds) {
                corps.append("- ").append(m.getNom())
                     .append(" (Réf: ").append(m.getReference())
                     .append(") - Stock actuel: ").append(m.getUnitesEnStock()).append("\n");
            }
        });

        corps.append("\nMerci de nous transmettre un devis dans les plus brefs délais.");
        
        message.setText(corps.toString());
        mailSender.send(message);
    }
}