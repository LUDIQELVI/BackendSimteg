package SimTeg.simulateur.BACKEND.Service;

import com.flickr4java.flickr.FlickrException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import SimTeg.simulateur.BACKEND.Config.JwtService;
import SimTeg.simulateur.BACKEND.Entity.RegistrationRequest;
import SimTeg.simulateur.BACKEND.Entity.RegistrationResponse;
import SimTeg.simulateur.BACKEND.Repository.RoleRepository;
import SimTeg.simulateur.BACKEND.Repository.UserRepository;
import SimTeg.simulateur.BACKEND.User.AuthenticationRequest;
import SimTeg.simulateur.BACKEND.User.AuthenticationResponse;
import SimTeg.simulateur.BACKEND.User.Role;
import SimTeg.simulateur.BACKEND.User.User;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private FlickrService flickrService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public RegistrationResponse register(RegistrationRequest request, InputStream photoStream, String photoTitle) {
        try {
            // Validation du numéro de téléphone (+237)
            if (!request.getTelephone().startsWith("+237") || request.getTelephone().length() != 13) {
                return RegistrationResponse.builder()
                        .erroMsg("Le numéro de téléphone doit commencer par +237 et contenir 9 chiffres.")
                        .build();
            }

            // Vérifier si l'email existe déjà
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return RegistrationResponse.builder()
                        .erroMsg("Cet email est déjà utilisé.")
                        .build();
            }

            // Initialisation du rôle USER
            var userRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new IllegalStateException("Rôle USER non initialisé"));

            // Gestion de la photo
            String photoUrl = null;
            boolean photoError = false;
            if (photoStream != null && photoTitle != null) {
                try {
                    photoUrl = flickrService.savePhotos(photoStream, photoTitle);
                } catch (FlickrException | RuntimeException e) {
                    // Log l'erreur mais ne bloque pas l'inscription
                    System.err.println("Erreur Flickr : " + e.getMessage());
                    photoError = true;
                }
            }

            // Construction de l'utilisateur
            var user = User.builder()
                    .firstName(request.getFirstName())
                    .email(request.getEmail())
                    .telephone(request.getTelephone())
                    .dateNaiss(LocalDate.parse(request.getDateNaiss()))
                    .photos(photoUrl)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .accountLocked(false)
                    .enable(true)
                    .roles(List.of(userRole))
                    .build();

            // Enregistrement de l'utilisateur
            User savedUser = userRepository.save(user);

            return RegistrationResponse.builder()
                    .user(savedUser)
                    .message(photoError
                            ? "Inscription réussie, mais la photo n’a pas pu être enregistrée."
                            : "Inscription réussie ! Veuillez vous connecter.")
                    .build();

        } catch (Exception e) {
            return RegistrationResponse.builder()
                    .erroMsg("Erreur lors de l'inscription : " + e.getMessage())
                    .build();
        }
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));

            var claims = new HashMap<String, Object>();
            claims.put("fullName", user.fullName());
            String refreshToken = jwtService.generateRefreshToken(user);
            var jwtToken = jwtService.generateToken(claims, user);

            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .refreshToken(refreshToken)
                    .user(user)
                    .message("Connexion réussie.")
                    .build();
        } catch (Exception e) {
            return AuthenticationResponse.builder()
                    .erroMsg("Erreur d'authentification : " + e.getMessage())
                    .build();
        }
    }

    public AuthenticationResponse getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.findAll(pageable);
        return AuthenticationResponse.builder()
                .userList(usersPage.getContent())
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .message("Liste des utilisateurs récupérée avec succès.")
                .build();
    }

    public RegistrationResponse updatePhotos(String email, String photoUrl) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User existingUser = userOptional.get();
                existingUser.setPhotos(photoUrl);
                User savedUser = userRepository.save(existingUser);
                return RegistrationResponse.builder()
                        .user(savedUser)
                        .message("Photo mise à jour avec succès.")
                        .build();
            } else {
                return RegistrationResponse.builder()
                        .erroMsg("Utilisateur non trouvé.")
                        .build();
            }
        } catch (Exception e) {
            return RegistrationResponse.builder()
                    .erroMsg("Erreur lors de la mise à jour de la photo : " + e.getMessage())
                    .build();
        }
    }

    public void toggleUserStatus(RegistrationRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            existingUser.setEnable(!existingUser.isEnabled());
            userRepository.save(existingUser);
        } else {
            throw new IllegalStateException("Utilisateur non trouvé avec l'email : " + request.getEmail());
        }
    }

    @Transactional
    public void updateRole(RegistrationRequest request, String roleName) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (!userOptional.isPresent()) {
            throw new IllegalStateException("Utilisateur non trouvé");
        }

        Optional<Role> roleOptional = roleRepository.findByName(roleName);
        if (!roleOptional.isPresent()) {
            throw new IllegalStateException("Rôle non trouvé");
        }

        User existingUser = userOptional.get();
        Role role = roleOptional.get();
        List<Role> roles = new ArrayList<>(existingUser.getRoles());
        if (!roles.contains(role)) {
            roles.add(role);
            existingUser.setRoles(roles);
            userRepository.save(existingUser);
        }
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    public String refreshAccessToken(String refreshToken) {
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new IllegalStateException("Le refresh token a expiré.");
        }
        String username = jwtService.extractUsername(refreshToken);
        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
        return jwtService.generateToken(user);
    }
}
