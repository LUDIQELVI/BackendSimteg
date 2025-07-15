package SimTeg.simulateur.BACKEND.User;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AuthenticationResponse {
    private String token;
    private String refreshToken;
    private User user;
    private List<User> userList;
    private String message;
    private String erroMsg;
    private long totalElements;
    private int totalPages;
}