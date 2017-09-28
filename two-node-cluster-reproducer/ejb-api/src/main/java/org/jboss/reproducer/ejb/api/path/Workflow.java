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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.logging.Logger;
import org.jboss.reproducer.ejb.api.EJBRemoteConfig;
import org.jboss.reproducer.ejb.api.EJBRequest;
import org.jboss.reproducer.ejb.api.TestConfig;

/**
 * @author bmaxwell
 *
 */
@XmlRootElement(name="workflow")
@XmlAccessorType(XmlAccessType.FIELD)
public class Workflow implements Serializable {

    private static final long serialVersionUID = 2328222418770622921L;

    @XmlAttribute(name="name")
    private String name;

    @XmlElement(name="ejb-action")
    protected List<Action> actions = new ArrayList<>();

    @XmlElement(name="invocation-path")
    private List<InvocationPath> invocationPath = new ArrayList<>();

    @XmlElement(name="expected-invocation-path")
    private List<ExpectedPath> expectedInvocationPath = new ArrayList<>();

    @XmlAttribute(name="current-action-index")
    private Integer currentActionIndex = 0;

    @XmlElement(name="expectations")
    private Set<Expectation> expectations = new HashSet<>();

    @XmlAttribute(name="current-workflow-action-index")
    private Integer currentWorkflowActionIndex = -1;

    protected Logger log = Logger.getLogger(this.getClass().getName());

    public Workflow() {
    }

    public Workflow(String name) {
        this.name = name;
    }


    public Workflow(Expectation... expectations) {
        this.expectations.addAll(Arrays.asList(expectations));
    }

    public Workflow(EJBAction...actions) {
        for(EJBAction action : actions)
            this.actions.add(action);
    }

    public List<Action> getActions() {
        return actions;
    }

//    public Workflow addAction(RemoteEJBConfig remoteEJBConfig, TestConfig.EJBS ejbToInvoke, String...expectedRoles) {
//        this.actions.add(new EJBAction(remoteEJBConfig, ejbToInvoke.info, expectedRoles));
//        return this;
//    }

    public Workflow addAction(Action action) {
        this.actions.add(action);
        return this;
    }

    public Workflow addAction(EJBRemoteConfig remoteEJBConfig, TestConfig.EJBS ejbToInvoke, String...expectedRoles) {
        this.actions.add(new EJBAction(remoteEJBConfig, ejbToInvoke.info, expectedRoles));
        return this;
    }

    public Workflow addAction(boolean resuseCachedInitialContextIfAvailable, EJBRemoteConfig remoteEJBConfig, TestConfig.EJBS ejbToInvoke, String...expectedRoles) {
        this.actions.add(new EJBAction(resuseCachedInitialContextIfAvailable, remoteEJBConfig, ejbToInvoke.info, expectedRoles));
        return this;
    }

    public Workflow addAction(boolean resuseCachedInitialContextIfAvailable, EJBRemoteConfig remoteEJBConfig, TestConfig.EJBS ejbToInvoke, TestConfig.Tx tx, String...expectedRoles) {
        this.actions.add(new EJBAction(resuseCachedInitialContextIfAvailable, remoteEJBConfig, ejbToInvoke.info, tx, expectedRoles));
        return this;
    }

    public WorkflowAction addWorkflowAction() {
        WorkflowAction workflowAction = new WorkflowAction(this, this.actions.size());
        this.actions.add(workflowAction);
        return workflowAction;
    }

    public WorkflowAction addWorkflowAction(String name) {
        WorkflowAction workflowAction = new WorkflowAction(this, name, this.actions.size());
        this.actions.add(workflowAction);
        return workflowAction;
    }

    public EJBRequest invokeNextAction(EJBRequest ejbRequest) throws Exception {
        return getCurrentWorkflowAction().invokeNextActionInternal(ejbRequest);
    }

    protected EJBRequest invokeNextActionInternal(EJBRequest ejbRequest) throws Exception {
        Action action = actions.get(currentActionIndex);
        return action.invoke(ejbRequest);
    }

    public void resetCurrentWorkflowAction() {
        currentWorkflowActionIndex = -1;
    }

    public Workflow getCurrentWorkflowAction() {
        if(currentWorkflowActionIndex == -1)
            return this;
        Workflow current = (WorkflowAction) this.actions.get(currentWorkflowActionIndex);
        Workflow next = current.getCurrentWorkflowAction();
        while(next != null && System.identityHashCode(current) != System.identityHashCode(next)) {
            current = next;
            next = current.getCurrentWorkflowAction();
        }
        return current;
    }

    public void addCurrentInvocationPathAndIncrementActionIndex(InvocationPath invocationPath) {
        addCurrentInvocationPathAndIncrementActionIndexInternal(invocationPath);
    }

    protected int getCurrentWorkflowActionIndex() {
        return this.currentWorkflowActionIndex;
    }

    protected void setCurrentWorkflowActionIndex(int currentWorkflowActionIndex) {
        this.currentWorkflowActionIndex = currentWorkflowActionIndex;
    }

    private void addCurrentInvocationPathAndIncrementActionIndexInternal(InvocationPath invocationPath) {
        System.out.printf("Adding path: %s to workflow: %s path size: %d\n", invocationPath.getNodeName(), this.getName(), this.invocationPath.size()); System.out.flush();
        log.debugf("Adding path: %s to workflow: %s\n", invocationPath.getNodeName(), this.getName());
        invocationPath.setWorkflow(this);
        this.invocationPath.add(invocationPath);

        currentActionIndex++; // incrementing means we have completed the action at this point
    }

    protected void incrementActionIndex() {
        this.currentActionIndex++;
    }

    public boolean hasNextAction() {
        return getCurrentWorkflowAction().hasNextActionInternal();
    }
    public boolean hasNextActionInternal() {
        return currentActionIndex < this.actions.size();
    }

    public Action getCurrentAction() {
        return getCurrentWorkflowAction().getCurrentActionInternal();
    }
    public Action getCurrentActionInternal() {
        if(currentActionIndex < 0 || this.actions.size() <= 0)
            return null;
        return this.getActions().get(currentActionIndex);
    }

    public void resetActionIndex() {
        this.currentActionIndex = 0;
    }

    public List<InvocationPath> getInvocationPath() {
        return invocationPath;
    }

    public List<InvocationPath> getFullInvocationPath() {
        List<InvocationPath> fullInvocationPath = new ArrayList<>();

        for (Action c : this.getActions()) {
            if (c.isWorkflowAction()) {
                fullInvocationPath.addAll(((Workflow) c).getInvocationPath());
            }
        }
        return fullInvocationPath;
    }


    public String getName() {
        return this.name;
    }

    public void setCurrentActionIndex(int currentActionIndex) {
        this.currentActionIndex = currentActionIndex;
    }

    public int getCurrentActionIndex() {
        return currentActionIndex;
    }

    public EJBRequest invoke(EJBRequest ejbRequest) throws Exception {
      EJBRequest response = ejbRequest;
      Workflow wf = response.getCurrentWorkflow();
      wf.resetActionIndex();

      for(int i=0; i<this.getActions().size(); i++) {
          log.debugf("Workflow: %s invoking action\n", this.getName());
          response = this.getActions().get(i).invoke(response);
      }
      return response;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 0, j = 0;
        sb.append(String.format("Workflow: %s\n", name));
        for (Action action : this.getActions()) {
            sb.append(String.format("- [%d] Action: %s\n", j, action.toString()));
            j++;
        }
        i++;
        return sb.toString();
    }
}