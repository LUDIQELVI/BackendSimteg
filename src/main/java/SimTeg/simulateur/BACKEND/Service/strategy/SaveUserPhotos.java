package SimTeg.simulateur.BACKEND.Service.strategy;

import com.flickr4java.flickr.FlickrException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import SimTeg.simulateur.BACKEND.Entity.RegistrationResponse;
import SimTeg.simulateur.BACKEND.Repository.UserRepository;
import SimTeg.simulateur.BACKEND.Service.AuthenticationService;
import SimTeg.simulateur.BACKEND.Service.FlickrService;
import SimTeg.simulateur.BACKEND.User.User;

import java.io.InputStream;

@Service("userStrategy")
@Slf4j
public class SaveUserPhotos implements Strategy<User>{

    private FlickrService flickrService;
    private UserRepository usersRepo;

    private AuthenticationService usersManagementService;

    @Autowired
    public SaveUserPhotos(FlickrService flickrService, AuthenticationService usersManagementService) {
        this.flickrService = flickrService;
        this.usersManagementService = usersManagementService;
    }

    @Override
    public RegistrationResponse savePhotos(String email, InputStream photo, String titre) throws FlickrException {

        String urlPhoto = flickrService.savePhotos(photo, titre);
        System.out.println(email);
        System.out.println(urlPhoto);

        if (!StringUtils.hasLength(urlPhoto)){
            throw new IllegalArgumentException("echecs de l'enregitrement");
        }
        return usersManagementService.updatePhotos(email, urlPhoto);
    }


}
