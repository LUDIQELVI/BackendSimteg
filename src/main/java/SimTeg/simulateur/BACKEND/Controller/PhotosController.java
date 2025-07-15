package SimTeg.simulateur.BACKEND.Controller;

import com.flickr4java.flickr.FlickrException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import SimTeg.simulateur.BACKEND.Service.strategy.StrategyPhotosContext;

import java.io.IOException;

@RestController
@CrossOrigin("*")
public class PhotosController {

   private StrategyPhotosContext strategyPhotosContext;

    @Autowired
    public PhotosController(StrategyPhotosContext strategyPhotosContext) {
        this.strategyPhotosContext = strategyPhotosContext;
    }
    @PostMapping("auth/photos/{email}/{title}/{context}")
    public Object savePhotos(@PathVariable String context, @PathVariable String email, @RequestParam("file") MultipartFile photo, @PathVariable String title) throws IOException, FlickrException {
        System.out.println(email+"controller");
        System.out.println(context+"controller");
        System.out.println(title+"controller");
        return strategyPhotosContext.savePhotos(context, email, photo.getInputStream(), title);
    }

}
