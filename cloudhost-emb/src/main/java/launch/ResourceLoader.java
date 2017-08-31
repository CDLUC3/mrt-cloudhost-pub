/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch;

/**
 *
 * @author replic
 */
import java.util.HashSet;
import java.util.Set;
 
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.cdlib.mrt.cloudhost.app.jersey.service.JerseyCloudhost;
 
public class ResourceLoader extends Application{
 
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        
        // register root resource
        classes.add(JerseyCloudhost.class);
        return classes;
    }
}
