/*
Copyright (c) 2005-2012, Regents of the University of California
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

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
package org.cdlib.mrt.cloudhost.app.jersey.service;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.CloseableService;


import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;


import org.cdlib.mrt.formatter.FormatterInf;
import org.cdlib.mrt.cloudhost.app.jersey.JerseyBase;
import org.cdlib.mrt.utility.TException;

/**
 * Thin Jersey layer for fixity handling
 * @author  David Loy
 */
@Path ("")
public class JerseyCloudhost
        extends JerseyBase
{

    protected static final String NAME = "JerseyCloudhost";
    protected static final String MESSAGE = NAME + ": ";
    protected static final FormatterInf.Format DEFAULT_OUTPUT_FORMAT
            = FormatterInf.Format.xml;
    protected static final boolean DEBUG = true;
    protected static final String NL = System.getProperty("line.separator");
    
    @GET
    @Path("/metadata/{nodeS}/{key}")
    public Response callGetMetadata(
            @PathParam("nodeS") String nodeIDS,
            @PathParam("key") String key,
            @DefaultValue("xhtml") @QueryParam("t") String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        int nodeID = getNodeID(nodeIDS);
        System.out.println("Metadata entered");
        return getMetadata(
            nodeID,
            key,
            formatType,
            cs,
            sc);
    }

    /**
     * Get state information about a specific node
     * @param nodeID node identifier
     * @param formatType user provided format type
     * @param cs on close actions
     * @param sc ServletConfig used to get system configuration
     * @return formatted service information
     * @throws TException
     */
    @GET
    @Path("/state/{nodeid}")
    public Response callGetServiceState(
            @PathParam("nodeid") String nodeIDS,
            @DefaultValue("anvl") @QueryParam("t") String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        int nodeID = getNodeID(nodeIDS);
        System.out.println("State entered");
        return getServiceState(
            nodeID,
            formatType,
            cs,
            sc);
    }
    
    
    @POST
    @Path("/content/{nodeid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response callAddMultipart(
            @PathParam("nodeid") String nodeIDS,
            @DefaultValue("") @FormDataParam("key") String key,
            @DefaultValue("") @FormDataParam("sha256") String sha256,
            @DefaultValue("") @FormDataParam("data") InputStream inStream, 
            @DefaultValue("anvl") @QueryParam("t") String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        if (DEBUG) System.out.println(MESSAGE + "callAddMultipart entered"
                    + " - nodeIDS=" + nodeIDS + NL
                    + " - key=" + key + NL
                    );
        if (DEBUG) System.out.println("addVersionMultipart entered");
        int nodeID = getNodeID(nodeIDS);
        
        return add(
            nodeID,
            key,
            sha256,
            inStream, 
            formatType,
            cs,
            sc);
    }
    
    
    @POST
    @Path("/content/{nodeS}/{key}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response callAdd(
            @PathParam("nodeS") String nodeIDS,
            @PathParam("key") String key,
            @DefaultValue("") @QueryParam("sha256") String sha256,
            @DefaultValue("xhtml") @QueryParam("t") String formatType,
            InputStream inStream,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        if (DEBUG) System.out.println(MESSAGE + "callAddMultipart entered"
                    + " - nodeIDS=" + nodeIDS + NL
                    + " - key=" + key + NL
                    );
        if (DEBUG) System.out.println("addVersionMultipart entered");
        int nodeID = getNodeID(nodeIDS);
        if (inStream == null) {
            throw new TException.INVALID_OR_MISSING_PARM("inStream null");
        }
        return add(
            nodeID,
            key,
            sha256,
            inStream, 
            formatType,
            cs,
            sc);
    }
    
    
    @PUT
    @Path("/fixity/{nodeid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response callFixityMultipart(
            @PathParam("nodeid") String nodeIDS,
            @DefaultValue("") @FormDataParam("key") String key,
            @DefaultValue("") @FormDataParam("digestType") String digestType,
            @DefaultValue("") @FormDataParam("digest") String digest, 
            @DefaultValue("") @FormDataParam("length") String lengthS, 
            @DefaultValue("anvl") @QueryParam("t") String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        if (DEBUG) System.out.println(MESSAGE + "callAddMultipart entered"
                    + " - nodeIDS=" + nodeIDS + NL
                    + " - key=" + key + NL
                    + " - digestType=" + digestType + NL
                    + " - digest=" + digest + NL
                    + " - lengthS=" + lengthS + NL
                    );
        if (DEBUG) System.out.println("addVersionMultipart entered");
        int nodeID = getNodeID(nodeIDS);
        
        return fixity(
            nodeID,
            key,
            digestType,
            digest,
            lengthS,
            formatType,
            cs,
            sc);
    }
    
    @GET
    @Path("/fixity/{nodeid}/{key}")
    public Response callFixityQuery(
            @PathParam("nodeid") String nodeIDS,
            @PathParam("key") String key,
            @DefaultValue("") @QueryParam("digestType") String digestType,
            @DefaultValue("") @QueryParam("digest") String digest, 
            @DefaultValue("") @QueryParam("length") String lengthS, 
            @DefaultValue("anvl") @QueryParam("t") String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        if (DEBUG) System.out.println(MESSAGE + "callAddMultipart entered"
                    + " - nodeIDS=" + nodeIDS + NL
                    + " - key=" + key + NL
                    + " - digestType=" + digestType + NL
                    + " - digest=" + digest + NL
                    + " - lengthS=" + lengthS + NL
                    );
        if (DEBUG) System.out.println("addVersionMultipart entered");
        int nodeID = getNodeID(nodeIDS);
        
        return fixity(
            nodeID,
            key,
            digestType,
            digest,
            lengthS,
            formatType,
            cs,
            sc);
    }

    @GET
    @Path("/data/{nodeid}/{key}")
    public Response callGetContent(
            @PathParam("nodeid") String nodeIDS,
            @PathParam("key") String key,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        int nodeID = getNodeID(nodeIDS);
        System.out.println("Entered callGetContent");
        return getCloudStream(
            nodeID,
            key,
            cs,
            sc);
    } 
    
    @DELETE
    @Path("/delete/{nodeS}/{key}")
    public Response callDelete(
            @PathParam("nodeS") String nodeIDS,
            @PathParam("key") String key,
            @DefaultValue("xhtml") @QueryParam("t") String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        int nodeID = getNodeID(nodeIDS);
        return deleteContent(
            nodeID,
            key,
            formatType,
            cs,
            sc);
    }

}
