package SimTeg.simulateur.BACKEND.Entity;

import SimTeg.simulateur.BACKEND.User.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistrationResponse {
    private User user;
    private String message;
    private String erroMsg;
}