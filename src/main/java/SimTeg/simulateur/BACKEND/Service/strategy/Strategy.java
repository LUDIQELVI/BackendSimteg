package SimTeg.simulateur.BACKEND.Service.strategy;

import com.flickr4java.flickr.FlickrException;
import SimTeg.simulateur.BACKEND.Entity.RegistrationResponse;

import java.io.InputStream;

public interface Strategy<T> {

    RegistrationResponse savePhotos(String email, InputStream photo, String titre) throws FlickrException;
}
