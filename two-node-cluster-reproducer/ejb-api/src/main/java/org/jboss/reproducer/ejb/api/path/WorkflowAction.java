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

import org.jboss.reproducer.ejb.api.EJBRemoteConfig;
import org.jboss.reproducer.ejb.api.EJBRequest;
import org.jboss.reproducer.ejb.api.TestConfig;
import org.jboss.reproducer.ejb.api.TestConfig.Tx;

/**
 * @author bmaxwell
 *
 */
public class WorkflowAction extends Workflow implements Action {

    private Workflow parent;
    private int myIndex;

    public WorkflowAction(Workflow parent, int myIndex) {
        this.parent = parent;
        this.myIndex = myIndex;
    }
    public WorkflowAction(Workflow parent, String name, int myIndex) {
        super(name);
        this.parent = parent;
        this.myIndex = myIndex;
    }

    // override addAction so that we can return WorkflowAction to be able to call end()
    @Override
    public WorkflowAction addAction(EJBRemoteConfig remoteEJBConfig, TestConfig.EJBS ejbToInvoke, String...expectedRoles) {
        super.addAction(remoteEJBConfig, ejbToInvoke, expectedRoles);
        return this;
    }

    // a repeated action we want to invoke the same action a number of times while not advancing the workflow, it would be similar to adding a workflow with 1 action n times
    public WorkflowAction addRepeatedEJBAction(int repeated, EJBRemoteConfig remoteEJBConfig, TestConfig.EJBS ejbToInvoke, String...expectedRoles) {
        EJBAction ejbAction = new EJBAction(remoteEJBConfig, ejbToInvoke.info, expectedRoles);
        RepeatedAction repeatedAction = new RepeatedAction(repeated, ejbAction);
        super.addAction(repeatedAction);
        return this;
    }
    public WorkflowAction addRepeatedEJBAction(int repeated, boolean reuseProxy, EJBRemoteConfig remoteEJBConfig, TestConfig.EJBS ejbToInvoke, String...expectedRoles) {
        EJBAction ejbAction = new EJBAction(remoteEJBConfig, ejbToInvoke.info, expectedRoles);
        ejbAction.setReuseCachedProxy(true);
        RepeatedAction repeatedAction = new RepeatedAction(repeated, ejbAction);
        super.addAction(repeatedAction);
        return this;
    }
    public WorkflowAction addRepeatedEJBAction(int repeated, boolean reuseProxy, EJBRemoteConfig remoteEJBConfig, TestConfig.EJBS ejbToInvoke, Tx tx, String...expectedRoles) {
        EJBAction ejbAction = new EJBAction(remoteEJBConfig, ejbToInvoke.info, expectedRoles);
        ejbAction.setReuseCachedProxy(true);
        ejbAction.setTx(tx);
        RepeatedAction repeatedAction = new RepeatedAction(repeated, ejbAction);
        super.addAction(repeatedAction);
        return this;
    }



    @Override
    public WorkflowAction addAction(Action action) {
        super.actions.add(action);
        return this;
    }

    public Workflow end() {
        return parent;
    }

    @Override
    public EJBRequest invoke(EJBRequest ejbRequest) throws Exception {
      EJBRequest response = ejbRequest;

      Workflow wf = response.getCurrentWorkflow();
      wf.resetActionIndex();

      // this changes the workflow index so that now this WorkflowAction is the current workflow
      parent.setCurrentWorkflowActionIndex(myIndex);


      // this is invoked twice, but something else is invoking Action 2 first, so something is not incremented
      // this was invoking it twice, because the action index has already moved beyond
//      for(int i=0; i<this.getActions().size(); i++) {
//          log.debugf("WorkflowAction: Running Workflow [%d] - %s\n", i, response.getCurrentWorkflow());
//          response = this.getActions().get(i).invoke(response);
//      }
      // TODO can we replace the for loop with response.getCurrentWorkflow().invokeNextAction?
      // TODO this works
      for(int i=0; i<this.getActions().size();) {
          log.debugf("Workflow: %s invoking action\n", this.getName());
          response = this.getActions().get(i).invoke(response);
          i = response.getCurrentWorkflow().getCurrentActionIndex();
      }
      // This appears to work also
//      Workflow cwf = response.getCurrentWorkflow();
//      while(cwf.hasNextAction()) {
//          log.debugf("WorkflowAction: Running Workflow - %s\n", response.getCurrentWorkflow());
//          response = cwf.invokeNextAction(response);
//          cwf = response.getCurrentWorkflow();
//      }

//      for(int i=0; i<this.getActions().size(); i++) {
////          response.currentWorkflowIndex = i;
//          log.debugf("WorkflowAction: Running Workflow [%d] - %s\n", i, response.getCurrentWorkflow());
////          response.getCurrentWorkflow().resetActionIndex();
//          response = response.getCurrentWorkflow().invokeNextAction(response);
//      }
      // this increments the workflow action index, should it be incrementing the action index instead?
      // this needs to be invoked on the objects from the response to ensure they are updated
      Workflow cfw = response.getCurrentWorkflow();
      if(cfw instanceof WorkflowAction) {
          ((WorkflowAction) cfw).getParent().setCurrentWorkflowActionIndex(myIndex+1);
          ((WorkflowAction) cfw).getParent().incrementActionIndex();
      }
      return response;
    }

    public Workflow getParent() {
        return this.parent;
    }

    @Override
    public boolean isWorkflowAction() {
        return true;
    }

}