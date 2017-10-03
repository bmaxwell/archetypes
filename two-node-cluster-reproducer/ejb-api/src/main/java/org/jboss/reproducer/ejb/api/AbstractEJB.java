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
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.SessionSynchronization;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.Xid;

import org.jboss.ejb.client.EJBClient;
import org.jboss.logging.Logger;
import org.jboss.reproducer.ejb.api.path.Action;
import org.jboss.reproducer.ejb.api.path.ActionHandler;
import org.jboss.reproducer.ejb.api.path.ClusterInfo;
import org.jboss.reproducer.ejb.api.path.EJBAction;
import org.jboss.reproducer.ejb.api.path.InvocationPath;
import org.jboss.reproducer.ejb.api.path.TransactionInfo;
import org.jboss.reproducer.ejb.api.path.TransactionStatus;
import org.jboss.reproducer.ejb.api.path.Workflow;
import org.wildfly.clustering.group.Group;
import org.wildfly.clustering.group.Node;

/**
 * @author bmaxwell
 *
 */
public class AbstractEJB implements EJBRemote, SessionSynchronization {

    protected Logger log = Logger.getLogger(this.getClass().getName());
    protected String nodeName = System.getProperty("jboss.node.name");
    protected String clusterName = null;

    @Resource
    private SessionContext context;

    @Resource(mappedName = "java:comp/TransactionSynchronizationRegistry")
    protected TransactionSynchronizationRegistry txSyncReg;

    private Group channelGroup;

    // TODO abstract it out to extend MockEJB so that everything is the same
    private InvocationPath createInvocationPath(String method, String callingNode) {
        InvocationPath path = new InvocationPath(this.getClass().getSimpleName(), nodeName, context.getCallerPrincipal().getName());
        path.setClusterName(getClusterName());
        path.setTransactionInfo(getTransactionInfo());
        path.setMethod(method);
        path.setCallerAddress(callingNode + " " + getCallerAddress());
        path.setClusterInfo(getClusterInfo());
        return path;
    }

    private EJBRequest createResponse(EJBRequest response, InvocationPath path, ActionHandler actionHandler, Principal principal) {
        // this can return null meaning the EJBRequest object contains no workflows or actions
        Workflow workflow = response.getCurrentWorkflow();

        // Create the InvocationPath before invoking actions/role checks as they will need to modify the path

        // this is when the EJBRequest has a workflow/actions
        if(workflow != null) {
            System.out.println("AbstractEJB EJB currentWorkflow: " + workflow.getName());
            // This must be called before addCurrentInvocationPathAndIncrementActionIndex
            Action action = workflow.getCurrentAction();

            if(actionHandler != null) {
                Exception exception = actionHandler.handle(action, principal);
                if(exception != null) {
                    path.setException(exception);
                    return response;
                }
            }

            response.addCurrentInvocationPathAndIncrementActionIndex(path); // this must be called before invoking or returning anything to save the path and inc the index

            // call actions
            try {
                int i = 0;
                while(workflow.hasNextAction()) {
                    System.out.println("AbstractEJB: workflow.hasNextAction" + workflow.hasNextAction() + " i=" + i + " CurrentWOrkflow: " + workflow.getName());
                    // this must be updated here as the response object changes after every action is invoked
                    response = workflow.invokeNextAction(response);
                    workflow = response.getCurrentWorkflow();
                    i++;
                }
            } catch(Exception e) {
                path.setException(new Exception(workflow.getCurrentAction().toString(), e));
            }
        } else { // if workflow is null, then we need to track the invocations
            response.addCurrentInvocationPathWithNoWorkflows(path);
        }
        return response;
    }

    private String getCallerAddress() {
        // EJBClient.SOURCE_ADDRESS_KEY == jboss.source-address
        InetSocketAddress clientAddress = (InetSocketAddress) context.getContextData().get(EJBClient.SOURCE_ADDRESS_KEY);
        if(clientAddress != null) return String.format("%s:%d", clientAddress.getHostString(), clientAddress.getPort());
        return null;
    }

    private EJBRequest invokeInternal(EJBRequest request, String method) {
        EJBRequest response = request;
        String callingNode = response.getCallingNode();
        log.info("request received to :" + method + " principal: " + context.getCallerPrincipal().getName() + " callingNode: " + callingNode);

        response.setCallingNode(null);
        return createResponse(response, createInvocationPath(method, callingNode), getActionHandler(), context.getCallerPrincipal());
    }

//    private EJBRequest invokeInternal(EJBRequest request, String method) {
//        log.info("request received to :" + method + " principal: " + context.getCallerPrincipal().getName());
//        EJBRequest response = request;
//
//        // this can return null meaning the EJBRequest object contains no workflows or actions
//        Workflow workflow = response.getCurrentWorkflow();
//
//        // Create the InvocationPath before invoking actions/role checks as they will need to modify the path
//        InvocationPath path = new InvocationPath(this.getClass().getSimpleName(), nodeName, context.getCallerPrincipal().getName());
//        path.setClusterName(getClusterName());
//        path.setTransactionInfo(getTransactionInfo());
//        path.setMethod(method);
//
//        // this is when the EJBRequest has a workflow/actions
//        if(workflow != null) {
////            System.out.println("Abstract EJB currentWorkflow: " + workflow.getName());
//            // This must be called before addCurrentInvocationPathAndIncrementActionIndex
//            Action action = workflow.getCurrentAction();
//
//            response.addCurrentInvocationPathAndIncrementActionIndex(path); // this must be called before invoking or returning anything to save the path and inc the index
//
//            Exception roleValidationException = validateExpectedRoles(context.getCallerPrincipal().getName(), action);
//            if(roleValidationException != null) {
//                path.setException(roleValidationException);
//                return response;
//            }
//
//            // call actions
//            try {
//                int i = 0;
//                while(workflow.hasNextAction()) {
////                    System.out.println("AbstractEJB: workflow.hasNextAction" + workflow.hasNextAction() + " i=" + i + " CurrentWOrkflow: " + workflow.getName());
//                    // this must be updated here as the response object changes after every action is invoked
//                    response = workflow.invokeNextAction(response);
//                    workflow = response.getCurrentWorkflow();
//                    i++;
//                }
//            } catch(Exception e) {
//                path.setException(new Exception(workflow.getCurrentAction().toString(), e));
//            }
//        } else { // if workflow is null, then we need to track the invocations
//            response.addCurrentInvocationPathWithNoWorkflows(path);
//        }
//        return response;
//    }

    @Override
    public EJBRequest invoke(EJBRequest request) {
        return invokeInternal(request, "invoke");
    }

    protected TransactionInfo getTransactionInfo() {

        TransactionInfo txInfo = new TransactionInfo();
        boolean transactionExists = TransactionStatus.isTransactionExists(txSyncReg.getTransactionStatus());
        if(transactionExists) {
//            System.out.println("TxClass: " + txSyncReg.getTransactionKey().getClass().getName());
//            for(Class iface : txSyncReg.getTransactionKey().getClass().getInterfaces()) {
//                System.out.println("Interface: " + iface.getName());
//            } System.out.flush();

            Object resource = txSyncReg.getResource(txSyncReg.getTransactionKey());
            if(resource == null)
                System.out.println("txSyncReg.getResource returned null");
            else
                System.out.println("txSyncReg.getResource returned " + resource.getClass().getName() + " " + resource);
            System.out.flush();

            String xidId = getXidAsString();
            if(xidId != null)
                txInfo.setKey(txSyncReg.getTransactionKey() + " " + xidId);
            else
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

    private TransactionManager getTransactionManager() throws Exception {
        return (TransactionManager) new InitialContext().lookup("java:jboss/TransactionManager");
    }
    private String getXidAsString() {
//        com.arjuna.ats.internal.jta.transaction.jts.TransactionImple implements javax.transaction.Transaction, com.arjuna.ats.jta.transaction.Transaction
//        public final Xid getTxId ()
        try {
            Transaction tx = getTransactionManager().getTransaction();
            Xid xid = (Xid) tx.getClass().getMethod("getXid", new Class[0]).invoke(tx, new Object[0]);
            String globalId = javax.xml.bind.DatatypeConverter.printHexBinary(xid.getGlobalTransactionId());
            String branchQualifier = javax.xml.bind.DatatypeConverter.printHexBinary(xid.getBranchQualifier());
            return String.format("Xid: formatId: %d globalTxId: %s branchQualifier: %s", xid.getFormatId(), globalId, branchQualifier);

        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
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

    private Group getChannelGroup() {
        if(channelGroup == null) {
            try {
                channelGroup = (Group) new InitialContext().lookup("java:jboss/clustering/group/default");
            } catch (NameNotFoundException e) {
                // this means it is not clustered
            } catch (NamingException e) {
                e.printStackTrace();
            }
        }
        return channelGroup;
    }

    public ClusterInfo getClusterInfo() {
        ClusterInfo clusterInfo = null;
        Group channelGroup = getChannelGroup();
        if(channelGroup != null) {
            clusterInfo = new ClusterInfo();
            clusterInfo.setName(channelGroup.getName());
            clusterInfo.setThisNode(channelGroup.getLocalNode().getName());
            clusterInfo.setCoordinatorNode(channelGroup.getCoordinatorNode().getName());

            Set<String> members = new TreeSet<>();
            for(Node node : channelGroup.getNodes())
                members.add(node.getName());
            clusterInfo.setMembers(members);
        }
        return clusterInfo;
    }

    private ActionHandler getActionHandler() {
        return new ActionHandler() {
            @Override
            public Exception handle(Action action, Principal principal) {
                if(action instanceof EJBAction) {
                    for(String role : ((EJBAction)action).getExpectedRoles()) {
                        if(!context.isCallerInRole(role))
                            return new Exception(String.format("%s is expected to have the role: %s", principal.getName(), role));
                    }
                }
                return null;
            }
        };
    }

    private Exception validateExpectedRoles(String caller, Action action) {
        if(action instanceof EJBAction) {
            for(String role : ((EJBAction)action).getExpectedRoles()) {
                if(!context.isCallerInRole(role))
                    return new Exception(String.format("%s is expected to have the role: %s", caller, role));
            }
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

    @Override
    public void afterBegin() throws EJBException, RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeCompletion() throws EJBException, RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterCompletion(boolean committed) throws EJBException, RemoteException {
        // TODO Auto-generated method stub

    }
}