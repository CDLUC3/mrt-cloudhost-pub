/*
Copyright (c) 2005-2010, Regents of the University of California
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
 *
- Redistributions of source code must retain the above copyright notice,
  this list of conditions and the following disclaimer.
- Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
- Neither the name of the University of California nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.
*********************************************************************/
package org.cdlib.mrt.cloudhost.action;
//import org.cdlib.mrt.s3.service.*;



import org.cdlib.mrt.s3.service.NodeService;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import org.cdlib.mrt.core.FileComponent;
import org.cdlib.mrt.core.FileContent;
import org.cdlib.mrt.utility.MessageDigestValue;


import org.cdlib.mrt.utility.FixityTests;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.s3.cloudhost.CloudhostAddState;
import org.cdlib.mrt.s3.cloudhost.CloudhostDeleteState;
import org.cdlib.mrt.s3.cloudhost.CloudhostFixityState;
import org.cdlib.mrt.s3.cloudhost.CloudhostMetaState;
import org.cdlib.mrt.s3.cloudhost.CloudhostServiceState;

import org.cdlib.mrt.s3.service.CloudResponse;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;

/**
 * Specific SDSC Storage Cloud handling
 * @author dloy
 */
public class TestCloudhost
{
    protected static final String NAME = "CloudServiceManager";
    protected static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = false;
    
    private static final String testS = "this is a test file\n";
    private final CloudServiceManager manager;
    private final String bucket;
    private final LoggerInf logger;
    
    protected CloudhostMetaState initialMeta = null;
    protected CloudhostAddState addState = null;
    protected CloudhostMetaState addMeta = null;
    protected FileContent fileContent = null;
    protected CloudhostDeleteState deleteState = null;
    
    protected CloudhostServiceState retState = new CloudhostServiceState();
    protected String error = null;
    private int forceTest = 0;
    
    public static TestCloudhost getTestCloudhost(
            CloudServiceManager manager,
            LoggerInf logger)
        throws TException
    {
        return new TestCloudhost(manager, logger);
    }
    
    protected TestCloudhost(
            final CloudServiceManager manager,
            final LoggerInf logger)
        throws TException
    {
        this.manager = manager;
        this.logger = logger;
        NodeService nodeService = manager.getService();
        bucket = nodeService.getBucket();
        retState.setBucket(bucket);
        
    }

    public CloudhostServiceState process(String key)
        throws TException
    {
        if (!initialMeta(key)) {
            return retState;
        }
        if (!add(key)) {
            return retState;
        }
        if (DEBUG) meta(key);
        if (!content(key)) {
            return retState;
        }
        if (forceTest == 5) {
            retState.setOk(false);
            return retState;
        }
        if (!delete(key)) {
            return retState;
        }
        retState.setOk(true);
        return retState;
    }

    protected boolean setError(String retError)
    {
        retState.setError(retError);
        System.out.println(retError);
        retState.setOk(false);
        return false;
    }
    
    protected boolean initialMeta(String key)
        throws TException
    {
        initialMeta = manager.getMeta(key);
        String metaError = initialMeta.getError();
        try {
            if (metaError != null) {
                if (metaError.contains("REQUESTED_ITEM_NOT_FOUND")) {
                    if (DEBUG) System.out.println("initialMeta: No initial delete file");
                } else {
                    error = "Initial Meta fail - Exception on meta request"
                            + " - bucket:" + bucket
                            + " - key:" + key
                            + " - error:" + metaError
                            ;
                    return setError(error);
                }
            } else {
                CloudhostDeleteState cds = manager.deleteContent(key);
                if (forceTest == 1) {
                    cds.setDeleted(false);
                }

                if (!cds.isDeleted()) {
                    error = "Initial Delete - Unable to delete content"
                            + " - bucket:" + bucket
                            + " - key:" + key
                            + " - error:" + cds.getError()
                    ;
                    return setError(error);
                }
            }
            return true;
            
        } catch (Exception ex) {
            error = "InitialMeta Catch Error - Unable to add content"
                        + " - bucket:" + bucket
                        + " - key:" + key
                        + " - error:" + ex.toString();
            return setError(error);
                
        }
    }
    
    protected boolean meta(String key)
        throws TException
    {
        addMeta = manager.getMeta(key);
        String metaError = initialMeta.getError();
        try {
            if (metaError != null) {
                if (metaError.contains("REQUESTED_ITEM_NOT_FOUND")) {
                    System.out.println("initialMeta: No initial delete file");
                } else {
                    error = "Meta fail - Exception on meta request"
                            + " - bucket:" + bucket
                            + " - key:" + key
                            + " - error:" + metaError
                            ;
                    return setError(error);
                }
            }
            System.out.println(PropertiesUtil.dumpProperties("meta output", addMeta.getProp()));
            return true;
            
        } catch (Exception ex) {
            error = "Meta Catch Error - Unable to add content"
                        + " - bucket:" + bucket
                        + " - key:" + key
                        + " - error:" + ex.toString();
            return setError(error);
                
        }
    }
        
    protected boolean add(String key) 
        throws TException
    {
        File testF = FileUtil.getTempFile("test", ".txt");
        try {
            FileUtil.string2File(testF, testS);
            addState = manager.postContent(key, testF, null);
            if (forceTest == 2) {
                addState.setAdded(false);
                addState.setError("forcetest add error");
            }
            if (addState.isAdded()) {
                return true;
            }
            String addError = addState.getError();
            if (addError != null) {
                error = "Add Error - Unable to delete content"
                        + " - bucket:" + bucket
                        + " - key:" + key
                        + " - error:" + addError
                ;
                return setError(error);
            }
            if (DEBUG) System.out.println(addState.dump("add"));
            return true;
            
        } catch (Exception ex) {
            error = "Add Catch Error - Unable to add content"
                        + " - bucket:" + bucket
                        + " - key:" + key
                        + " - error:" + ex.toString();
            return setError(error);
                
        } finally {
            try {
                testF.delete();
            } catch(Exception ex) { }
        }
    }
        
    protected boolean content(String key) 
        throws TException
    {
        try {
            fileContent = manager.getContent(key);
            InputStream inStream = fileContent.getInputStream();
            String retTest = StringUtil.streamToString(inStream, "utf8");
            if (forceTest == 3) {
                retTest = "forcetest error";
            }
            if (!retTest.equals(testS)) {
                error = "Content Error - failed content match"
                        + " - bucket:" + bucket
                        + " - key:" + key
                        + " - testS:" + testS
                        + " - retTest:" + retTest
                ;
                return setError(error);
            }
            FileComponent fileComponent = fileContent.getFileComponent();
            if (DEBUG) System.out.println(fileComponent.dump("content"));
            return true;
            
        } catch (Exception ex) {
            error = "Content Catch Error - Unable to add content"
                        + " - bucket:" + bucket
                        + " - key:" + key
                        + " - error:" + ex.toString();
            return setError(error);
                
        }
    }

    protected boolean delete(String key)
        throws TException
    {
        CloudhostMetaState meta = manager.getMeta(key);
        String metaError = meta.getError();
        try {
            if (metaError != null) {
                if (metaError.contains("REQUESTED_ITEM_NOT_FOUND")) {
                    error = "Delete file not found:"
                            + " - bucket:" + bucket
                            + " - key:" + key
                            + " - error:" + metaError
                            ;
                } else {
                    error = "Delete Meta fail - Exception on meta request"
                            + " - bucket:" + bucket
                            + " - key:" + key
                            + " - error:" + metaError
                            ;
                }
                return setError(error);
                
            } else {
                deleteState = manager.deleteContent(key);

                if (forceTest == 4) {
                    deleteState.setDeleted(false);
                }
                if (!deleteState.isDeleted()) {
                    error = "Delete - Unable to delete content"
                            + " - bucket:" + bucket
                            + " - key:" + key
                            + " - error:" + deleteState.getError()
                    ;
                    return setError(error);
                }
            }
            if (DEBUG) System.out.println(deleteState.dump("dump"));
            return true;
            
        } catch (Exception ex) {
            error = "Delete Catch Error - Unable to add content"
                        + " - bucket:" + bucket
                        + " - key:" + key
                        + " - error:" + ex.toString();
            return setError(error);
                
        }
    }

    public TestCloudhost setForceTest(int forceTest) {
        this.forceTest = forceTest;
        System.out.println("***FORCETEST=" + forceTest);
        return this;
    }
}

