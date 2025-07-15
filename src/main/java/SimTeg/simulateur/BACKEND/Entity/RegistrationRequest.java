package SimTeg.simulateur.BACKEND.Entity;

import lombok.Data;

@Data
public class RegistrationRequest {
    private String firstName;
    private String email;
    private String telephone;
    private String dateNaiss;
    private String password;
}