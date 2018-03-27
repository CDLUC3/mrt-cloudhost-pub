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

/**
 * Specific SDSC Storage Cloud handling
 * @author dloy
 */
public class CloudServiceManager
{
    protected static final String NAME = "CloudServiceManager";
    protected static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = false;
    private NodeService service;
    private LoggerInf logger;
    public static CloudServiceManager getCloudServiceManager(
            NodeService service,
            LoggerInf logger)
        throws TException
    {
        return new CloudServiceManager(service, logger);
    }
    
    protected CloudServiceManager(
            final NodeService service,
            final LoggerInf logger)
        throws TException
    {
        this.service = service;
        this.logger = logger;
    }
    
    public NodeService getService()
    {
        return service;
    }

    public FileContent getContent(String key)
        throws TException
    {
        CloudResponse response = new CloudResponse(service.getBucket(), key);
        InputStream stream = service.getObject(key, response);
        responseException(response);
        FileComponent fileComponent = getFileComponent(key);
        FileContent fileContent = FileContent.getFileContent(fileComponent, stream, null);
        return fileContent;
    }

    public InputStream getContentStream(String key)
        throws TException
    {
        CloudResponse response = new CloudResponse(service.getBucket(), key);
        return service.getObject(key, response);
    }

    public CloudhostFixityState fixity(
            String key,
            String digestType, 
            String testDigest, 
            long testLength)
        throws TException
    {
        boolean ok = true;
        CloudResponse response = new CloudResponse(service.getBucket(), key);
        InputStream stream = service.getObject(key, response);
        responseException(response);
        MessageDigestValue data = new MessageDigestValue(stream, digestType, logger);
        if (data.getInputSize() != testLength) {
            ok = false;
        }
        if (!data.getChecksum().equals(testDigest)) {
            ok = false;
        }
        CloudhostFixityState state = new CloudhostFixityState()
            .setBucket(service.getBucket())
            .setKey(key)
            .setTestChecksum(testDigest) 
            .setTestLength(testLength)
            .setDataChecksum(data.getChecksum())
            .setDataLength(data.getInputSize())
            .setChecksumType(data.getChecksumType())
            .setOK(ok);
        return state;
    }

    public CloudhostServiceState getServiceStatus()
        throws TException
    {
        CloudhostServiceState state = new CloudhostServiceState()
                .setBucket(service.getBucket())
                .setNode(service.getNode())
                ;
        return state;
    }

    public CloudhostServiceState getServiceState(long node, Integer forceTest)
        throws TException
    {
        TestCloudhost testCloudHost = TestCloudhost.getTestCloudhost(this, logger);
        if (forceTest != null) testCloudHost.setForceTest(forceTest);
        CloudhostServiceState state = testCloudHost.process("ark:/99999/test|1|prod/test");
        state.setNode(node);
        return state;
    }

    public CloudhostDeleteState deleteContent(String key)
        throws TException
    {
        boolean ok = false;
        String error = null;
        CloudResponse response = service.deleteObject(key);
        if (response.getException() == null) {
            ok = true;
        } else {
            error = response.getException().toString();
        }
        CloudhostDeleteState state = new CloudhostDeleteState()
                .setDeleted(ok)
                .setBucket(service.getBucket())
                .setKey(key)
                .setError(error);
                ;
        return state;
    }

    public CloudhostAddState postContent(String key, File postFile, String testSha256)
        throws TException
    {
        boolean ok = false;
        if (testSha256 != null) {
            FixityTests fixity = new FixityTests(postFile, "SHA-256", logger);
            FixityTests.FixityResult fixityResult 
                    = fixity.validateSizeChecksum(testSha256, "SHA-256", postFile.length());
            if (!fixityResult.checksumMatch) {
                throw new TException.FIXITY_CHECK_FAILS("Post file does not match digest:"
                        + " - test sha256=" + testSha256
                        + " - content sha256=" + fixity.getChecksum()
                );
            }
        }
        CloudResponse response = service.putObject(key, postFile);
        if (response.getException() == null) ok = true;
        CloudhostAddState state = new CloudhostAddState()
                .setAdded(ok)
                .setBucket(service.getBucket())
                .setKey(key)
                ;
        if (response.getException() != null) {
            state.setError(response.getException().toString());
        }
        return state;
    }

    public CloudhostMetaState getMeta(String key)
        throws TException
    {
        try {
        Properties prop = service.getObjectMeta(key);
        if (prop == null) {
            throw new TException.INVALID_OR_MISSING_PARM("Exception occurs getting:" + key);
        }
        if (prop.size() == 0) {
            throw new TException.REQUESTED_ITEM_NOT_FOUND("Key not found:" + key);
        }
        CloudhostMetaState state = new CloudhostMetaState()
                .setBucket(service.getBucket())
                .setKey(key)
                .setProp(prop)
                ;
        return state;
        
        } catch (TException tex) {
            return new CloudhostMetaState()
                .setBucket(service.getBucket())
                .setKey(key)
                .setError(tex.toString());
        }
    }

    public void getMetaExc(String key)
        throws TException
    {
        Properties prop = service.getObjectMeta(key);
        if (prop == null) {
            throw new TException.INVALID_OR_MISSING_PARM("Exception occurs getting:" + key);
        }
        if (prop.size() == 0) {
            throw new TException.REQUESTED_ITEM_NOT_FOUND("Key not found:" + key);
        }
    }
    
    protected FileComponent getFileComponent(String key)
        throws TException
    {
        FileComponent fileComponent = new FileComponent();
        CloudhostMetaState state = getMeta(key);
        Properties prop = state.getProp();
        fileComponent.setIdentifier(key);
        String sizeS = prop.getProperty("size");
        if (sizeS != null) {
            fileComponent.setSize(sizeS);
        }
        String digestType  = prop.getProperty("digesttype");
        String digest = prop.getProperty("digest");
        if ((digestType != null) && (digest != null)) {
            fileComponent.setFirstMessageDigest(digest, digestType);
        }
        return fileComponent;
    }
    
    protected void responseException(CloudResponse response)
        throws TException
    {
        Exception ex = response.getException();
        if (ex == null) return;
        if (DEBUG) ex.printStackTrace();
        if (ex instanceof TException) {
            throw (TException)ex;
        } else {
            if (ex.toString().contains("404")) {
                throw new TException.REQUESTED_ITEM_NOT_FOUND(ex);
            } else {
                throw new TException(ex);
            }
        }
    }
}

