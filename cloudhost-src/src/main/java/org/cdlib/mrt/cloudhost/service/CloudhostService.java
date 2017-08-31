/******************************************************************************
Copyright (c) 2005-2012, Regents of the University of California
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
*******************************************************************************/

package org.cdlib.mrt.cloudhost.service;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import org.cdlib.mrt.core.FileContent;
import org.cdlib.mrt.cloudhost.action.CloudServiceManager;

import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;




import org.cdlib.mrt.s3.service.NodeIO;
import org.cdlib.mrt.s3.service.NodeService;
import org.cdlib.mrt.s3.cloudhost.CloudhostAddState;
import org.cdlib.mrt.s3.cloudhost.CloudhostDeleteState;
import org.cdlib.mrt.s3.cloudhost.CloudhostFixityState;
import org.cdlib.mrt.s3.cloudhost.CloudhostMetaState;
import org.cdlib.mrt.s3.cloudhost.CloudhostServiceState;
import org.cdlib.mrt.utility.FileUtil;

/**
 * Base properties for Replication
 * @author  dloy
 */

public class CloudhostService
{
    private static final String NAME = "CloudService";
    private static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = true;

    protected int terminationSeconds = 600;
    protected Properties serviceProperties = null;
    protected Properties setupProperties = null;
    //protected long node = 0;
    protected NodeIO nodes = null;
    //protected NodeService service = null;
    protected LoggerInf logger = null;

    public static CloudhostService getCloudhostService(LoggerInf logger, Properties setupProp)
        throws TException
    {
        return new CloudhostService(logger, setupProp);
    }

    protected CloudhostService(LoggerInf logger, Properties setupProp)
        throws TException
    {
        try {
            this.setupProperties = setupProp;
            this.logger = logger;
            if (logger == null) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "missing logger");
            }
            String nodeName = setupProp.getProperty("nodeName");
            if (StringUtil.isEmpty(nodeName)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "missing property: nodeName");
            }
            /*
            String nodeNumber = setupProp.getProperty("nodeNumber");
            if (StringUtil.isEmpty(nodeName)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "missing property: nodeNumber");
            }
            try {
                node = Long.parseLong(nodeNumber);
            } catch (Exception ex) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "non-numeric node:" + nodeNumber);
            }
                    */
            
            nodes = new NodeIO(nodeName, logger);
            nodes.printNodes(NAME);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    protected CloudServiceManager getManager(long node)
        throws TException
    {
        NodeService service = null;
        try {
            service =  NodeService.getNodeService(nodes, node, logger);
            return CloudServiceManager.getCloudServiceManager(service,logger);
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public CloudhostServiceState getServiceState(
            long node)
        throws TException
    {
        try {
            CloudServiceManager manager = getManager(node);
            return manager.getServiceState();
        
        } catch (TException tex) {
            throw tex;
            
        }       
    }
    
    public CloudhostAddState add(
            long node,
            String key,
            String sha256,
            InputStream inStream)
        throws TException
    {
        File tmpFile = null;
        try {
            CloudServiceManager manager = getManager(node);
            tmpFile = FileUtil.getTempFile("tmpcloud", ".txt");
            FileUtil.stream2File(inStream, tmpFile);
            CloudhostAddState responseState = manager.postContent(key, tmpFile, sha256);
            return responseState;
        
        } catch (TException tex) {
            throw tex;
            
        } finally {
            try {
                if (tmpFile != null) tmpFile.delete();
            } catch (Exception ex) { }
        }        
    }
    
    public FileContent getContent(
            long node,
            String key)
        throws TException
    {
        try {
            CloudServiceManager manager = getManager(node);
            return manager.getContent(key);
        
        } catch (TException tex) {
            throw tex;
            
        }       
    }
    
    public InputStream getContentStream(
            long node,
            String key)
        throws TException
    {
        try {
            CloudServiceManager manager = getManager(node);
            return manager.getContentStream(key);
        
        } catch (TException tex) {
            throw tex;
            
        }       
    }
    
    public CloudhostFixityState fixity(
            long node,
            String key,
            String digestType, 
            String digest,
            long length
    )
        throws TException
    {
        try {
            CloudServiceManager manager = getManager(node);
            return manager.fixity(key, digestType, digest, length);
        
        } catch (TException tex) {
            throw tex;
            
        }       
    }
    
    public CloudhostMetaState getMetadata(
            long node,
            String key)
        throws TException
    {
        try {
            CloudServiceManager manager = getManager(node);
            return  manager.getMeta(key);
        
        } catch (TException tex) {
            //tex.printStackTrace();
            throw tex;
            
        }       
    }
    
    public void getMetaExc(
            long node,
            String key)
        throws TException
    {
        CloudServiceManager manager = getManager(node);
        manager.getMetaExc(key);
    }
    
    public String getBucket(long node)
        throws TException
    {
        try {
            CloudServiceManager manager = getManager(node);
            NodeService nodeService =  manager.getService();
            return nodeService.getBucket();
        
        } catch (TException tex) {
            //tex.printStackTrace();
            throw tex;
            
        }       
    }
    
    public CloudhostDeleteState deleteContent(
            long node,
            String key)
        throws TException
    {
        File tmpFile = null;
        try {
            CloudServiceManager manager = getManager(node);
            return manager.deleteContent(key);
        
        } catch (TException tex) {
            throw tex;
            
        } finally {
            try {
                if (tmpFile != null) tmpFile.delete();
            } catch (Exception ex) { }
        }        
    }
    
    public LoggerInf getLogger()
    {
        return logger;
    }
}
