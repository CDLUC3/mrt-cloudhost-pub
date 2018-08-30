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
**********************************************************/

package org.cdlib.mrt.cloudhost.app;


import org.cdlib.mrt.utility.TFrameInit;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.cdlib.mrt.cloudhost.service.CloudhostService;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.TException;
/**
 * Initialize handling for storage service in servlet
 * @author dloy
 */
public class CloudhostInit
        extends TFrameInit
{
    private enum Type {
        Regular, Default
    }
    protected CloudhostService cloudhostService = null;

    /**
     * Get resolved storage service
     * @return
     */
    public CloudhostService getCloudhostService() {
        return cloudhostService;
    }

    /**
     * Factory: StorageServiceInit
     * @param servletConfig servlet configuration object
     * @return StorageServiceInit
     * @throws TException
     */
    public static synchronized CloudhostInit getCloudhostInit(
            ServletConfig servletConfig)
            throws TException
    {
        String serviceName = "cloudService";
        ServletContext servletContext = servletConfig.getServletContext();
        Properties jettyProp = (Properties)servletContext.getAttribute("jettyProp");
        if (jettyProp == null) System.out.println("***jettyProp not found");
        CloudhostInit cloudServiceInit  = (CloudhostInit)servletContext.getAttribute(serviceName);
        if (cloudServiceInit == null) {
            cloudServiceInit = new CloudhostInit(Type.Regular, servletConfig, serviceName, jettyProp);
            servletContext.setAttribute(serviceName, cloudServiceInit);
        }
        return cloudServiceInit;
    }

    /**
     * Factory: StorageServiceInit
     * @param servletConfig servlet configuration object
     * @return StorageServiceInit
     * @throws TException
     */
    public static synchronized CloudhostInit getCloudhostInitDefault(
            ServletConfig servletConfig)
            throws TException
    {
        return getCloudhostInit(servletConfig);
    }

    /**
     * Constructor
     * @param servletConfig servlet configuration
     * @param serviceName service name for logging and for persistence
     * @throws TException
     */
    protected CloudhostInit(Type type, ServletConfig servletConfig, String serviceName, Properties jettyProp)
            throws TException
    {
        super(servletConfig, serviceName);
        Properties resourceProp = tFrame.getProperties();
        if (jettyProp != null) {
            resourceProp.putAll(jettyProp);
            System.out.println(PropertiesUtil.dumpProperties("***" + NAME + "***", jettyProp));
        }
        if (type == Type.Regular) {
            cloudhostService = CloudhostService.getCloudhostService(
                    tFrame.getLogger(), resourceProp);
        }
        if (type == Type.Default) {
            cloudhostService = CloudhostService.getCloudhostService(
                    tFrame.getLogger(), resourceProp);
        }
    }
}
