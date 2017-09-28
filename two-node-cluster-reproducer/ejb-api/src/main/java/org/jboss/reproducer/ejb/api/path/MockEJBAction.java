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

import org.jboss.logging.Logger;
import org.jboss.reproducer.ejb.api.EJBRequest;

/**
 * @author bmaxwell
 *
 */
public class MockEJBAction extends EJBAction implements Action {

    protected Logger log = Logger.getLogger(this.getClass().getName());

    private String name;

    public MockEJBAction(String name) {
        this.name = name;
        expectedRoles.add("MockRole1");
        expectedRoles.add("MockRole2");
    }

    @Override
    public EJBRequest invoke(EJBRequest ejbRequest) throws Exception {
        EJBRequest response = ejbRequest;


        response = new MockEJB("MockEJB").invoke(response);

//        // this can return null meaning the EJBRequest object contains no workflows or actions
//        Workflow workflow = response.getCurrentWorkflow();
//
//        // Create the InvocationPath before invoking actions/role checks as they will need to modify the path
//        InvocationPath path = new InvocationPath(this.getClass().getSimpleName(), "MockNode-" + name, "MockPrincipal");
//        path.setClusterName("MockCluster");
//        path.setMethod("MockEJBAction.invoke");
//
//        // this is when the EJBRequest has a workflow/actions
//        if(workflow != null) {
//            System.out.println("MockEJBAction EJB currentWorkflow: " + workflow.getName());
//            // This must be called before addCurrentInvocationPathAndIncrementActionIndex
////            Action action = workflow.getCurrentAction();
//
//            response.addCurrentInvocationPathAndIncrementActionIndex(path); // this must be called before invoking or returning anything to save the path and inc the index
//
//            // call actions
//            try {
//                int i = 0;
//                while(workflow.hasNextAction()) {
//                    System.out.println("MockEJBAction: workflow.hasNextAction" + workflow.hasNextAction() + " i=" + i + " CurrentWOrkflow: " + workflow.getName());
//                    // this must be updated here as the response object changes after every action is invoked
//                    response = workflow.invokeNextAction(response);
//                    workflow = response.getCurrentWorkflow();
//                    i++;
//                }
//            } catch(Exception e) {
//                path.setException(new Exception(workflow.getCurrentAction().toString(), e));
//            }
//        } else { // if workflow is null, then we need to track the invocations
////            response.addCurrentInvocationPathWithNoWorkflows(path);
//        }
        return response;
    }

    @Override
    public String toString() {
//        return String.format("EJBAction invoke %s using %s", ejbInfo.getRemoteLookupPath(), remoteEJBConfig.toString());
        return String.format("MockEJBAction name: %s", name);
    }

}