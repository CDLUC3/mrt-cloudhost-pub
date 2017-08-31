/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch;
import javax.servlet.ServletException;
 
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import org.cdlib.mrt.utility.FileUtil;
 
public class Main {
 
    public static void main(String[] args) throws Exception, LifecycleException {
        new Main().start();
    }
 
    public void start() throws ServletException, LifecycleException,
            MalformedURLException {
 
        // Define a folder to hold web application contents.
        //String webappDirLocation = "/apps/replic/MRTMaven/test/embedded/context/";
        String webappDirLocation = "./cxt/";
        String webinfDirLocation = webappDirLocation + "WEB-INF";
        File webinfDir = new File(webinfDirLocation);
        webinfDir.mkdirs();
        File webxml = new File(webinfDir,"web.xml"); 
        Test test = new Test();
        InputStream instream =  test.getClass().getClassLoader().
                getResourceAsStream("resources/embedded-web.xml");
        try {
            FileUtil.stream2File(instream, webxml);
        } catch(Exception ex) {
            throw new ServletException(ex.toString());
        }
        
        File fileCloud = new File("./fileCloud");
        fileCloud.mkdir();
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
        Tomcat tomcat = new Tomcat();
 
        // Define port number for the web application
        String webPort = System.getenv("CLOUDHOST_PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "38080";
        }
        // Bind the port to Tomcat server
        tomcat.setPort(Integer.valueOf(webPort));
 
        // Define a web application context - sets URL prefix
        Context context = tomcat.addWebapp("/cloudhost", new File(
                webappDirLocation).getAbsolutePath());
 
        // Define and bind web.xml file location.
        File configFile = new File(webappDirLocation + "WEB-INF/web.xml");
        context.setConfigFile(configFile.toURI().toURL());
 
        tomcat.start();
        tomcat.getServer().await();
    }
    
    public static class Test { }
 
}