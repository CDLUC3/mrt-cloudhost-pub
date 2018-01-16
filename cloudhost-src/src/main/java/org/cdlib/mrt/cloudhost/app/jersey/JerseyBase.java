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
package org.cdlib.mrt.cloudhost.app.jersey;

import org.glassfish.jersey.server.CloseableService;

import java.io.InputStream;


import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletConfig;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.cdlib.mrt.formatter.FormatterInf;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.cloudhost.app.CloudhostInit;
import org.cdlib.mrt.cloudhost.service.CloudhostService;
import org.cdlib.mrt.s3.cloudhost.CloudhostAddState;
import org.cdlib.mrt.s3.cloudhost.CloudhostDeleteState;
import org.cdlib.mrt.s3.cloudhost.CloudhostFixityState;
import org.cdlib.mrt.s3.cloudhost.CloudhostMetaState;
import org.cdlib.mrt.s3.cloudhost.CloudhostServiceState;

/**
 * Base Jersey handling for both Storage and CAN services
 * The attempt is to keep the Jersey layer as thin as possible.
 * Jersey provides the servlet layer for storage RESTful interface
 * <pre>
 * The Jersey routines typically perform the following functions:
 * - get System configuration
 * - get StorageManager
 * - call appropriate StorageManager method
 * - return file or create formatted file
 * - encapsolate formatted file in Jersey Response - setting appropriate return codes
 * </pre>
 * @author dloy
 */
public class JerseyBase
    extends JerseyUtil
{

    protected static final String NAME = "JerseyBase";
    protected static final String MESSAGE = NAME + ": ";
    protected static final FormatterInf.Format DEFAULT_OUTPUT_FORMAT
            = FormatterInf.Format.xml;
    protected static final boolean DEBUG = false;
    protected static final String NL = System.getProperty("line.separator");

    protected LoggerInf defaultLogger = new TFileLogger("Jersey", 10, 10);
    
    protected JerseyCleanup jerseyCleanup = new JerseyCleanup();
    
    public CloudhostService getService(ServletConfig sc)
        throws TException
    {
        CloudhostInit cloudhostInit = CloudhostInit.getCloudhostInit(sc);
        CloudhostService service = cloudhostInit.getCloudhostService();
        return service;
    }
    
    public Response add(
            long node,
            String key,
            String sha256,
            InputStream inStream, 
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = null;
        if (DEBUG)System.out.println("***JerseyBase add " 
                + " - node:" + node + "\n"
                + " - key:" + key + "\n"
                + " - sha256:" + sha256 + "\n"
        );
        try {
            if ((sha256 != null) && (sha256.length() == 0)) sha256 = null;
            CloudhostService service = getService(sc);
            logger = service.getLogger();
            CloudhostAddState responseState = service.add(node, key, sha256, inStream);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            try {
                return getExceptionResponse(cs, tex, "xml", logger);

            } catch (Exception ex2) {
                throw new TException.GENERAL_EXCEPTION(ex2);
            }

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    public Response fixity(
            long node,
            String key,
            String digestType,
            String digest,
            String lengthS,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = null;
        try {
            CloudhostService service = getService(sc);
            logger = service.getLogger();
            long length = 0;
            if (StringUtil.isAllBlank(lengthS)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "fixity length not supplied");
            }
            try {
                length = Long.parseLong(lengthS);
            } catch (Exception ex) {
                
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "supplied fixity length invalid:" + lengthS);
            }
            CloudhostFixityState responseState = service.fixity(node, key, digestType, digest, length);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            try {
                return getExceptionResponse(cs, tex, "xml", logger);

            } catch (Exception ex2) {
                throw new TException.GENERAL_EXCEPTION(ex2);
            }

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    
    /*
    public Response getContent(
            long node,
            String key,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {

        LoggerInf logger = null;
        try {
            CloudhostService service = getService(sc);
            logger = service.getLogger();
            FileContent content = service.getContent(node, key);
            String formatType = "octet";
            return getFileResponse(content, formatType, key, cs, logger);

        } catch (TException tex) {
            try {
                return getExceptionResponse(cs, tex, "xml", logger);

            } catch (Exception ex2) {
                throw new TException.GENERAL_EXCEPTION(ex2);
            }

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    */
    
    /**
     * Get a specific file for a node-object-version
     * @param nodeID node identifier
     * @param objectIDS object identifier
     * @param versionIDS version identifier
     *   Note that a zero or less versionID is treated as current
     * @param fileID file name
     * @param formatType user provided format type
     * @param sc ServletConfig used to get system configuration
     * @return formatted version state information
     * @throws TException processing exception
     */
    public Response getCloudStream(
            long node,
            String key,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = null;
        try {
            log("getCloudStreamr entered:"
                    + " - key=" + key
                    );
            CloudhostService service = getService(sc);
            logger = service.getLogger();
            service.getMetaExc(node, key);
            System.out.println("after service.getMetadata");
            logger = service.getLogger();;
            
            CloudStreamingOutput streamingOutput = new CloudStreamingOutput(service, node, key);
            String fileResponseName = getFileResponseFileName(key);
            return getFileResponseEntity(streamingOutput, fileResponseName);
            
        } catch (TException tex) {
            try {
                return getExceptionResponse(cs, tex, "xml", logger);
            } catch (TException tex2) {
                throw tex2;
            } catch (Exception ex2) {
                throw new TException.GENERAL_EXCEPTION(ex2);
            }

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    public Response deleteContent(
            long node,
            String key,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {

        LoggerInf logger = null;
        try {
            CloudhostService service = getService(sc);
            logger = service.getLogger();
            CloudhostDeleteState responseState = service.deleteContent(node, key);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            try {
                return getExceptionResponse(cs, tex, "xml", logger);

            } catch (Exception ex2) {
                throw new TException.GENERAL_EXCEPTION(ex2);
            }

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    public Response getMetadata(
            long node,
            String key,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {

        if (DEBUG)System.out.println("***JerseyBase getMetadata " 
                + " - node:" + node + "\n"
                + " - key:" + key + "\n"
                + " - formatType:" + formatType + "\n"
        );
        LoggerInf logger = null;
        try {
            CloudhostService service = getService(sc);
            logger = service.getLogger();
            CloudhostMetaState responseState = service.getMetadata(node, key);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            try {
                return getExceptionResponse(cs, tex, "xml", logger);

            } catch (Exception ex2) {
                throw new TException.GENERAL_EXCEPTION(ex2);
            }

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    public Response getServiceState(
            long node,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {

        LoggerInf logger = null;
        try {
            CloudhostService service = getService(sc);
            logger = service.getLogger();
            CloudhostServiceState responseState = service.getServiceState(node);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            try {
                return getExceptionResponse(cs, tex, "xml", logger);

            } catch (Exception ex2) {
                throw new TException.GENERAL_EXCEPTION(ex2);
            }

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    public static class CloudStreamingOutput
        implements StreamingOutput
    {
        private static final boolean DEBUG = false;
        protected final CloudhostService  service;
        protected final long node;
        protected final String key;
    
        public CloudStreamingOutput(
                CloudhostService  service,
                long node,
                String key)
            throws TException
        {
            
            this.service = service;
            this.node = node;
            this.key = key;
            log("CloudStreamingOutput entered:"
                + " - bucket=" + service.getBucket(node)
                + " - key=" + key);
        }

            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                if (false) throw new RuntimeException("Made it into write");
                InputStream inputStream = null;
                try {
                    log("CloudStreamingOutput write called");
                    inputStream = service.getContentStream(node, key);
                    FileUtil.stream2Stream(inputStream, outputStream);
                    
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                    
                } finally {
                    try {
                        if (inputStream != null) inputStream.close();
                    } catch (Exception ex) { }
                }
            }
    
            protected void log(String msg)
            {
                if (DEBUG) System.out.println("[JerseyStorage]>" + msg);
                //logger.logMessage(msg, 0, true);
            }
    }
}