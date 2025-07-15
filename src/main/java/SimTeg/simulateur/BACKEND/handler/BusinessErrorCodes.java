package SimTeg.simulateur.BACKEND.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public enum BusinessErrorCodes {

    NO_CODE(0, NOT_IMPLEMENTED, "No code"),
    INCORRECT_CURRENT_PASSWORD(300, BAD_REQUEST, "Current password is incorrect"),
    NEW_PASSWORD_DOES_NOT_MATCH(301, BAD_REQUEST, "The new password does not match"),
    ACCOUNT_LOCKED(302, FORBIDDEN, "User Account is locked"),
    ACCOUNT_DISABLE(303, FORBIDDEN, "User Account is disable"),
    BAD_CREDENTIALS(304, FORBIDDEN, "Login and / or Password Incorrect")


    ;

    @Getter
    private int code;
    @Getter
    private String description;
    @Getter
    private HttpStatus httpStatus;

    BusinessErrorCodes(int code , HttpStatus httpStatus, String description){
        this.code =code;
        this.httpStatus= httpStatus;
        this.description=description;
    }

}
