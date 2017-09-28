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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jboss.reproducer.ejb.api.EJBInfo;
import org.jboss.reproducer.ejb.api.ServletInfo;
import org.jboss.reproducer.ejb.api.ThrowableAdapter;

/**
 * @author bmaxwell
 *
 */
@XmlRootElement(name = "invocation-path")
@XmlAccessorType(XmlAccessType.FIELD)
public class InvocationPath implements Serializable {

    private static final long serialVersionUID = 7627944248376385678L;

    @XmlElement(name = "ejb-info")
    private EJBInfo ejbInfo;

    @XmlElement(name = "servlet-info")
    private ServletInfo servletInfo;

    @XmlAttribute(name = "node-name")
    private String nodeName;

    @XmlAttribute(name = "caller-principal")
    private String callerPrincipal;

    @XmlElement(name = "info")
    private String info;

    @XmlAttribute(name = "cluster-name")
    private String clusterName;

    @XmlAttribute(name = "service")
    private String service;

    @XmlAttribute(name = "method")
    private String method;

    @XmlAttribute(name = "caller-address")
    private String callerAddress;

    private Workflow workflow;

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    @XmlElement(name = "transaction-info")
    private TransactionInfo transactionInfo;

    @XmlJavaTypeAdapter(ThrowableAdapter.class)
    @XmlElement(name = "exception")
    private Throwable exception;



    public InvocationPath() {
    }

    public InvocationPath(String nodeName, String callerPrincipal) {
        this.nodeName = nodeName;
        this.callerPrincipal = callerPrincipal;
    }

    public InvocationPath(EJBInfo ejbInfo, String nodeName, String callerPrincipal) {
        this.ejbInfo = ejbInfo;
        this.nodeName = nodeName;
        this.callerPrincipal = callerPrincipal;
    }

    public InvocationPath(ServletInfo servletInfo, String nodeName, String callerPrincipal) {
        this.servletInfo = servletInfo;
        this.nodeName = nodeName;
        this.callerPrincipal = callerPrincipal;
    }

    public InvocationPath(String service, String nodeName, String callerPrincipal) {
        this.service = service;
        this.nodeName = nodeName;
        this.callerPrincipal = callerPrincipal;
    }


    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public EJBInfo getEjbInfo() {
        return ejbInfo;
    }

    public ServletInfo getServletInfo() {
        return servletInfo;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getCallerPrincipal() {
        return callerPrincipal;
    }

    public void setPrincipalName(String principalName) {
        this.callerPrincipal = principalName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public TransactionInfo getTransactionInfo() {
        return transactionInfo;
    }

    public void setTransactionInfo(TransactionInfo transactionInfo) {
        this.transactionInfo = transactionInfo;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getService() {
        if(service != null)
            return service;
        if(ejbInfo != null)
            return "EJB: " + ejbInfo.getEjbName();
        if(servletInfo != null)
            return "Servlet: " + servletInfo.getServletSimpleName() + " - " + servletInfo.getContextRoot();
        return "";
    }

    public String getCallerAddress() {
        return callerAddress;
    }

    public void setCallerAddress(String callerAddress) {
        this.callerAddress = callerAddress;
    }

    @Override
    public String toString() {
        if(callerAddress != null)
            return String.format("Invocation: Caller: %s Node: %s principal: %s Service: %s Method: %s Transaction: %s", getCallerAddress(), getNodeName(), getCallerPrincipal(), getService(), getMethod(), getTransactionInfo());
        return String.format("Invocation: Node: %s principal: %s Service: %s Method: %s Transaction: %s", getNodeName(), getCallerPrincipal(), getService(), getMethod(), getTransactionInfo());
    }
}