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

package org.jboss.reproducer.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.reproducer.ejb.api.EJBRequest;
import org.jboss.reproducer.ejb.api.TestConfig;
import org.jboss.reproducer.ejb.api.path.InvocationPath;
import org.junit.Assert;

/**
 * @author bmaxwell
 *
 */
public class Asserts {

    public static void assertNodeName(InvocationPath invocationPath, TestConfig.SERVER server) {
        System.out.println("Assert nodeName: " + server.nodeName + " == " + invocationPath.getNodeName());
        Assert.assertEquals("NodeName was not as expected", server.nodeName, invocationPath.getNodeName());
    }

    public static void assertNodeName(InvocationPath invocationPath, String nodeName) {
        System.out.println("Assert nodeName: " + nodeName + " == " + invocationPath.getNodeName());
        Assert.assertEquals("NodeName was not as expected", nodeName, invocationPath.getNodeName());
    }

    public static void assertNodeName(InvocationPath expected, InvocationPath actual) {
        System.out.println("Assert nodeName: " + expected.getNodeName() + " == " + actual.getNodeName());
        Assert.assertEquals("NodeName was not as expected", expected.getNodeName(), actual.getNodeName());
    }

    public static void assertPrincipal(InvocationPath expected, InvocationPath actual) {
        System.out.println("Assert principal: " + expected.getCallerPrincipal() + " == " + actual.getCallerPrincipal());
        Assert.assertEquals("Principal was not as expected", expected.getCallerPrincipal(), actual.getCallerPrincipal());
    }

    public static boolean isAllWorkflowsSticky(EJBRequest response) {
        for(int i=0; i<response.getWorkflowsSize(); i++) {
            if(!isWorkflowSticky(response,i))
                return false;
        }
        return true;
    }

    public static boolean isWorkflowTxSticky(EJBRequest response, int workflowIndex, int invocationPathStartIndex) {
        Map<String, Integer> txMap = new HashMap<>();

        List<InvocationPath> invocationPath = response.getWorkflow(workflowIndex).getFullInvocationPath();
        for(int i=invocationPathStartIndex; i<invocationPath.size(); i++) {
            InvocationPath path = invocationPath.get(i);
            Integer count = txMap.get(path.getTransactionInfo().getKey());
            if(count == null)
                count = 1;
            else
                count++;
            txMap.put(path.getTransactionInfo().getKey(), count);
        }
        if(txMap.size() > 1)
            return false;
        return true;
    }


    public static boolean isWorkflowSticky(EJBRequest response, int workflowIndex) {
        return isWorkflowSticky(response, workflowIndex, 0);
    }
    public static boolean isWorkflowSticky(EJBRequest response, int workflowIndex, int invocationPathStartIndex) {
        String nodeName = null;
        List<InvocationPath> invocationPath = response.getWorkflow(workflowIndex).getFullInvocationPath();
        for(int i=invocationPathStartIndex; i<invocationPath.size(); i++) {
            InvocationPath path = invocationPath.get(i);
            if(nodeName == null)
                nodeName = path.getNodeName();
            else
                if(!nodeName.equals(path.getNodeName()))
                        return false;
        }
        return true;
    }
    public static boolean isWorkflowClustered(EJBRequest response, int workflowIndex) {
        return isWorkflowClustered(response, workflowIndex);
    }
    public static boolean isWorkflowClustered(EJBRequest response, int workflowIndex, int invocationPathStartIndex) {
        return ! isWorkflowSticky(response, workflowIndex, invocationPathStartIndex);
    }

    public static void assertWorkflowClustered(EJBRequest response, int workflowIndex) {
        Assert.assertEquals("Expected Clustered Invocations and they were not", true, isWorkflowClustered(response, workflowIndex));
    }
    public static void assertWorkflowSticky(EJBRequest response, int workflowIndex) {
        Assert.assertEquals("Expected Sticky Invocations and they were not", true, isWorkflowSticky(response, workflowIndex));
    }

    public static boolean isSticky(List<InvocationPath> invocationPath) {
        String nodeName = null;
        for(InvocationPath ip : invocationPath) {
            if(nodeName == null)
                nodeName = ip.getNodeName();
            else
                if(!nodeName.equals(ip.getNodeName()))
                        return false;
        }
        return true;
    }

    public static boolean isClustered(List<InvocationPath> invocationPath) {
        return ! isSticky(invocationPath);
    }

    public static void failIfNotClustered(List<InvocationPath> invocationPath) {
        Assert.assertEquals("Expected clustered, but all invocations went to only 1 node: " + getNodesInvoked(invocationPath), true, isClustered(invocationPath));
    }

    public static Collection<Integer> getInvocationsPerNode(List<InvocationPath> invocationPath) {
        Map<String, Integer> map = new HashMap<>();
        for(InvocationPath ip : invocationPath) {
            Integer i = map.get(ip.getNodeName());
            if(i == null)
                i = 1;
            else
                i++;
            map.put(ip.getNodeName(), i);
        }
        return map.values();
    }

    public static Collection<String> getNodesInvoked(List<InvocationPath> invocationPath) {
        Set<String> nodes = new HashSet<>();
        for(InvocationPath ip : invocationPath) {
            nodes.add(ip.getNodeName());
        }
        return nodes;
    }


//    public static void assertExpectedResult(ExpectedResult er, EJBRequest response) {
//        int i=0;
//        for(InvocationPath expected : er.getExpectedPath()) {
//            InvocationPath actual = response.getInvocationPath().get(i);
//            assertNodeName(expected , actual);
//            assertPrincipal(expected, actual);
//            i++;
//        }
//
//        // check sticky
//        if(er.getExpectSticky() != null) {
//            if(er.getExpectSticky() == true)
//                Assert.assertEquals("Expected Sticky did not match", er.getExpectSticky(), isSticky(response.getInvocationPath()));
//        }
//
//        // check clustered
//        if(er.getExpectClustered() != null) {
//            if(er.getExpectClustered() == true)
//                Assert.assertEquals("Expected Sticky did not match", er.getExpectClustered(), isClustered(response.getInvocationPath()));
//        }
//
//
//        // check # nodes invoked
//        if(er.getExpectedUniqueNodesInvoked() != null) {
//            Assert.assertEquals("Number of Nodes Invoked did not meet expectations", er.getExpectedUniqueNodesInvoked().intValue(), getNodesInvoked(response.getInvocationPath()).size());
//        }
//    }
}