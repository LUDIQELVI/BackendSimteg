package SimTeg.simulateur.BACKEND.Service.strategy;

import com.flickr4java.flickr.FlickrException;
import lombok.Setter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class StrategyPhotosContext {

    private BeanFactory beanFactory;
    private Strategy strategy;

    @Setter
    private String context;

    @Autowired
    public StrategyPhotosContext(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public Object savePhotos(String context, String email, InputStream photos, String title) throws FlickrException {
        determineContext(context);
        System.out.println(email + "Strategy");
        if (strategy == null) {
            throw new FlickrException("Strategy not initialized for context: " + context);
        }
        return strategy.savePhotos(email, photos, title);
    }

    public void determineContext(String context) {
        this.context = context; // Store context for debugging
        final String beanName = context + "Strategy";
        try {
            switch (context) {
                case "user":
                    strategy = beanFactory.getBean(beanName, SaveUserPhotos.class);
                    break;
                default:
                    throw new IllegalStateException("Contexte non pris en charge : " + context);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la détermination du contexte : " + e.getMessage(), e);
        }
    }
}