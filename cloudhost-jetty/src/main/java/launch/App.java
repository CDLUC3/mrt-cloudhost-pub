/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch;

import java.net.MalformedURLException;
import javax.servlet.ServletException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebAppContext;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.StringUtil;

public class App {
 
    public static void main(String[] args) throws Exception {
        new App().start();
    }
 
    public void start() throws ServletException, MalformedURLException, Exception {
 
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
        if (!fileCloud.exists()) {
            fileCloud.mkdir();
            File anchor = new File(fileCloud, "bucket-anchor.txt");
            FileUtil.string2File(anchor, "bucket anchor");
        }
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
 
                String webPortS = System.getenv("CLOUDHOST_PORT");
        if (webPortS == null || webPortS.isEmpty()) {
            webPortS= "38080";
        }
        int webPort = Integer.parseInt(webPortS);
        
        Server jettyServer = new Server(webPort);
        jettyServer.setStopAtShutdown(true);
        
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/cloudhost");
        webAppContext.setResourceBase("./cxt");       
        webAppContext.setClassLoader(getClass().getClassLoader());
        jettyServer.setHandler(webAppContext);
        jettyServer.start();
    }
    
    public static class Test { }
 
}