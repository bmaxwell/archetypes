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

package org.jboss.reproducer.ejb.api.path;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlTransient;

import org.jboss.logging.Logger;
import org.jboss.reproducer.ejb.api.EJBInfo;
import org.jboss.reproducer.ejb.api.EJBRemote;
import org.jboss.reproducer.ejb.api.EJBRemoteConfig;
import org.jboss.reproducer.ejb.api.EJBRemoteNamingConfig;
import org.jboss.reproducer.ejb.api.EJBRemoteScopedContextConfig;
import org.jboss.reproducer.ejb.api.EJBRequest;
import org.jboss.reproducer.ejb.api.TestConfig;

/**
 * @author bmaxwell
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class EJBAction extends AbstractAction implements Action, Serializable, Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = 2752875206431283834L;

    @XmlTransient
    private static Logger log = Logger.getLogger(EJBAction.class.getName());

    @XmlElement(name="remote-ejb-config")
    @XmlElementRefs({
        @XmlElementRef(type=EJBRemoteNamingConfig.class),
        @XmlElementRef(type=EJBRemoteScopedContextConfig.class)
      })
    private EJBRemoteConfig remoteEJBConfig;

    @XmlElement(name="ejb-info")
    private EJBInfo ejbInfo;

    @XmlElement(name="expected-roles")
    protected Set<String> expectedRoles = new HashSet<>();

    @XmlAttribute(name="resuse-cached-initial-context-if-available")
    private boolean resuseCachedContext = Boolean.TRUE;

    @XmlAttribute(name="resuse-cached-proxy-if-available")
    private boolean reuseCachedProxy = Boolean.FALSE;

    @XmlAttribute(name="tx")
    private TestConfig.Tx tx = TestConfig.Tx.REQUIRES_NEW;

    // *** Adding workflow to an action **
    @XmlElement(name="workflow")
    private List<Workflow> workflows = new ArrayList<>();

    // *** Adding workflow to an action **

    public EJBAction() {
    }

    public EJBAction(EJBRemoteConfig remoteEJBConfig, EJBInfo ejbInfo, String...expectedRolesArray) {
        this(false, remoteEJBConfig, ejbInfo, expectedRolesArray);
    }

    // TODO this is unconfirmed after modifying things
    public EJBAction(boolean reuseCachedProxy, Action action, TestConfig.Tx tx) {
        this.reuseCachedProxy = reuseCachedProxy;
        this.resuseCachedContext = true;
//        this.remoteEJBConfig = remoteEJBConfig;
//        this.ejbInfo = ejbInfo;
        if(tx != null) this.tx = tx;
//        if(expectedRolesArray != null) {
//            this.expectedRoles = new HashSet<>();
//            this.expectedRoles.addAll(Arrays.asList(expectedRolesArray));
//        }

    }

    public EJBAction(boolean resuseCachedInitialContextIfAvailable, EJBRemoteConfig remoteEJBConfig, EJBInfo ejbInfo, String...expectedRolesArray) {
        this(resuseCachedInitialContextIfAvailable, remoteEJBConfig, ejbInfo, TestConfig.Tx.REQUIRES_NEW, expectedRolesArray);
    }

    public EJBAction(boolean resuseCachedInitialContextIfAvailable, EJBRemoteConfig remoteEJBConfig, EJBInfo ejbInfo, TestConfig.Tx tx, String...expectedRolesArray) {
        this.resuseCachedContext = resuseCachedInitialContextIfAvailable;
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


    @Override
    public EJBRequest invoke(EJBRequest ejbRequest) throws Exception {

        EJBRemote remoteEJB = null;
        String lookup = "";
        Context ctx = null;

        if(reuseCachedProxy) {
            ctx = ejbRequest.getCachedInitialContext();
            remoteEJB = ejbRequest.getCachedProxy();
            // we are being run from the workflow, we need to compare to last ejbAction like ejbRquest.getCurrentWorkflow().getLastEJBAction()
//            if(remoteEJB != null && remoteEJBConfig == same && ejbInfo.getRemoteLookupPath == same) // TODO TODO
            if(remoteEJB != null) {
                log.debug("EJBProxy was cached, using it");
            }
        }

        // Use the cached initial context if it is not null
        // this is only useful when trying to invoke several methods from the same context
        if(resuseCachedContext && ejbRequest.getCachedInitialContext() != null) {
            ctx = ejbRequest.getCachedInitialContext();
        }

        if(ctx == null)
            ctx = remoteEJBConfig.getInitialContext();

        switch(remoteEJBConfig.getConfigType()) {
            case REMOTE_NAMING:
                lookup = ejbInfo.getRemoteNamingLookupPath();
            break;
            case WILDFLY_NAMING:
                lookup = ejbInfo.getEJBClientLookupPath();
            break;
            case SCOPED_CONTEXT:
                lookup = ejbInfo.getEJBClientLookupPath();
            break;
            case REMOTING_EJB_RECEIVER:
                lookup = ejbInfo.getRemoteLookupPath();
            break;
            case IN_VM:
                lookup = ejbInfo.getInVmGlobalLookupPath();
            break;
        }

        log.debug("Lookup: " + lookup + " using context: " + System.identityHashCode(ctx) + " -> " + ctx);
        if(remoteEJB == null)
            remoteEJB = (EJBRemote) ctx.lookup(lookup);
        log.debug("Invoking with: " + ejbRequest);

        EJBRequest response = ejbRequest;
        try {
            // set the callingNodeName
            ejbRequest.setCallingNode(System.getProperty("jboss.node.name"));
//            ejbRequest = super.invoke(ejbRequest); // this is not working or some reason

            response = invoke(remoteEJB, ejbRequest, tx);
            response.setCachedInitialContext(ctx);
            response.setCachedProxy(remoteEJB);
            return response;
        } finally {
            switch(remoteEJBConfig.getConfigType()) {
                case REMOTE_NAMING:
                case SCOPED_CONTEXT:
                    remoteEJBConfig.close(ctx);
                break;
                default:
                    // cache the context for possible resuse
                    response.setCachedInitialContext(ctx);
                    response.setCachedProxy(remoteEJB);
                break;
            }
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

    public EJBRemoteConfig getRemoteEJBConfig() {
        return remoteEJBConfig;
    }

    public Set<String> getExpectedRoles() {
        return expectedRoles;
    }

    @Override
    public boolean isWorkflowAction() {
        return false;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        // TODO
        return super.clone();
    }

    public EJBAction setResuseCachedInitialContextIfAvailable(boolean resuseCachedInitialContextIfAvailable) {
        this.resuseCachedContext = resuseCachedInitialContextIfAvailable;
        return this;
    }

    public EJBAction setReuseCachedProxy(boolean reuseCachedProxy) {
        this.reuseCachedProxy = reuseCachedProxy;
        return this;
    }

    public TestConfig.Tx getTx() {
        return tx;
    }

    public void setTx(TestConfig.Tx tx) {
        this.tx = tx;
    }

    @Override
    public String toString() {
        String remoteEJBConfig = this.remoteEJBConfig == null ? "" : this.remoteEJBConfig.toString();
        return String.format("EJBAction invoke %s using %s", ejbInfo.getRemoteLookupPath(), remoteEJBConfig);
    }

    public static EJBAction build() {
        return new EJBAction();
    }
    public static EJBAction build(EJBRemoteConfig remoteEJBConfig, EJBInfo ejbInfo) {
        return build().setRemoteEJBConfig(remoteEJBConfig).setEjbToInvoke(ejbInfo);
    }

    public EJBAction setRemoteEJBConfig(EJBRemoteConfig remoteEJBConfig) {
        this.remoteEJBConfig = remoteEJBConfig;
        return this;
    }
    public EJBAction setExpectedRoles(String...expectedRoles) {
        if(expectedRoles != null)
            this.expectedRoles.addAll(Arrays.asList(expectedRoles));
        return this;
    }
    public EJBAction setEjbToInvoke(EJBInfo ejbInfo) {
        this.ejbInfo = ejbInfo;
        return this;
    }
}