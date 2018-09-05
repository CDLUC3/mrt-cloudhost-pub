/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch;

import java.net.MalformedURLException;
import java.util.Properties;
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
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.io.OutputStream;
import org.cdlib.mrt.cloudhost.action.CloudServiceManager;
import org.cdlib.mrt.s3.service.NodeIO;
import org.cdlib.mrt.s3.service.NodeService;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;

public class AppDocker {
 
    private String prefix = null;
    private Properties hostProp = null;
    private String nodeName = null;
    private Integer cloudhostNode = null;
    private LoggerInf logger = null;
    
    public static void main(String[] args) 
        throws Exception 
    {
        String password = null; 
        Integer sslPort = null; 
        Integer standardPort = null;
        Integer cloudhostNode = null;
        System.out.println("***args length:" + args.length);
        if (args.length > 0) password = args[0];
        if (args.length > 1) {
            sslPort = convInt("ssl port# parameter", args[1]);
        }
        if (args.length > 2) {
            standardPort = convInt("standard port# parameter", args[2]);
        }
        if (args.length > 3) {
            cloudhostNode = convInt("pairtree node", args[3]);
        }
        new AppDocker().start(password, sslPort, standardPort, cloudhostNode);
        
        //new AppDocker().start("cdluc3", 30443, 38080, 8002); // legit
        //new AppDocker().start("cdluc3", 30443, 38080, null); // set default
        //new AppDocker().start("cdluc3", 30443, 38080, 9999); // not supported exception
        //new AppDocker().start("xxxxxx", 30443, 38080, null); // error but only on execution
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
 
    public void start(String password, 
            Integer sslPort, 
            Integer standardPort, 
            Integer cloudhostNode) 
            throws ServletException, MalformedURLException, Exception 
    {
        hostProp = getProps("resources/CloudhostServer.properties");
        System.out.println(PropertiesUtil.dumpProperties("CloudServer***", hostProp, 0));
        if (hostProp == null) {
            throw new TException.INVALID_OR_MISSING_PARM("resources/CloudhostServer.properties" + " not supplied");
        }
        nodeName = hostProp.getProperty("nodeName");
        if (StringUtil.isAllBlank(nodeName)) {
            throw new TException.INVALID_OR_MISSING_PARM("nodename not supplied");
        }
        if (!nodeName.equals("nodes-cs-emb")) {
            throw new TException.INVALID_OR_MISSING_PARM("For AppDocker nodeName=nodes-cs-emb is required");
        }
        this.cloudhostNode = cloudhostNode;
        logger = new TFileLogger(null, 0, 0);
        getPrefix();
        
        // test set prefix
        File base = new File(prefix);
        if (!base.exists()) {
            throw new TException.INVALID_OR_MISSING_PARM("Cloudhost base not found:" + prefix);
        }
            
        // set context properties
        hostProp.setProperty("cloudhostNode", "" + this.cloudhostNode);
        hostProp.setProperty("cloudhostPrefix", prefix);
        System.out.println(PropertiesUtil.dumpProperties("AppDocker hostProp", hostProp));
        
        addWebxml();
        addFilecloud();
        addKeystore();
        setJetty(password, sslPort, standardPort);
    }
    
    public void getPrefix()
            throws ServletException, MalformedURLException, Exception
    {
        try {
            if (cloudhostNode == null) {
                prefix = ".";
                cloudhostNode = 8100;
                return;
            }
            NodeIO nodes = new NodeIO(nodeName, logger);
            NodeIO.AccessNode accessNode = nodes.getAccessNode(cloudhostNode);
            if (accessNode == null) {
                throw new TException.INVALID_OR_MISSING_PARM("cloudhostNode not supported:" + cloudhostNode);
            }
            System.out.println("accessNode.serviceType=" + accessNode.serviceType);
            if (!accessNode.serviceType.equals("pairtree")) {
                throw new TException.INVALID_OR_MISSING_PARM("nodename not supplied");
            }
            File localFile = new File(accessNode.container);
            File baseFile = localFile.getParentFile();
            prefix = baseFile.getCanonicalPath();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    protected Properties getProps(String resource)
            throws Exception
    {
        try {
            InputStream instream =  getClass().getClassLoader().
                    getResourceAsStream(resource);
            Properties serverProp = new Properties();
            serverProp.load(instream);
            return serverProp;
            
        } catch (Exception ex) {
            return null;
        }
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
        webAppContext.setResourceBase(prefix + "/cxt");       
        webAppContext.setClassLoader(getClass().getClassLoader());
        if ((hostProp != null) && (cloudhostNode != null)) {
            webAppContext.setAttribute("jettyProp", hostProp);
        }
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
            sslContextFactory.setKeyStorePath(prefix + "/etc/keystore.jks");
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
        Properties jettyProp = (Properties)webAppContext.getAttribute("jettyProp");
        System.out.println(PropertiesUtil.dumpProperties("***jettyProp***", jettyProp));
        
        jettyServer.start();
    }
    
    public void addResource(String resource, File targetFile) 
            throws ServletException, MalformedURLException, Exception 
    {
        InputStream instream =  getClass().getClassLoader().
                getResourceAsStream(resource);
        try {
            FileUtil.stream2File(instream, targetFile);
        } catch(Exception ex) {
            throw new ServletException(ex.toString());
        }
    }
    
    public void storeProperties(Properties prop, File targetFile) 
            throws ServletException, MalformedURLException, Exception 
    {
	OutputStream output = null;
	try {
            output = new FileOutputStream(targetFile);
            prop.store(output, null);

	} catch (Exception ex) {
		throw new TException(ex);
                
	} finally {
            if (output != null) {
                try {
                    output.close();
                } catch (Exception e) { 
                    throw new TException(e);
                }
            }
	}
    }
    
    public void addFilecloud() 
            throws ServletException, MalformedURLException, Exception 
    {
        File fileCloud = new File(prefix + "/fileCloud");
        if (!fileCloud.exists()) {
            fileCloud.mkdir();
            File anchor = new File(fileCloud, "bucket-anchor.txt");
            FileUtil.string2File(anchor, "bucket anchor");
        }
    }
    
    public void addWebxml() 
            throws ServletException, MalformedURLException, Exception 
    {
        String webappDirLocation = prefix + "/cxt/";
        String webinfDirLocation = webappDirLocation + "WEB-INF";
        File webinfDir = new File(webinfDirLocation);
        webinfDir.mkdirs();
        File webxml = new File(webinfDir,"web.xml"); 
        addResource("resources/embedded-web.xml", webxml);
    }
    
    public void addKeystore() throws ServletException, MalformedURLException, Exception {
 
        // Define a folder to hold web application contents.
        //String webappDirLocation = "/apps/replic/MRTMaven/test/embedded/context/";
        String etcLocation = prefix + "/etc/";
        File etcDir = new File(etcLocation);
        etcDir.mkdirs();
        File keystore = new File(etcDir, "keystore.jks");
        addResource("resources/keystore", keystore);
    }
 
}