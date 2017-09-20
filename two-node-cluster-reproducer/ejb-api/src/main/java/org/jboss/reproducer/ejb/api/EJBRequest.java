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

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.reproducer.ejb.api.path.ExpectedPath;
import org.jboss.reproducer.ejb.api.path.InvocationPath;
import org.jboss.reproducer.ejb.api.path.Workflow;

/**
 * @author bmaxwell
 *
 */
@XmlRootElement(name="request-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class EJBRequest implements Serializable {

    private static final long serialVersionUID = -5099964062746921413L;

    @XmlAttribute(name="caller")
    private String caller;

    @XmlAttribute(name="current-action-index")
    private Integer currentActionIndex = 0;

    @XmlAttribute(name="current-workflow-index")
    private Integer currentWorkflowIndex = 0;


    @XmlElement(name="ejb-action")
    private List<EJBAction> actions = new ArrayList<>();

    @XmlElement(name="invocation-path")
    private List<InvocationPath> invocationPath = new ArrayList<>();

    @XmlElement(name="expected-invocation-path")
    private List<ExpectedPath> expectedInvocationPath = new ArrayList<>();

    // new

    @XmlElement(name="workflow")
    private List<Workflow> workflows = new ArrayList<>();

    private transient Context cachedInitialContext = null;
    private transient EJBRemote cachedProxy = null;

    public EJBRequest() {
    }

    public EJBRequest(String caller) {
        this.caller = caller;
//        invocationPath.add(new InvocationPath(nodeName, caller)); // don't track the test client / junit case
    }

//    public List<InvocationPath> getInvocationPath() {
//        return invocationPath;
//    }

    public void addCurrentInvocationPathAndIncrementActionIndex(InvocationPath invocationPath) {
        System.out.printf("CurrentWorkflowIndex: %s InvocationPath: %s\n", currentWorkflowIndex, invocationPath.getService());
        this.workflows.get(currentWorkflowIndex).addCurrentInvocationPathAndIncrementActionIndex(invocationPath);
    }

    public void addCurrentInvocationPathWithNoWorkflows(InvocationPath invocationPath) {
        // create a workflow to hold the invocation paths but only 1
        if(this.workflows.size() == 0) {
            this.addWorkflow();
            this.currentWorkflowIndex = -1; // to prevent EJB from thinking there is a real Workflow
        }
        // this will increment the action which should be ok, even though there is not really a workflow or action
        this.workflows.get(0).addCurrentInvocationPathAndIncrementActionIndex(invocationPath);
    }


    public void addAction(EJBAction action) {
        this.actions.add(action);
    }

    public String getCaller() {
        return caller;
    }

    public List<EJBAction> getActions() {
        return actions;
    }

    public void throwIfAnyExceptions() throws Throwable {
        for(InvocationPath path : invocationPath)
            if(path.getException() != null)
                throw path.getException();
    }

    public String getURLParams() {
        try {
            return String.format("%s=%s", "ejbRequest", URLEncoder.encode(this.marshall(), StandardCharsets.UTF_8.name()));
        } catch(UnsupportedEncodingException | JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public String marshall() throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(EJBRequest.class, EJBAction.class, InvocationPath.class, RemoteEJBConfig.class, EJBInfo.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ctx.createMarshaller().marshal(this, baos);
        return new String(baos.toByteArray());
    }

    public static EJBRequest unmarshall(String string) throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(EJBRequest.class, EJBAction.class, InvocationPath.class, RemoteEJBConfig.class, EJBInfo.class);
        return (EJBRequest) ctx.createUnmarshaller().unmarshal(new StringReader(string));
    }

    public String getResponseInvocationPathByWorkflow() {
        StringBuilder sb = new StringBuilder();
        int i=0, j=0;;
        for(Workflow workflow : this.workflows) {
            sb.append(String.format("Workflow: [%d]\n", j));
            for(InvocationPath path : workflow.getInvocationPath()) {
              // sb.append(String.format("[%03d] - Node: %s callerPrincipal: %s %s\n", i, path.getNodeName(), path.getCallerPrincipal(), path.getService()));
                sb.append(String.format("[%03d] - %s\n", i, path));
              i++;
            }
            j++;
            sb.append(String.format("-------------------------------------\n"));
        }
        return sb.toString();
    }

    public String getResponseInvocationPath() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Caller: %s\n", caller));
        sb.append(String.format("Invocation path:\n"));

        // create full invocation path
        List<InvocationPath> fullInvocationPath = new ArrayList<>();
        for(Workflow workflow : workflows) {
            fullInvocationPath.addAll(workflow.getInvocationPath());
        }

//        for(InvocationPath path : getInvocationPath())
          int i=0;
          for(InvocationPath path : fullInvocationPath) {
//            sb.append(String.format("[%03d] - Node: %s callerPrincipal: %s %s\n", i, path.getNodeName(), path.getCallerPrincipal(), path.getService()));
            sb.append(String.format("[%03d] - %s\n", i, path));
            i++;
          }
        sb.append(String.format("-------------------------------------\n"));
        return sb.toString();
    }

    public List<ExpectedPath> getExpectedInvocationPath() {
        return expectedInvocationPath;
    }

    public static EJBRequest builder() {
        return new EJBRequest();
    }

    // invoke the workflows and invoke their actions
    public EJBRequest invoke() throws Exception {
        // TODO any need to handle if action is <= 0
        // for current workflow / current action invoke
        EJBRequest response = this;
        // ALWAYS invoke on response, do not use this because response is changing after each invocation
        for(int i=0; i<workflows.size(); i++) {
            response.currentWorkflowIndex = i;
            System.out.printf("Running Workflow [%d] - %s\n", i, response.getCurrentWorkflow());
            response.getCurrentWorkflow().resetActionIndex();
            response = response.getCurrentWorkflow().invokeNextAction(response);
        }
        return response;
    }

    public EJBRequest invokeWorkflow(int workflowIndex) throws Exception {
        this.currentWorkflowIndex = workflowIndex;
        Workflow workflow = this.workflows.get(workflowIndex);
//        workflow.resetActionIndex(); // do we need to reset the action index?
        EJBRequest reponse = this;
        for(int i=0; i<workflow.getActions().size(); i++)
            reponse = workflow.getActions().get(i).invoke(reponse);
        return reponse;
    }

    public Workflow getCurrentWorkflow() {
        // allow EJBRequest with no Workflows/Actions
        if(this.workflows.size() == 0 || currentWorkflowIndex < 0)
            return null;
        return this.workflows.get(currentWorkflowIndex);
    }

//    public EJBRequest invokeNextAction() throws Exception {
//        // TODO any need to handle if action is <= 0
//        // for current workflow / current action invoke
//        if (currentWorkflowIndex < this.workflows.size()) {
//            try {
//                return this.workflows.get(currentWorkflowIndex).invokeNextAction(this);
//            } finally {
//                currentWorkflowIndex++;
//            }
//        }
//        return this;
//    }

    public Set<String> getExpectedRoles() {
        return this.getExpectedInvocationPath().get(currentActionIndex).getExpectedRoles();
    }

    public Workflow getWorkflow(int index) {
        return workflows.get(index);
    }

    public EJBRequest addWorkflow(RemoteEJBConfig remoteEJBConfig, TestConfig.EJBS ejbToInvoke) {
        Workflow workflow = new Workflow();

        // add the action which tells it to invoke the ejb
        workflow.getActions().add(new EJBAction(remoteEJBConfig, ejbToInvoke.info));

        // add the expected path which will be used to compare to the InvocationPath after
        workflow.getExpectedInvocationPath().add(new ExpectedPath(remoteEJBConfig.getNodeName(), remoteEJBConfig.getUsername()));

        this.workflows.add(workflow);

        return this;
    }

    public Workflow addWorkflow() {
        Workflow workflow = new Workflow();
        this.workflows.add(workflow);
        return workflow;
    }

    public EJBRequest addPreviouWorkflow(boolean resuseCachedProxy) {
        Workflow previous = this.workflows.get(this.workflows.size()-1);
        addWorkflow().getActions().add(new EJBAction(resuseCachedProxy, previous.getActions().get(previous.getActions().size()-1), null));
        return this;
    }

    public EJBRequest addPreviouWorkflow(boolean resuseCachedProxy, TestConfig.Tx tx) {
        Workflow previous = this.workflows.get(this.workflows.size()-1);
        addWorkflow().getActions().add(new EJBAction(resuseCachedProxy, previous.getActions().get(previous.getActions().size()-1), tx));
        return this;
    }


//    public EJBRequest addWorkflow(Expectations expectations, EJBAction...ejbActions) {
////// when invoking, call getWorkflow() , then invoke the workflow actions until done then invoke the next workflow
//        // add Workflow : call EJB1, call EJB2, expect clustered, expect sticky, expect node/cluster/roles at each path
//        // new Workflow(EJBAction...).setExpectStick().setExpectClustered()
//        // new Workflow().addPath(...).setExpectSticky
//
//        // EJBRequest response = new EJBRequest("testEjbInvocationsInvokeClustered");
//        // RemoteEJBConfig remoteEJBConfig = new RemoteEJBConfig(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.EJBUSER);
//        // response.addPath(remoteEJBConfig, TestConfig.EJBS.CLUSTERED_EJB1);
//
//        // addWorkflow( Simple: RemoteEJBConfig, TestConfig.EJBS )
//        // addWorkflow( Complex: List of Actions, Expectations )
//        // addWorkflow( RepeatedSimple: List of Actions
//        // how to track invocation path per Workflow?  There is a full invocation path and a invocation path per workflow, full is just add all from workflows. So ejb should add Invocationpath to current Workflow
//
//        Workflow workflow = new Workflow(ejbActions);
//        this.getWorkflow().add(workflow);
//
//        this.getExpectedWorkflow().add(new ExpectedWorkflow(expectations), ejbActions);
//        return this;
//    }

    public Context getCachedInitialContext() {
        return cachedInitialContext;
    }

    public void setCachedInitialContext(Context cachedInitialContext) {
        this.cachedInitialContext = cachedInitialContext;
    }

    public EJBRemote getCachedProxy() {
        return cachedProxy;
    }

    public void setCachedProxy(EJBRemote cachedProxy) {
        this.cachedProxy = cachedProxy;
    }

    public String getWorkflowsList() {
        StringBuilder sb = new StringBuilder();
        int i = 0, j=0;
        for(Workflow workflow : this.workflows) {
            sb.append(String.format("Workflow [%d]\n", i));
            for(EJBAction action : workflow.getActions()) {
                sb.append(String.format("- [%d] Action: %s\n", j, action.toString()));
                j++;
            }
            i++;

        }
        return sb.toString();
    }

    public int getWorkflowsSize() {
        return this.workflows.size();
    }

    public EJBRequest addPath(RemoteEJBConfig remoteEJBConfig, TestConfig.EJBS ejbToInvoke) {
//        EJBRequest ejbRequest = new EJBRequest("Standalone Client", TestConfig.CREDENTIAL.NONE.username);
//        RemoteEJBConfig remoteEJBConfig = new RemoteEJBConfig(TestConfig.SERVER.NODE2, TestConfig.CREDENTIAL.EJBUSER);
//        ejbRequest.getActions().add(new EJBAction(remoteEJBConfig, TestConfig.EJBS.CLUSTERED_EJB1_JBOSS_EJB_CLIENT_XML.info));

        // add the action which tells it to invoke the ejb
        this.getActions().add(new EJBAction(remoteEJBConfig, ejbToInvoke.info));

        // add the expected path which will be used to compare to the InvocationPath after
        this.getExpectedInvocationPath().add(new ExpectedPath(remoteEJBConfig.getNodeName(), remoteEJBConfig.getUsername()));

        return this;
    }
}