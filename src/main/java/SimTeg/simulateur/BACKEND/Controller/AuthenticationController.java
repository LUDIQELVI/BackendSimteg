package SimTeg.simulateur.BACKEND.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import SimTeg.simulateur.BACKEND.Entity.RegistrationRequest;
import SimTeg.simulateur.BACKEND.Entity.RegistrationResponse;
import SimTeg.simulateur.BACKEND.Service.AuthenticationService;
import SimTeg.simulateur.BACKEND.User.AuthenticationRequest;
import SimTeg.simulateur.BACKEND.User.AuthenticationResponse;
import SimTeg.simulateur.BACKEND.User.User;
import SimTeg.simulateur.BACKEND.Entity.RoleUpdateRequest;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Validated
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegistrationResponse> register(
            @RequestPart(value = "request", required = false) @Valid RegistrationRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestPart(value = "photoTitle", required = false) String photoTitle
    ) {
        if (request == null) {
            return ResponseEntity.badRequest().body(RegistrationResponse.builder()
                    .erroMsg("Requête d'inscription manquante")
                    .build());
        }
        try {
            RegistrationResponse response = service.register(request, photo != null ? photo.getInputStream() : null, photoTitle);
            if (response.getErroMsg() != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RegistrationResponse.builder()
                            .erroMsg("Erreur serveur lors de l'inscription : " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        AuthenticationResponse response = service.authenticate(request);
        if (response.getErroMsg() != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/activate")
    public ResponseEntity<?> activateAccount(@RequestBody @Valid RegistrationRequest request) {
        try {
            service.toggleUserStatus(request);
            return ResponseEntity.ok(Map.of("message", "Statut du compte modifié avec succès."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/admin/updateRole")
    public ResponseEntity<?> updateRole(@RequestBody RoleUpdateRequest roleUpdateRequest) {
        try {
            RegistrationRequest request = roleUpdateRequest.getRequest();
            String roleName = roleUpdateRequest.getRoleName();

            if (request == null || roleName == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Requête invalide"));
            }

            service.updateRole(request, roleName);
            return ResponseEntity.ok(Map.of("message", "Rôle mis à jour avec succès."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur serveur : " + e.getMessage()));
        }
    }

    @GetMapping("/admin/getAllUsers")
    public ResponseEntity<AuthenticationResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getAllUsers(page, size));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Utilisateur non authentifié"));
            }
            String email = authentication.getName();
            User user = service.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Utilisateur non trouvé"));
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur serveur : " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        service.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshAccessToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            String newAccessToken = service.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
