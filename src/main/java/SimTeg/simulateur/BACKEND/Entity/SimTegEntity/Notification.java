package SimTeg.simulateur.BACKEND.Entity.SimTegEntity;

import jakarta.persistence.*;
import lombok.Data;
import SimTeg.simulateur.BACKEND.Entity.AbstractEntity;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "notification")
public class Notification extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "simulation_id")
    private Simulation simulation;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "date_notification")
    private LocalDateTime dateNotification;

    @Column(name = "alerte")
    private boolean alerte;

    @Column(name = "annee")
    private int annee;

    @Column(name = "`read`", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean read = false;

    @Version
    @Column(nullable = false)
    private Integer version = 0;

    @PrePersist
    protected void onCreate() {
        this.setCreationDate(LocalDateTime.now());
        this.setLastModifyDate(LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        this.setLastModifyDate(LocalDateTime.now());
    }
}
