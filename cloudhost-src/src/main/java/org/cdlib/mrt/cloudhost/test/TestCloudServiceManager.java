/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cdlib.mrt.cloudhost.test;
import java.io.IOException;
import org.cdlib.mrt.s3.service.NodeService;
import org.cdlib.mrt.core.FileComponent;
import org.cdlib.mrt.core.FileContent;
import org.cdlib.mrt.core.MessageDigest;
import org.cdlib.mrt.cloudhost.action.CloudServiceManager;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFileLogger;

import org.cdlib.mrt.s3.cloudhost.CloudhostMetaState;
import org.cdlib.mrt.s3.cloudhost.CloudhostServiceState;
/**
 * 
 *
 * @author replic
 */
public class TestCloudServiceManager 
{
    protected static final String NAME = "TestAWSService";
    protected static final String MESSAGE = NAME + ": ";
    public static void main(String[] args) 
            throws IOException,TException 
    {
        
        LoggerInf logger = new TFileLogger(NAME, 1, 1);
        String nodeName = "nodes-cs-test";
        NodeService service8002 = NodeService.getNodeService(nodeName, 8002, logger);
        CloudServiceManager manager = CloudServiceManager.getCloudServiceManager(service8002,logger);
        String key = "ark:/13030/qt0h89w4n9|1|system/mrt-owner.txt";
        try {
            test(manager, key, logger);
            //test(service9001, key, logger);
            //test(service9001, 9001,  key, logger);
            
        
            
        } catch (Exception ex) {
            System.out.println("TException:" + ex);
            ex.printStackTrace();
            
        }
    }
    
    public static void test(CloudServiceManager manager, String key,LoggerInf logger)
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
