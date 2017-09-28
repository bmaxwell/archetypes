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

import org.jboss.reproducer.ejb.api.EJBRequest;

/**
 * @author bmaxwell
 *
 */
public class RepeatedAction implements Action, Serializable {

    private Action action;
    private int repeated = 0;

    public RepeatedAction() {
    }

    public RepeatedAction(int repeated, Action action) {
        this.repeated = repeated;
        this.action = action;
    }

    @Override
    public EJBRequest invoke(EJBRequest ejbRequest) throws Exception {
        // i think the workflow reference will work since we are not using a WorkflowAction
        // for the EJB update to work, we need to make sure the action index / action is correct / reset each time
        EJBRequest response = ejbRequest;
        Workflow cwf = response.getCurrentWorkflow();
        int actionIndex = cwf.getCurrentActionIndex();

        for(int i=0; i<repeated; i++) {
            // reset the action index each time since we are repeating the 1 action over and over
//            Workflow cwf = response.getCurrentWorkflow();
//            cwf.resetActionIndex();
////            response = action.invoke(ejbRequest);
//            response = cwf.invokeNextAction(response);

            cwf.setCurrentActionIndex(actionIndex);
            response = action.invoke(response);
            cwf = response.getCurrentWorkflow();
        }
        // increment the action index else we will loop forever
        cwf.setCurrentActionIndex(actionIndex+1);
        return response;
    }

    @Override
    public boolean isWorkflowAction() {
        return false;
    }

}