package pharmacie.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pharmacie.service.ApprovisionnementService;

@RestController
@RequestMapping("/api/approvisionnement")
public class ApprovisionnementController {

    @Autowired
    private ApprovisionnementService approService;

    @PostMapping("/declencher")
    public ResponseEntity<String> declencher() {
        try {
            approService.traiterReapprovisionnement();
            return ResponseEntity.ok("Processus de réapprovisionnement lancé et emails envoyés.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur : " + e.getMessage());
        }
    }
}
