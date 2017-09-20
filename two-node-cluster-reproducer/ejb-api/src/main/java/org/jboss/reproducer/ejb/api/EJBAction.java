/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.reproducer.ejb.api;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jboss.logging.Logger;

/**
 * @author bmaxwell
 *
 */
@XmlRootElement(name="ejb-action")
@XmlAccessorType(XmlAccessType.FIELD)
public class EJBAction implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2752875206431283834L;

    @XmlTransient
    private static Logger log = Logger.getLogger(EJBAction.class.getName());

    @XmlElement(name="remote-ejb-config")
    private RemoteEJBConfig remoteEJBConfig;

    @XmlElement(name="ejb-info")
    private EJBInfo ejbInfo;

    @XmlElement(name="expected-roles")
    private Set<String> expectedRoles = new HashSet<>();

    @XmlAttribute(name="resuse-cached-initial-context-if-available")
    private boolean resuseCachedInitialContextIfAvailable = Boolean.TRUE;

    @XmlAttribute(name="resuse-cached-proxy-if-available")
    private boolean reuseCachedProxy = Boolean.FALSE;

    @XmlAttribute(name="tx")
    private TestConfig.Tx tx = TestConfig.Tx.REQUIRES_NEW;


    public EJBAction() {
    }

    public EJBAction(RemoteEJBConfig remoteEJBConfig, EJBInfo ejbInfo, String...expectedRolesArray) {
        this(false, remoteEJBConfig, ejbInfo, expectedRolesArray);
    }

    public EJBAction(boolean reuseCachedProxy, EJBAction action, TestConfig.Tx tx) {
        this(true, action.getRemoteEJBConfig(), action.getEjbInfo(), tx, action.getExpectedRoles().toArray(new String[action.getExpectedRoles().size()]));
        System.out.println("Brad: adding action with reuseCachedProxy: "+ reuseCachedProxy );
        this.reuseCachedProxy = reuseCachedProxy;
    }

    public EJBAction(boolean resuseCachedInitialContextIfAvailable, RemoteEJBConfig remoteEJBConfig, EJBInfo ejbInfo, String...expectedRolesArray) {
        this(resuseCachedInitialContextIfAvailable, remoteEJBConfig, ejbInfo, TestConfig.Tx.REQUIRES_NEW, expectedRolesArray);
    }

    public EJBAction(boolean resuseCachedInitialContextIfAvailable, RemoteEJBConfig remoteEJBConfig, EJBInfo ejbInfo, TestConfig.Tx tx, String...expectedRolesArray) {
        System.out.println("Brad: adding action with resuseCachedInitialContextIfAvailable: "+ resuseCachedInitialContextIfAvailable );
        this.resuseCachedInitialContextIfAvailable = resuseCachedInitialContextIfAvailable;
        this.remoteEJBConfig = remoteEJBConfig;
        this.ejbInfo = ejbInfo;
        if(tx != null) this.tx = tx;
        if(expectedRolesArray != null) {
            this.expectedRoles = new HashSet<>();
            this.expectedRoles.addAll(Arrays.asList(expectedRolesArray));
        }
    }


//    public EJBAction(ServerConfig serverConfig, Credentials credentials, EJBInfo ejbInfo) {
//        this.remoteEJBConfig = new RemoteEJBConfig(serverConfig, credentials);
//        this.ejbInfo = ejbInfo;
//        if(expectedRoles != null)
//            this.expectedRoles.addAll(Arrays.asList(credentials.getRoles()));
//    }

    public EJBRequest invoke(EJBRequest ejbRequest) throws Exception {
        EJBRemote remoteEJB = null;
        String lookup = "";
        Context ctx = null;

        if(reuseCachedProxy) {
            ctx = ejbRequest.getCachedInitialContext();
            remoteEJB = ejbRequest.getCachedProxy();
        }

        // Use the cached initial context if it is not null
        // this is only useful when trying to invoke several methods from the same context
        if(resuseCachedInitialContextIfAvailable && ejbRequest.getCachedInitialContext() != null) {
            ctx = ejbRequest.getCachedInitialContext();
            System.out.println("Using Cached InitialContext"); System.out.flush();
        }
        System.out.println("resuseCachedInitialContextIfAvailable: " + resuseCachedInitialContextIfAvailable); System.out.flush();

        if (remoteEJBConfig.isJbossEjbClientXml()) {
            if(ctx == null)
                ctx = new InitialContext(remoteEJBConfig.getEnvironment());
            lookup = ejbInfo.getRemoteLookupPath();
        } else if (remoteEJBConfig.isRemote()) {
            log.debug("Invoking remote: " + remoteEJBConfig);
            if (remoteEJBConfig.getUsername() == null) {
                if(ctx == null)
                    ctx = EJBUtil.getWildflyInitialContext(remoteEJBConfig.getHost(), remoteEJBConfig.getPort(), null, null, remoteEJBConfig.getEnvironment());
            } else {
                if(ctx == null)
                    ctx = EJBUtil.getWildflyInitialContext(remoteEJBConfig.getHost(), remoteEJBConfig.getPort(),
                        remoteEJBConfig.getUsername(), remoteEJBConfig.getPassword(), remoteEJBConfig.getEnvironment());
            }
            lookup = ejbInfo.getRemoteLookupPath();
        } else {
            log.debug("Invoking InVM remote interface: " + remoteEJBConfig);
            if(ctx == null)
                ctx = new InitialContext(remoteEJBConfig.getEnvironment());
            lookup = ejbInfo.getInVmGlobalLookupPath();
        }


        log.info("Lookup: " + lookup + " using context: " + System.identityHashCode(ctx) + " -> " + ctx);
        System.out.println("Lookup: " + lookup + " using context: " + System.identityHashCode(ctx)  + " -> " + ctx + " Thread: " + Thread.currentThread().getName()); System.out.flush();
        if(remoteEJB == null)
            remoteEJB = (EJBRemote) ctx.lookup(lookup);
        log.info("Invoking with: " + ejbRequest);
        EJBRequest response = ejbRequest;
        try {
            response = invoke(remoteEJB, ejbRequest, tx);
            response.setCachedInitialContext(ctx);
            response.setCachedProxy(remoteEJB);
            return response;
        } finally {
            // cache the context for possible resuse
            response.setCachedInitialContext(ctx);
            response.setCachedProxy(remoteEJB);
        }
    }

    private EJBRequest invoke(EJBRemote remoteEJB, EJBRequest request, TestConfig.Tx tx) {
        switch(tx) {
            case MANDATORY:
                return remoteEJB.mandatory(request);
            case NEVER:
                return remoteEJB.never(request);
            case NOT_SUPPORTED:
                return remoteEJB.notSupported(request);
            case REQUIRED:
                return remoteEJB.required(request);
            case REQUIRES_NEW:
                return remoteEJB.requiresNew(request);
            case SUPPORTS:
                return remoteEJB.supports(request);
            default:
                return remoteEJB.invoke(request);
        }
    }

    public EJBInfo getEjbInfo() {
        return ejbInfo;
    }

    public RemoteEJBConfig getRemoteEJBConfig() {
        return remoteEJBConfig;
    }

    public Set<String> getExpectedRoles() {
        return expectedRoles;
    }

//    public void setResuseCachedInitialContextIfAvailable(boolean resuseCachedInitialContextIfAvailable) {
//        this.resuseCachedInitialContextIfAvailable = resuseCachedInitialContextIfAvailable;
//    }

    @Override
    public String toString() {
        String remoteEJBConfig = this.remoteEJBConfig == null ? "" : this.remoteEJBConfig.toString();
//        return String.format("EJBAction invoke %s using %s", ejbInfo.getRemoteLookupPath(), remoteEJBConfig.toString());
        return String.format("EJBAction invoke %s using %s", ejbInfo.getRemoteLookupPath(), remoteEJBConfig);
    }
}