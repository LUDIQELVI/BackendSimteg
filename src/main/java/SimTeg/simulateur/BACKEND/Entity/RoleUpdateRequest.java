
package SimTeg.simulateur.BACKEND.Entity;

import lombok.Data;

@Data
public class RoleUpdateRequest {
    private RegistrationRequest request;
    private String roleName;
}
