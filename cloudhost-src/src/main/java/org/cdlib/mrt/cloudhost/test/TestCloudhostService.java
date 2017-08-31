/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cdlib.mrt.cloudhost.test;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import org.cdlib.mrt.core.FileComponent;
import org.cdlib.mrt.core.FileContent;
import org.cdlib.mrt.core.MessageDigest;
import org.cdlib.mrt.cloudhost.action.CloudServiceManager;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFileLogger;


import org.cdlib.mrt.cloudhost.service.CloudhostService;

import org.cdlib.mrt.s3.cloudhost.CloudhostAddState;
import org.cdlib.mrt.s3.cloudhost.CloudhostDeleteState;
import org.cdlib.mrt.s3.cloudhost.CloudhostMetaState;
import org.cdlib.mrt.s3.cloudhost.CloudhostServiceState;
/**
 * 
 *
 * @author replic
 */
public class TestCloudhostService 
{
    protected static final String NAME = "TestCloudhostService";
    protected static final String MESSAGE = NAME + ": ";
    
    
    public static void main(String[] args) 
            throws IOException,TException 
    {
        
        LoggerInf logger = new TFileLogger(NAME, 1, 1);
        String nodeName = "nodes-cs-test";
        Properties setupProp = new Properties();
        setupProp.setProperty("nodeName", "nodes-cs-test");
        long node = 8002;
        CloudhostService service = CloudhostService.getCloudhostService(logger, setupProp);
        String key = "ark:/88888/trythisout|1|producer/test.pdf";
        String fileS = "/apps/replic/test/csh/test.pdf";
        File infile = new File(fileS);
        String outS = "/apps/replic/test/csh/out.pdf";
        File outfile = new File(outS);
        try {
            test(service, node, key, infile, outfile, logger);
            //test(service9001, key, logger);
            //test(service9001, 9001,  key, logger);
            
        
            
        } catch (Exception ex) {
            System.out.println("TException:" + ex);
            ex.printStackTrace();
            
        }
    }
    
    public static void test(CloudhostService service, long node, String key, File infile, File outfile, LoggerInf logger)
            throws IOException,TException 
    {
        try {
            CloudhostServiceState state = service.getServiceState(node);
            System.out.println(state.dump("serviceState"));
            InputStream inStream = new FileInputStream(infile);
            CloudhostAddState addState = service.add(node, key, null, inStream);
            System.out.println(addState.dump("addState"));
            
            CloudhostMetaState propState = service.getMetadata(node, key);
            System.out.println(propState.dump("propState"));
            FileContent content = service.getContent(node, key);
            FileUtil.stream2File(content.getInputStream(), outfile);;
            FileComponent fileComponent = content.getFileComponent();
            String identifier = fileComponent.getIdentifier();
            MessageDigest digest = fileComponent.getMessageDigest();
            long size  = fileComponent.getSize();
            System.out.println("fileComponent:\n"
                    + " - identifier:" + identifier + "\n"
                    + " - digest:" + digest + "\n"
                    + " - size:" + size + "\n"
            );
            
            CloudhostDeleteState deleteState = service.deleteContent(node, key);
            System.out.println(deleteState.dump("deleteState"));
            
            try {
                CloudhostMetaState propState2 = service.getMetadata(node, key);
                System.out.println(propState2.dump("propState2"));
            } catch (Exception ex) {
                System.out.println("Exception propState2:" + ex);
            }
            
            try {
                FileContent content2 = service.getContent(node, key);
                //System.out.println(propState2.dump("propState2"));
            } catch (Exception ex) {
                System.out.println("Exception content2:" + ex);
            }
                    
         } catch (Exception ex) {
            System.out.println("Exception : " + ex);
            
        } 
    }
        
        
    
    public static void build(CloudServiceManager manager, String key,LoggerInf logger)
            throws IOException,TException 
    {
        try {
            CloudhostServiceState state = manager.getServiceState();
            System.out.println(state.dump("serviceState"));
            CloudhostMetaState propState = manager.getMeta(key);
            System.out.println(propState.dump("propState"));
            FileContent content = manager.getContent(key);
            String disp = StringUtil.streamToString(content.getInputStream(), "utf-8");
            FileComponent fileComponent = content.getFileComponent();
            String identifier = fileComponent.getIdentifier();
            MessageDigest digest = fileComponent.getMessageDigest();
            long size  = fileComponent.getSize();
            System.out.println("fileComponent:\n"
                    + " - identifier:" + identifier + "\n"
                    + " - digest:" + digest + "\n"
                    + " - size:" + size + "\n"
            );
            System.out.println("Disp=" + disp);
         } catch (Exception ex) {
            System.out.println("Exception : " + ex);
            
        } 
        
    }
}
