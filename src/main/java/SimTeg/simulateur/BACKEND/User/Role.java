package SimTeg.simulateur.BACKEND.User;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import SimTeg.simulateur.BACKEND.Entity.AbstractEntity;

import java.util.List;

@Getter
@Setter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Role")
@EntityListeners(AuditingEntityListener.class)
public class Role extends AbstractEntity {

    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;

}
