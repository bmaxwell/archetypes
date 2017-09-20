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

import java.lang.management.ManagementFactory;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.management.ObjectName;
import javax.transaction.TransactionSynchronizationRegistry;

import org.jboss.logging.Logger;
import org.jboss.reproducer.ejb.api.path.InvocationPath;
import org.jboss.reproducer.ejb.api.path.TransactionInfo;
import org.jboss.reproducer.ejb.api.path.TransactionStatus;
import org.jboss.reproducer.ejb.api.path.Workflow;

/**
 * @author bmaxwell
 *
 */
public class AbstractEJB implements EJBRemote {

    protected Logger log = Logger.getLogger(this.getClass().getName());
    protected String nodeName = System.getProperty("jboss.node.name");
    protected String clusterName = null;

    @Resource
    private SessionContext context;

    @Resource(mappedName = "java:comp/TransactionSynchronizationRegistry")
    protected TransactionSynchronizationRegistry txSyncReg;

    private EJBRequest invokeInternal(EJBRequest request, String method) {
        log.info("request received to :" + method);
        EJBRequest response = request;

        // this can return null meaning the EJBRequest object contains no workflows or actions
        Workflow workflow = response.getCurrentWorkflow();

        // Create the InvocationPath before invoking actions/role checks as they will need to modify the path
        InvocationPath path = new InvocationPath(this.getClass().getSimpleName(), nodeName, context.getCallerPrincipal().getName());
        path.setClusterName(getClusterName());
        path.setTransactionInfo(getTransactionInfo());
        path.setMethod(method);

        // this is when the EJBRequest has a workflow/actions
        if(workflow != null) {
            // This must be called before addCurrentInvocationPathAndIncrementActionIndex
            EJBAction action = workflow.getCurrentAction();

            response.addCurrentInvocationPathAndIncrementActionIndex(path); // this must be called before invoking or returning anything to save the path and inc the index

            Exception roleValidationException = validateExpectedRoles(context.getCallerPrincipal().getName(), action);
            if(roleValidationException != null) {
                path.setException(roleValidationException);
                return response;
            }

            // call actions
            try {
                while(workflow.hasNextAction()) {
                    // this must be updated here as the reponse object changes after every action is invoked
                    response = workflow.invokeNextAction(response);
                    workflow = response.getCurrentWorkflow();
                }
            } catch(Exception e) {
                path.setException(new Exception(workflow.getCurrentAction().toString(), e));
            }
        } else { // if workflow is null, then we need to track the invocations
            response.addCurrentInvocationPathWithNoWorkflows(path);
        }
        return response;
    }

    @Override
    public EJBRequest invoke(EJBRequest request) {
        return invokeInternal(request, "invoke");
    }

    protected TransactionInfo getTransactionInfo() {

        TransactionInfo txInfo = new TransactionInfo();
        boolean transactionExists = TransactionStatus.isTransactionExists(txSyncReg.getTransactionStatus());
        if(transactionExists) {
            txInfo.setKey(txSyncReg.getTransactionKey());
            txInfo.setActive(transactionExists);
            txInfo.setRollbackOnly(txSyncReg.getRollbackOnly());
            txInfo.setStatus(TransactionStatus.getStatusForCode(txSyncReg.getTransactionStatus()));
        } else {
            txInfo.setActive(transactionExists);
            try {
                txInfo.setStatus(TransactionStatus.getStatusForCode(txSyncReg.getTransactionStatus()));
            } catch(Exception e) {}
        }
        return txInfo;
    }

    public String getClusterName() {
        if(clusterName == null) {
            try {
                ObjectName on = new ObjectName("jboss.as:subsystem=ejb3,service=remote");
                this.clusterName = (String) ManagementFactory.getPlatformMBeanServer().getAttribute(on, "cluster");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return clusterName;
    }

    private Exception validateExpectedRoles(String caller, EJBAction action) {
        for(String role : action.getExpectedRoles()) {
            if(!context.isCallerInRole(role))
                return new Exception(String.format("%s is expected to have the role: %s", caller, role));
        }
        return null;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public EJBRequest mandatory(EJBRequest request) {
        return invokeInternal(request, "mandatory");
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public EJBRequest never(EJBRequest request) {
        return invokeInternal(request, "never");
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public EJBRequest notSupported(EJBRequest request) {
        return invokeInternal(request, "notSupported");
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public EJBRequest required(EJBRequest request) {
        return invokeInternal(request, "required");
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public EJBRequest requiresNew(EJBRequest request) {
        return invokeInternal(request, "requiresNew");
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public EJBRequest supports(EJBRequest request)  {
        // EJB 3.1 FR 13.6.2.9 getRollbackOnly is not allowed with SUPPORTS attribute
        return invokeInternal(request, "supports");
    }
}