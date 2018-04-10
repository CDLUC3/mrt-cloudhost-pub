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
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;

public class AppSSL {
 
    public static void main(String[] args) 
        throws Exception 
    {
        String password = null; 
        Integer sslPort = null; 
        Integer standardPort = null;
        System.out.println("***args length:" + args.length);
        if (args.length > 0) password = args[0];
        if (args.length > 1) {
            sslPort = convInt("ssl parameter", args[1]);
        }
        if (args.length > 2) {
            standardPort = convInt("general parameter", args[2]);
        }
        new AppSSL().start(password, sslPort, standardPort);
    }
    
    private static int convInt(String header, String integerS)
        throws TException
    {
        int retVal = 0;
        if (StringUtil.isAllBlank(integerS)) {
            throw new TException.INVALID_OR_MISSING_PARM(header + " not supplied");
        }
        try {
            retVal = Integer.parseInt(integerS);
        } catch (Exception ex) {
            throw new TException.INVALID_OR_MISSING_PARM(header + " supplied value not numeric:" + integerS);
        }
        System.out.println("*convInt*" + header + ":" + retVal);
        return retVal;
    }
 
    public void start(String password, Integer sslPort, Integer standardPort) 
            throws ServletException, MalformedURLException, Exception 
    {
        addWebxml();
        addFilecloud();
        addKeystore();
        setJetty(password, sslPort, standardPort);
    }
 
    public void setJetty(String password, Integer sslPort, Integer standardPort) 
            throws ServletException, MalformedURLException, TException, Exception 
    {
        System.out.println("***input parameters:\n"
                + " - password=" + password + "\n"
                + " - sslPort=" + sslPort + "\n"
                + " - standardPort=" + standardPort + "\n"
        );
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
 
        String standPortS = System.getenv("CLOUDHOST_PORT");
        if (!StringUtil.isAllBlank(standPortS)) {
            standardPort = convInt("CLOUDHOST_PORT", standPortS);
        }
 
        String sslPortS = System.getenv("CLOUDHOST_SSL");
        if (!StringUtil.isAllBlank(sslPortS)) {
            sslPort = convInt("CLOUDHOST_SSL", sslPortS);
        }
        
        if ((standardPort == null) && (sslPort == null)) {
            throw new TException.INVALID_OR_MISSING_PARM("Error: No port supplied");
        }
        
        Server jettyServer = new Server();
        if (standardPort != null) {
            ServerConnector connector = new ServerConnector(jettyServer);
            connector.setPort(standardPort);
            jettyServer.addConnector(connector);
        }
        
        String sslpwd = System.getenv("CLOUDHOST_PWD");
        System.out.println("***system env:\n"
                + " - CLOUDHOST_PORT=" + standPortS + "\n"
                + " - CLOUDHOST_SSL=" + sslPortS + "\n"
                + " - CLOUDHOST_PWD=" + sslpwd + "\n"
        );
        jettyServer.setStopAtShutdown(true);
        
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/cloudhost");
        webAppContext.setResourceBase("./cxt");       
        webAppContext.setClassLoader(getClass().getClassLoader());
        jettyServer.setHandler(webAppContext);
        
        if (sslPort != null) {
 
            if (!StringUtil.isAllBlank(sslpwd)) {
                password = sslpwd;
            }
            if (StringUtil.isAllBlank(password)) {
                throw new TException.INVALID_OR_MISSING_PARM("SSL password not supplied");
            }
        
            HttpConfiguration https = new HttpConfiguration();
            https.addCustomizer(new SecureRequestCustomizer());
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath("./etc/keystore.jks");
            sslContextFactory.setKeyStorePassword(password);
            sslContextFactory.setKeyManagerPassword(sslpwd);
            
            ServerConnector sslConnector = new ServerConnector(jettyServer,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(https));
            sslConnector.setPort(sslPort);
            jettyServer.addConnector(sslConnector);
        }
        System.out.println("***Ports\n"
                + " - standardPort=" + standardPort + "\n"
                + " - sslPort=" + sslPort + "\n"
        );
        jettyServer.start();
    }
    
    public void addResource(String resource, File targetFile) 
            throws ServletException, MalformedURLException, Exception 
    {
        Test test = new Test();
        InputStream instream =  test.getClass().getClassLoader().
                getResourceAsStream(resource);
        try {
            FileUtil.stream2File(instream, targetFile);
        } catch(Exception ex) {
            throw new ServletException(ex.toString());
        }
    }
    
    public void addFilecloud() 
            throws ServletException, MalformedURLException, Exception 
    {
        File fileCloud = new File("./fileCloud");
        if (!fileCloud.exists()) {
            fileCloud.mkdir();
            File anchor = new File(fileCloud, "bucket-anchor.txt");
            FileUtil.string2File(anchor, "bucket anchor");
        }
    }
    
    public void addWebxml() 
            throws ServletException, MalformedURLException, Exception 
    {
        String webappDirLocation = "./cxt/";
        String webinfDirLocation = webappDirLocation + "WEB-INF";
        File webinfDir = new File(webinfDirLocation);
        webinfDir.mkdirs();
        File webxml = new File(webinfDir,"web.xml"); 
        addResource("resources/embedded-web.xml", webxml);
    }
    
    public void addKeystore() throws ServletException, MalformedURLException, Exception {
 
        // Define a folder to hold web application contents.
        //String webappDirLocation = "/apps/replic/MRTMaven/test/embedded/context/";
        String etcLocation = "./etc/";
        File etcDir = new File(etcLocation);
        etcDir.mkdirs();
        File keystore = new File(etcDir, "keystore.jks");
        addResource("resources/keystore", keystore);
    }
    
    public static class Test { }
 
}