package SimTeg.simulateur.BACKEND.Controller.SimTegController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.Notification;
import SimTeg.simulateur.BACKEND.Service.SimTegService.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/afficherNot/{idSimulation}")
    public ResponseEntity<List<Notification>> afficherNotifSimulation(@PathVariable Integer idSimulation) {
        try {
            List<Notification> notifications = notificationService.afficherNotificationSim(idSimulation);
            return ResponseEntity.ok(notifications);
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer id) {
        try {
            notificationService.markNotificationAsRead(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}