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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.reproducer.ejb.api.EJBInfo;
import org.jboss.reproducer.ejb.api.EJBRemote;
import org.jboss.reproducer.ejb.api.EJBRemoteConfig;
import org.jboss.reproducer.ejb.api.EJBRemoteNamingConfig;
import org.jboss.reproducer.ejb.api.EJBRemoteNamingConfig.Version;
import org.jboss.reproducer.ejb.api.EJBRequest;
import org.jboss.reproducer.ejb.api.TestConfig;
import org.jboss.reproducer.ejb.api.TestConfig.CLUSTER;
import org.jboss.reproducer.ejb.api.TestConfig.CREDENTIAL;
import org.jboss.reproducer.ejb.api.TestConfig.EJBS;
import org.jboss.reproducer.ejb.api.TestConfig.SERVER;
import org.jboss.reproducer.ejb.api.path.InvocationPath;

/**
 * @author bmaxwell
 *
 */
public class TestClusterClient {

    private static String applicationName = "SLSBTest";
//    private static EJBInfo clusterEJBInfo = new EJBInfo(applicationName, "SLSBEJB", "ClusterSLSBEJB", "org.jboss.reproducer.ejb.slsb", ClusterSLSBRemote.class);

    private static EJBInfo clusterSlsb = EJBS.CLUSTERED_EJB1.info;
    private static EJBInfo ejbJBossEjbClientXml = EJBS.CLUSTERED_EJB1_JBOSS_EJB_CLIENT_XML.info;
    private static EJBInfo ejbWildflyClientXml = EJBS.CLUSTERED_EJB1_WILDFLY_CLIENT_XML.info;
    private static TestConfig.SERVER server = SERVER.NODE1;
    private static TestConfig.CLUSTER cluster = CLUSTER.CLUSTER1;
    private static TestConfig.CREDENTIAL credential = CREDENTIAL.EJBUSER;

    private static EJBRemoteConfig unavailable_node = new EJBRemoteNamingConfig(SERVER.UNAVAILABLE_NODE, TestConfig.CREDENTIAL.EJBUSER);
    private static EJBRemoteConfig cluster1_node1 = new EJBRemoteNamingConfig(SERVER.CLUSTER1_NODE1, CREDENTIAL.EJBUSER);
    private static EJBRemoteConfig cluster2_node1 = new EJBRemoteNamingConfig(SERVER.CLUSTER2_NODE1, CREDENTIAL.EJBUSER);

//    EJBRemoteConfig cluster2_node1 = new EJBRemoteNamingConfig(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.EJBUSER);
//    EJBRemoteConfig cluster2_node2 = new EJBRemoteNamingConfig(TestConfig.SERVER.NODE2, TestConfig.CREDENTIAL.EJBUSER);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        testClusterSeparationSimple();
    }

    public static void testClusterSeparationSimple() throws Exception {
        String ejbLookup = "ejb:reproducer-ear/reproducer-ejb/ClusterSLSBEJB!org.jboss.reproducer.ejb.api.slsb.ClusterSLSBRemote";
        Properties env1 = new Properties();
        Integer port = 8080;
        env1.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        env1.setProperty(Context.PROVIDER_URL, "remote+http://localhost:" + (port+100));
        env1.setProperty(Context.SECURITY_PRINCIPAL, "ejbuser");
        env1.setProperty(Context.SECURITY_CREDENTIALS, "redhat1!");

        Properties env2 = new Properties();
        env2.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        env2.setProperty(Context.PROVIDER_URL, "remote+http://localhost:" + (port+250));
        env2.setProperty(Context.SECURITY_PRINCIPAL, "ejbuser");
        env2.setProperty(Context.SECURITY_CREDENTIALS, "redhat1!");

        System.out.println("Test config: (list cluster1 node1)" );
        System.out.println("--------------------------------------------------------");
        env1.list(System.out);
        System.out.println("Lookup: " + ejbLookup);
        Context ctx1 = new InitialContext(env1);
        EJBRemote ejbCluster1 = (EJBRemote) ctx1.lookup(ejbLookup);
        EJBRequest response1 = new EJBRequest();
        for(int i=0; i<20; i++)
            response1 = ejbCluster1.invoke(response1);

        System.out.println(response1.getResponseInvocationPath());
        InvocationsInfo info = new InvocationsInfo(response1);
        System.out.println("Nodes Invoked: " + info.getNodesInvoked());
        if(info.getNodesInvoked().size() > 2)
            System.out.println("Cluster separation failed as there should only be 2 nodes in the cluster");
        System.out.println("Cluster info");
        for(String nodeName : info.getNodesInvoked()) {
            System.out.printf("Node: %s clusterInfo: %s\n", nodeName, info.getInvocationPathForNode(nodeName).get(0).getClusterInfo());
        }
        System.out.println("--------------------------------------------------------");

        System.out.println("Test config: (list cluster2 node2)" );
        System.out.println("--------------------------------------------------------");
        env2.list(System.out);
        System.out.println("Lookup: " + ejbLookup);
        Context ctx2 = new InitialContext(env2);
        EJBRemote ejbCluster2 = (EJBRemote) ctx2.lookup(ejbLookup);
        EJBRequest response2 = new EJBRequest();
        for(int i=0; i<20; i++)
            response2 = ejbCluster2.invoke(response2);

        System.out.println(response1.getResponseInvocationPath());
        info = new InvocationsInfo(response2);
        System.out.println("Nodes Invoked: " + info.getNodesInvoked());
        if(info.getNodesInvoked().size() > 2)
            System.out.println("Cluster separation failed as there should only be 2 nodes in the cluster");
        System.out.println("Cluster info");
        for(String nodeName : info.getNodesInvoked()) {
            System.out.printf("Node: %s clusterInfo: %s\n", nodeName, info.getInvocationPathForNode(nodeName).get(0).getClusterInfo());
        }
        System.out.println("--------------------------------------------------------");
    }

    public static class InvocationsInfo {

        private EJBRequest response;
        private Set<String> nodesInvoked = null;
        private Map<String, List<InvocationPath>> invocationsPerNode = null;

        public InvocationsInfo(EJBRequest response) {
            this.response = response;
        }

        public Set<String> getNodesInvoked() {
            if(nodesInvoked == null) {
                Set<String> nodes = new TreeSet<>();
                for(InvocationPath ip : response.getInvocationPath()) {
                    nodes.add(ip.getNodeName());
                }
                this.nodesInvoked = nodes;
            }
            return this.nodesInvoked;
        }

        public Map<String, List<InvocationPath>> getInvocationPathsPerNodeMap() {
            if(this.invocationsPerNode == null) {
                this.invocationsPerNode = new HashMap<>();
                for(InvocationPath ip : response.getInvocationPath()) {
                    List<InvocationPath> paths = this.invocationsPerNode.get(ip.getNodeName());
                    if(paths == null) {
                        paths = new ArrayList<>();
                        this.invocationsPerNode.put(ip.getNodeName(), paths);
                    }
                    paths.add(ip);
                }
            }
            return this.invocationsPerNode;
        }

        public List<InvocationPath> getInvocationPathForNode(String nodeName) {
            return getInvocationPathsPerNodeMap().get(nodeName);
        }
    }


    public static Set<String> getNodesInvoked(List<InvocationPath> invocationPath) {
        Set<String> nodes = new HashSet<>();
        for(InvocationPath ip : invocationPath) {
            nodes.add(ip.getNodeName());
        }
        return nodes;
    }

    private static void testClustering() throws Exception {

        String testExpectation = "testClustering 1 - expect client specifies 1 node in providerURL and then load balances across all of the cluster nodes";

        String testName = "testClustering 1";
        EJBRequest response = new EJBRequest(testName);
        System.setProperty("jboss.node.name", testName);

        // invoke the ejb 50 times, start with just 1 node in the providerURL, reuse the proxy after it is retrieved
        response.addWorkflow("Main")
        .addWorkflowAction("WFAction1")
            .addRepeatedEJBAction(50, true, cluster1_node1, clusterSlsb).end();
        response = response.invoke();
        // check the results
        System.out.println(Asserts.isWorkflowClustered(response, 0));
        System.out.println(response.getResponseInvocationPath());
        /* ---------------- end -------------------- */

        testExpectation = "testClustering 2 - expect client specifies 2 nodes in providerURL where the first is not available and it should failover to 2nd and then load balance across all available cluster nodes";
        response = new EJBRequest(testName);
        EJBRemoteNamingConfig config = new EJBRemoteNamingConfig(Version.WildflyInitialContextFactory);
        config.setUsernamePassword(credential.username, credential.password);
        config.addProvider(SERVER.UNAVAILABLE_NODE.host, SERVER.UNAVAILABLE_NODE.remotingPort);
        config.addProvider(SERVER.NODE1.host, SERVER.NODE1.remotingPort);
        response.addWorkflow("Main")
        .addWorkflowAction("WFAction1")
            .addRepeatedEJBAction(50, true, cluster1_node1, clusterSlsb).end();
        response = response.invoke();
        // check the results
        System.out.println(Asserts.isWorkflowClustered(response, 0));
        System.out.println(response.getResponseInvocationPath());
        /* ---------------- end -------------------- */
    }

    private static void testClusterSeparation() throws Exception {
        String testExpectation = "testClusterSeparation 1 - EJB deployed on 2 clusters, expect load balancing sticks to the cluster attached to the InitialContext";

        // need 2 clusters separate with multicast, both will default to ejb as the cluster name
        // test 1 see if they are separate
        // test if cluster name works

        // standalone client use InitialContext with node1 in cluster1 and verify load balancing happens only on cluster1
        // then use new InitialContext to hit node1 on cluster2 and make sure it only invokes on cluster2
        // then repeat where standalone client calls client server (non clustered) and have it repeat trying to call cluster1 then cluster2

        Set<String> clusterOneNodes = new HashSet<>();
        clusterOneNodes.add(SERVER.CLUSTER1_NODE1.nodeName);
        clusterOneNodes.add(SERVER.CLUSTER1_NODE2.nodeName);
        Set<String> clusterTwoNodes = new HashSet<>();
        clusterTwoNodes.add(SERVER.CLUSTER2_NODE1.nodeName);
        clusterTwoNodes.add(SERVER.CLUSTER2_NODE2.nodeName);


        String testName = "testClusterSeparation 1";
        EJBRequest response = new EJBRequest(testName);
        System.setProperty("jboss.node.name", testName);

        response.addWorkflow("Main")
        .addWorkflowAction("WFAction1")
            .addRepeatedEJBAction(10, true, cluster1_node1, clusterSlsb).end();
        response = response.invoke();

        System.out.println("Nodes Invoked: " + Asserts.getNodesInvoked(response.getInvocationPath()));
        System.out.println(Asserts.wereNodesInvoked(response.getInvocationPath(), clusterTwoNodes));
        System.out.println(response.getResponseInvocationPath());
        /* ---------------- end -------------------- */

        testName = "testClusterSeparation 2";
        response = new EJBRequest(testName);
        System.setProperty("jboss.node.name", testName);

        response.addWorkflow("Main")
        .addWorkflowAction("WFAction1")
            .addRepeatedEJBAction(10, true, cluster2_node1, clusterSlsb).end();
        response = response.invoke();

        System.out.println("Nodes Invoked: " + Asserts.getNodesInvoked(response.getInvocationPath()));
        System.out.println(Asserts.wereNodesInvoked(response.getInvocationPath(), clusterTwoNodes));
        System.out.println(response.getResponseInvocationPath());

        /* ---------------- end -------------------- */
    }
}