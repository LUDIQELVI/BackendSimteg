package SimTeg.simulateur.BACKEND.Service.impl;

import com.flickr4java.flickr.*;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.uploader.UploadMetaData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import SimTeg.simulateur.BACKEND.Service.FlickrService;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class FlickrServiceImpl implements SimTeg.simulateur.BACKEND.Service.FlickrService {

    private String apiKey = "81ad6ad2a9ea9e218688128cb4c573c3";

    private String apiSecret = "360d183003cce6b4";

    private String appKey = "72157720925926688-4af8f6a880d582a0";

    private String appSecret = "3cdce1a1be3a2395";


    private Flickr flickr;

    @Autowired
    public FlickrServiceImpl(Flickr flickr) {
        this.flickr = flickr;
    }

    @Override
    @SneakyThrows
    public String savePhotos(InputStream photo, String title) {
        try {
            connect();
            System.out.println("Connected to Flickr API successfully");

            UploadMetaData uploadMetaData = new UploadMetaData();
            uploadMetaData.setTitle(title);
            System.out.println("Metadata set successfully");

            String photoId = flickr.getUploader().upload(photo, uploadMetaData);
            System.out.println("Photo uploaded successfully. Photo ID: " + photoId);

            return flickr.getPhotosInterface().getPhoto(photoId).getMedium640Url();
        } catch (FlickrRuntimeException e) {
            System.err.println("Flickr API Error: " + e.getMessage());
            throw new RuntimeException("Error communicating with Flickr API", e);
        } catch (IOException e) {
            System.err.println("File I/O Error: " + e.getMessage());
            throw new RuntimeException("Error reading photo file", e);
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            throw new RuntimeException("An unexpected error occurred", e);
        }
    }


    public void deletePhoto(String photoId) throws FlickrException {
        flickr.getPhotosInterface().delete(photoId);
    }


    private void connect() throws InterruptedException, ExecutionException, IOException, FlickrException {
        flickr = new Flickr(apiKey, apiSecret, new REST());
        Auth auth = new Auth();
        auth.setPermission(Permission.READ);
        auth.setToken(appKey);
        auth.setTokenSecret(appSecret);
        RequestContext requestContext = RequestContext.getRequestContext();
        requestContext.setAuth(auth);
        flickr.setAuth(auth);
    }

}
