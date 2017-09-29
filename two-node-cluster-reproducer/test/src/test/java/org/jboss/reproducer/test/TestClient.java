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
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.jboss.reproducer.ejb.api.EJBInfo;
import org.jboss.reproducer.ejb.api.EJBRemote;
import org.jboss.reproducer.ejb.api.EJBRemoteConfig;
import org.jboss.reproducer.ejb.api.EJBRemoteNamingConfig;
import org.jboss.reproducer.ejb.api.EJBRemoteNamingConfig.Version;
import org.jboss.reproducer.ejb.api.EJBRequest;
import org.jboss.reproducer.ejb.api.TestConfig;
import org.jboss.reproducer.ejb.api.TestConfig.Tx;
import org.jboss.reproducer.ejb.api.path.EJBAction;
import org.jboss.reproducer.ejb.api.path.MockEJBAction;
import org.jboss.reproducer.ejb.api.slsb.ClusterSLSBRemote;
import org.wildfly.naming.client.WildFlyInitialContextFactoryBuilder;

/**
 * @author bmaxwell
 *
 */
public class TestClient {

    private static String applicationName = "SLSBTest";
    private static EJBInfo clusterEJBInfo = new EJBInfo(applicationName, "SLSBEJB", "ClusterSLSBEJB", "org.jboss.reproducer.ejb.slsb", ClusterSLSBRemote.class);

    /**
     * @param args
     * @throws NamingException
     */
    public static void main(String[] args) throws Throwable {

//        testClustering();
        testStickyTransactionsWhenSameProxy();
//        test_JBEAP_13215();
//        test_JBEAP_13218();

//        testWildflyNamingInvocationTimeout();
//        System.out.println("wildflyNamingStickyTransactions Test");
//        System.out.println("---------------------------------");
//            wildflyNamingStickyTransactions();


//          System.out.println("WildFlyInitialContextFactory Test");
//          System.out.println("---------------------------------");
//          wildflyNaming();
//          System.out.println();
//
//          System.out.println("RemoteNaming InitialContextFactory Test");
//          System.out.println("---------------------------------");
//          remoteNaming();
//          System.out.println();
//
//          System.out.println("Scoped Context Configuration with 1 Initial Connection Specified Test");
//          System.out.println("---------------------------------");
//          scopedContextWorksInEAP70(false);
//          System.out.println();
//
//          System.out.println("Scoped Context Configuration with 2 Initial Connection Specified Test");
//          System.out.println("---------------------------------");
//          scopedContextWorksInEAP70(true);
//          System.out.println();

//        boolean addConnection2 = false;
//        if(args.length > 0) {
//            if(Boolean.valueOf(args[0]))
//                addConnection2 = true;
//        }
//        scopedContextWorksInEAP70(addConnection2);


//        try {
//            remoteNaming();
//        } catch(Throwable t) {
//            t.printStackTrace();
//            System.out.println("Surpressed: " + t.getSuppressed());
//        }
    }

    private static void test_JBEAP_13218() throws Throwable {

        TestConfig.SERVER server1 = TestConfig.SERVER.NODE1;
        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;
        EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;

        EJBRemoteNamingConfig callNode1 = new EJBRemoteNamingConfig(Version.WildflyInitialContextFactory);
        callNode1.setUsernamePassword(credential.username, credential.password);
        callNode1.addProvider(server1.host, server1.remotingPort);

        // System property
        String original = System.getProperty("java.naming.factory.initial");
        System.setProperty("java.naming.factory.initial", "org.wildfly.naming.client.WildFlyInitialContextFactory");
        // test here
        Context ctx = new InitialContext();
        System.setProperty("java.naming.factory.initial", original);

        // Environment property
        // this is typical use case for JBoss

        // Programmatic installation
        NamingManager.setInitialContextFactoryBuilder(new WildFlyInitialContextFactoryBuilder());
        // later...
//        Context ctx = new InitialContext();

        // Direct instantiation
//        Context ctx = new WildFlyInitialContext(callNode1.getConfiguration());


        Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");

        callNode1.listConfiguration(System.out);
//        Context ctx = new WildFlyInitialContext(callNode1.getConfiguration());
        EJBRemote remote = (EJBRemote) ctx.lookup(ejbInfo.getRemoteLookupPath());
        EJBRequest response = remote.invoke(new EJBRequest());
        response.throwIfAnyExceptions();
        System.out.println(response.getWorkflowsList());
        System.out.println(response.getResponseInvocationPath());

    }

    private static void test_JBEAP_13215() throws Throwable {
        TestConfig.SERVER server1 = TestConfig.SERVER.NODE1;
        TestConfig.SERVER server2 = TestConfig.SERVER.NODE2;
        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;
        EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;

        System.out.println("https://issues.jboss.org/browse/JBEAP-13215");

        System.out.println("Standalone client -> new InitialContext( host:8080, host:8180, host:9180) -> EJB1");
        EJBRemoteNamingConfig callNode1Node2 = new EJBRemoteNamingConfig(Version.RemoteNamingHttpInitialContextFactory);
        callNode1Node2.setUsernamePassword(credential.username, credential.password);
        callNode1Node2.addProvider(server1.host, server1.remotingPort);
        callNode1Node2.addProvider(server2.host, server2.remotingPort);
        callNode1Node2.addProvider(server1.host, 9180);

        // try workflow of 1 action
        EJBRequest response = new EJBRequest("testClustering");

        // call clustered EJB - this EJBRemoteNamingConfig will default to WilflyInitialContextFactory
        EJBRemoteConfig callNode1 = new EJBRemoteNamingConfig(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.EJBUSER);
        // from EJB1 we will call EJB2 using RemoteNaming config
        EJBRemoteConfig callNode2 = new EJBRemoteNamingConfig(Version.RemoteNamingHttpInitialContextFactory, TestConfig.SERVER.NODE2, TestConfig.CREDENTIAL.EJBUSER);

        response.addWorkflow()
        // this will send a request to EJB1 on server1
            .addAction(callNode1Node2, TestConfig.EJBS.CLUSTERED_EJB1);

        response = response.invoke();
        response.throwIfAnyExceptions();
        System.out.println(response.getWorkflowsList());
        System.out.println(response.getResponseInvocationPath());
    }

    private static void testSFSB() {
        TestConfig.SERVER server1 = TestConfig.SERVER.NODE1;
        TestConfig.SERVER server2 = TestConfig.SERVER.NODE2;
        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;
        TestConfig.EJBS sfsb = TestConfig.EJBS.SFSB;

        EJBRemoteNamingConfig callNode1Node2 = new EJBRemoteNamingConfig(Version.RemoteNamingHttpInitialContextFactory);
        callNode1Node2.setUsernamePassword(credential.username, credential.password);
        callNode1Node2.addProvider(server1.host, server1.remotingPort);
        callNode1Node2.addProvider(server2.host, server2.remotingPort);

        EJBRequest response = new EJBRequest("testSFSB");
        response.addWorkflow().addAction(callNode1Node2, sfsb);
        // test SFSB sticky
        // have cluster servers
        // how to invoke remove ? (EJBRemoveAction?)

        // test WorkflowAction



    }

//    private static void test_JBEAP_12285() {
//      System.out.println("test_JBEAP_12285 Test");
//      System.out.println("---------------------------------");
//      TestConfig.SERVER server1 = TestConfig.SERVER.NODE1;
//      TestConfig.SERVER server2 = TestConfig.SERVER.NODE2;
//      TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;
//      EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;
//
//      RemoteNamingConfig remoteNaming = new RemoteNamingConfig(Version.WildflyInitialContextFactory);
//      // set provider
//      // set provider
//      remoteNaming.setHost(server1.host);
//      remoteNaming.setPort(server1.remotingPort);
//
//      // set user / pass
//      remoteNaming.setUsername(credential.username);
//      remoteNaming.setPassword(credential.password);
//
//      // add a 2nd
//      remoteNaming.addProvider(server2.host, server2.remotingPort);
//
//      remoteNaming.listConfiguration(System.out);
//
//      Context ctx = null;
//      try {
//          ctx = remoteNaming.getInitialContext();
//
//          EJBRequest response = new EJBRequest();
//          // we expect to see node1 and node2 invoked
//          for (int i = 0; i < 10; i++) {
//              System.out.println("Lookup: " + ejbInfo.getRemoteLookupPath());
//              ClusterSLSBRemote remote = (ClusterSLSBRemote) ctx.lookup(ejbInfo.getRemoteLookupPath());
//              response = remote.invoke(response);
//          }
//      } catch(Exception e) {
//          e.printStackTrace();
//      }
//
//      System.out.println("---------------------------------");
//    }

    private static void testMockObjects() {
        EJBRequest response = new EJBRequest();
        response.addWorkflow("Main")
        // send a request to Node1
//        .addAction(node1, TestConfig.EJBS.CLUSTERED_EJB1)
        // have node 1 invoke node 2 twice
        .addWorkflowAction("WFAction1")
            .addAction(new MockEJBAction("MockAction1"))
            .addAction(new MockEJBAction("MockAction2")).end()
        .addWorkflowAction("WFAction2")
            .addAction(new MockEJBAction("MockAction2"))
            .addAction(new MockEJBAction("MockAction1")).end()
        .addWorkflowAction("WFAction3")
            .addAction(new MockEJBAction("MockAction2"))
            .addAction(new MockEJBAction("MockAction1")).end()
        .addWorkflowAction("WFAction4")
            .addAction(new MockEJBAction("MockAction2"))
            .addAction(new MockEJBAction("MockAction1")).end();
        //
//      .addWorkflowAction("WFAction1")
//      .addAction(new EchoAction("Message1"))
//      .addAction(new EchoAction("Message2")).end()
//  .addWorkflowAction("WFAction2")
//      .addAction(new EchoAction("Message3"))
//      .addAction(new EchoAction("Message4"))
//      .addAction(new EchoAction("Message5")).end();

    }

    // create a ServletAction & fix the servlet serialization via JAXB
    // test clustering to SLSB with no ejb name set
    // test clustering to SFSB with no ejb name set

    private static void unsetNodeName() {
        System.clearProperty("jboss.node.name");
    }
    private static void setNodeName(String nodeName) {
        System.setProperty("jboss.node.name", nodeName);
    }
    private static void setNodeName() {
        setNodeName("TestClient");
    }

    // test sticky transactions - invoking on the same proxy
    public static void testStickyTransactionsWhenSameProxy() {
        // need to invoke a cluster

        String info = "This tests standalone client using WildflyInitialContextFactory with 2 servers listed in the providerURL. " +
        "The standalone client will invoke the EJB with REQUIRES_NEW, then that EJB will use WildflyInitialContextFactory with 2 servers " +
        "listed in the providerURL to invoke an EJB with MANDATORY 50 times and the expecation is the same node is invoked (sticky tx)";

        EJBRemoteNamingConfig nodeOne = new EJBRemoteNamingConfig(Version.WildflyInitialContextFactory);
        nodeOne.setUsernamePassword(TestConfig.CREDENTIAL.EJBUSER.username, TestConfig.CREDENTIAL.EJBUSER.password);
        nodeOne.addProvider(TestConfig.SERVER.NODE1.host, TestConfig.SERVER.NODE1.remotingPort);

        EJBRemoteNamingConfig nodeTwo = new EJBRemoteNamingConfig(Version.WildflyInitialContextFactory);
        nodeTwo.setUsernamePassword(TestConfig.CREDENTIAL.EJBUSER.username, TestConfig.CREDENTIAL.EJBUSER.password);
        nodeTwo.addProvider(TestConfig.SERVER.NODE2.host, TestConfig.SERVER.NODE2.remotingPort);

        // this is not a cluster but will probably load balance
        EJBRemoteNamingConfig twoNodes = new EJBRemoteNamingConfig(Version.WildflyInitialContextFactory);
        twoNodes.setUsernamePassword(TestConfig.CREDENTIAL.EJBUSER.username, TestConfig.CREDENTIAL.EJBUSER.password);
        twoNodes.addProvider(TestConfig.SERVER.NODE1.host, TestConfig.SERVER.NODE1.remotingPort);
        twoNodes.addProvider(TestConfig.SERVER.NODE2.host, TestConfig.SERVER.NODE2.remotingPort);

        try {
            setNodeName(); // set a name for the test which will show up in the invocation path
            EJBRequest response = new EJBRequest();
            response.addWorkflow("WF1")
                .addWorkflowAction("WFAction1")
//                    .addAction(twoNodes, TestConfig.EJBS.CLUSTERED_EJB1)
                    .addAction(EJBAction.build(nodeOne, TestConfig.EJBS.CLUSTERED_EJB1.info).setTx(Tx.REQUIRED))
                    .addRepeatedEJBAction(10, true, twoNodes, TestConfig.EJBS.CLUSTERED_EJB1, Tx.MANDATORY).end();
            response.addWorkflow("WF2")
                .addWorkflowAction("WFAction2")
//                    .addAction(twoNodes, TestConfig.EJBS.CLUSTERED_EJB1)
                    .addAction(EJBAction.build(nodeTwo, TestConfig.EJBS.CLUSTERED_EJB1.info).setTx(Tx.REQUIRED))
                    .addRepeatedEJBAction(10, true, twoNodes, TestConfig.EJBS.CLUSTERED_EJB1, Tx.MANDATORY).end();


            response = response.invoke();

//          .addAction(EJBAction.build(twoNodes, TestConfig.EJBS.CLUSTERED_EJB1.info).setReuseCachedProxy(true))
            System.out.println(response.getResponseInvocationPath());

            System.out.println("In this use case, it is expected wasSticky returns true and wasClustered returns false");
            System.out.println("The test is run 4 times and each is checked to confirm each was sticky");
            System.out.println("Run 1 - wasNodeSticky: " + Asserts.isWorkflowSticky(response, 0, 1));
            System.out.println("Run 1 - wasTxSticky: " + Asserts.isWorkflowTxSticky(response, 0, 0));
            System.out.println("Run 2 - wasNodeSticky: " + Asserts.isWorkflowSticky(response, 1, 1));
            System.out.println("Run 2 - wasTxSticky: " + Asserts.isWorkflowTxSticky(response, 1, 0));

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            unsetNodeName();
        }
    }

    // test SSL security propagation
    // test cluster separation
    // test Wolf's UserTranaction to JPA sticky transaction



    private static void testClustering() throws Exception {
        EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;
        EJBInfo ejbJBossEjbClientXml = TestConfig.EJBS.CLUSTERED_EJB1_JBOSS_EJB_CLIENT_XML.info;
        EJBInfo ejbWildflyClientXml = TestConfig.EJBS.CLUSTERED_EJB1_WILDFLY_CLIENT_XML.info;
        TestConfig.SERVER server = TestConfig.SERVER.NODE1;
        TestConfig.CLUSTER cluster = TestConfig.CLUSTER.CLUSTER1;
        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;

        System.out.println("testClustering"); System.out.flush();
        // try workflow of 1 action
        EJBRequest response = new EJBRequest("testClustering");

        // call clustered EJB
        EJBRemoteConfig node1 = new EJBRemoteNamingConfig(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.EJBUSER);
        EJBRemoteConfig node2 = new EJBRemoteNamingConfig(TestConfig.SERVER.NODE2, TestConfig.CREDENTIAL.EJBUSER);

        System.setProperty("jboss.node.name", "TestClient");
//        Action action = new MockEJBAction("Hello");
////      .addAction(new RepeatedAction(10, action)).end();
        response.addWorkflow("Main")
        // send a request to Node1
        // have node 1 invoke node 2 twice
        .addWorkflowAction("WFAction1")
            .addAction(node1, TestConfig.EJBS.CLUSTERED_EJB1)
            .addAction(node2, TestConfig.EJBS.CLUSTERED_EJB1)
            .addRepeatedEJBAction(5, node1, TestConfig.EJBS.CLUSTERED_EJB1).end();
//        .addWorkflowAction("WFAction2")
//            .addRepeatedEJBAction(20, node2, TestConfig.EJBS.CLUSTERED_EJB1).end();

        // a repeated action invokes from the same location.
        // a repeated workflow might make sense?

//        response.addWorkflow("Main")
//            // send a request to Node1
//            // have node 1 invoke node 2 twice
//            .addWorkflowAction("WFAction1")
//                .addAction(node1, TestConfig.EJBS.CLUSTERED_EJB1)
//                .addAction(node2, TestConfig.EJBS.CLUSTERED_EJB1).end()
//            .addWorkflowAction("WFAction2")
//                .addAction(node2, TestConfig.EJBS.CLUSTERED_EJB1)
//                .addAction(node1, TestConfig.EJBS.CLUSTERED_EJB1).end()
//            .addWorkflowAction("WFAction3")
//                .addAction(node1, TestConfig.EJBS.CLUSTERED_EJB1).end()
//            .addWorkflowAction("WFAction4")
//                .addAction(node2, TestConfig.EJBS.CLUSTERED_EJB1).end();

        response = response.invoke();

//        Asserts.assertWorkflowClustered(response, 0);
//        System.out.println(response.getResponseInvocationPathByWorkflow());
        System.out.println(response.getResponseInvocationPath());


    }

    private static void testGetClusterNameHack() {
        // setup InitialContext to call one of the nodes
        // have the EJB use JMX to get the cluster name and return it
        // setup the InitialContext again with the cluster name this time
        // alternatively use JMX or JBoss Management API to retrieve the cluster name of the server instance and then use it in the InitialContext
    }

//    private static void testWildflyNamingInvocationTimeout() throws Exception {
//        TestConfig.SERVER server = TestConfig.SERVER.NODE1;
//        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;
//        EJBInfo ejbInfo = TestConfig.EJBS.SLSB.info;
//        RemoteNamingConfig remoteNaming = new RemoteNamingConfig(Version.WildflyInitialContextFactory);
//        remoteNaming.setClusterName("ejb");
//        // set provider
//        remoteNaming.setHost(server.host);
//        remoteNaming.setPort(server.remotingPort);
//
//        // set user / pass
//        remoteNaming.setUsername(credential.username);
//        remoteNaming.setPassword(credential.password);
//
//        remoteNaming.setInvocationTimeout(1000L);
//
//        Context ctx = null;
//        try {
//            ctx = remoteNaming.getInitialContext();
//            remoteNaming.getConfiguration().list(System.out);
//            System.out.println("Lookup: " + ejbInfo.getRemoteLookupPath());
//            SLSBRemote remote = (SLSBRemote) ctx.lookup(ejbInfo.getRemoteLookupPath());
//            System.out.println("SleepReturned: " + remote.sleep(5000));
//
//        } catch(Throwable t) {
//            t.printStackTrace();
//        } finally {
//            EJBUtil.closeSafe(ctx);
//        }
//
//    }

//    private static void wildflyNamingStickyTransactions() throws Exception {
//        TestConfig.SERVER server = TestConfig.SERVER.NODE1;
//        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;
//        EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;
//
////        RemoteNamingConfig remoteNaming = new RemoteNamingConfig(Version.WildflyInitialContextFactory);
////        remoteNaming.setClusterName("ejb");
////        // set provider
////        remoteNaming.setHost(server.host);
////        remoteNaming.setPort(server.remotingPort);
////
////        // set user / pass
////        remoteNaming.setUsername(credential.username);
////        remoteNaming.setPassword(credential.password);
//
//        RemoteEJBConfig remoteEJBConfigNode1 = new RemoteEJBConfig(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.EJBUSER);
//        EJBRequest response = new EJBRequest("testClustering");
//        response.addWorkflow().addAction(true, remoteEJBConfigNode1, TestConfig.EJBS.CLUSTERED_EJB1);
//        for(int i=0; i<10; i++)
//            response.addPreviousWorkflow(true, TestConfig.Tx.REQUIRED); // invoke the same action using the same proxy and context
//
//        // call EJB then have it invoke a remote ejb several times using same proxy
////        response.addWorkflow().addAction(true, remoteEJBConfigNode1, TestConfig.EJBS.CLUSTERED_EJB1)
////            .addWorkflow().addAction()
////        for(int i=0; i<10; i++)
////            response.addPreviousWorkflow(true, TestConfig.Tx.REQUIRED); // invoke the same action using the same proxy and context
//
//        // TODO by default have actions reuse context
//        // by default can we reuse the proxy? Need to test if the proxy is the same as the new EJB being invoked
//        // have switch when adding a workflow or action to default to resue proxy / context if available
//
//        // an action goes across multiple nodes
//        // a workflow all starts at the origin of the calls
//
//        Context ctx = null;
//        try {
//            // this will invoke the 2 workflows
//            response = response.invoke();
//            System.out.println("isAllWorkflowsSticky: " + Asserts.isAllWorkflowsSticky(response));
//            System.out.println(response.getResponseInvocationPath());
//        } catch(Throwable t) {
//            t.printStackTrace();
//        } finally {
//
//        }
//    }

//    private static void wildflyNaming() throws Exception {
//        TestConfig.SERVER server = TestConfig.SERVER.NODE1;
//        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;
//        EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;
//        RemoteNamingConfig remoteNaming = new RemoteNamingConfig(Version.WildflyInitialContextFactory);
////        remoteNaming.setClusterName("ejb");
//        // set provider
//        remoteNaming.setHost(server.host);
//        remoteNaming.setPort(server.remotingPort);
//
//        // set user / pass
//        remoteNaming.setUsername(credential.username);
//        remoteNaming.setPassword(credential.password);
//
//        TestReport report = new TestReport("wildflyNaming", ClientType.StandaloneApp, EjbConfigMethod.RemoteNaming, ejbInfo.getRemoteNamingLookupPath());
//        report.setConfiguration(remoteNaming.getConfiguration());
//        Context ctx = null;
//        try {
//            ctx = remoteNaming.getInitialContext();
//            remoteNaming.getConfiguration().list(System.out);
//            // invocation.invoke(ctx, ctx.lookup(ejbInfo.getRemoteNamingLookupPath()), report);
//            System.out.println("Lookup: " + ejbInfo.getRemoteLookupPath());
//            ClusterSLSBRemote remote = (ClusterSLSBRemote) ctx.lookup(ejbInfo.getRemoteLookupPath());
//            // SLSBTest/SLSBEJB/ClusterSLSBEJB!org.jboss.reproducer.ejb.api.slsb.ClusterSLSBRemote
////            System.out.println("ClusterName: " + remote.getClusterName());
//            EJBRequest response = new EJBRequest();
//
//            // call clustered EJB
//
//            for (int i = 0; i < 500; i++)
//                response = remote.invoke(response);
//            logSummary("TestClient", response);
//
//        } catch(Throwable t) {
//            report.setException(t);
//            t.printStackTrace();
//        } finally {
//            EJBUtil.closeSafe(ctx);
//        }
//    }

//    @BeforeClass
//    public static void beforeClass() throws Exception {
//        // trigger the static init of the correct proeprties file - this also depends on running in forkMode=always
//        JBossEJBProperties ejbProperties = JBossEJBProperties.fromClassPath(SimpleInvocationTestCase.class.getClassLoader(), PROPERTIES_FILE);
//        JBossEJBProperties.getContextManager().setGlobalDefault(ejbProperties);
//    }


//    private static void remoteNaming() throws Exception {
//        TestConfig.SERVER server = TestConfig.SERVER.NODE1;
//        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;
//        EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;
//        RemoteNamingConfig remoteNaming = new RemoteNamingConfig(Version.RemoteNamingHttpInitialContextFactory);
//        remoteNaming.setClusterName("ejb");
//        // set provider
//        remoteNaming.setHost(server.host);
//        remoteNaming.setPort(server.remotingPort);
//
//        // set user / pass
//        remoteNaming.setUsername(credential.username);
//        remoteNaming.setPassword(credential.password);
//
//        TestReport report = new TestReport("remoteNaming", ClientType.StandaloneApp, EjbConfigMethod.RemoteNaming, ejbInfo.getRemoteNamingLookupPath());
//        report.setConfiguration(remoteNaming.getConfiguration());
//        Context ctx = null;
//        try {
//            ctx = remoteNaming.getInitialContext();
//            remoteNaming.getConfiguration().list(System.out);
//            // invocation.invoke(ctx, ctx.lookup(ejbInfo.getRemoteNamingLookupPath()), report);
//            System.out.println("Lookup: " + ejbInfo.getRemoteNamingLookupPath());
//            ClusterSLSBRemote remote = (ClusterSLSBRemote) ctx.lookup(ejbInfo.getRemoteNamingLookupPath());
//            // SLSBTest/SLSBEJB/ClusterSLSBEJB!org.jboss.reproducer.ejb.api.slsb.ClusterSLSBRemote
//            System.out.println("ClusterName: " + remote.getClusterName());
//            EJBRequest response = new EJBRequest();
//            for (int i = 0; i < 500; i++)
//                response = remote.invoke(response);
//            logSummary("TestClient", response);
//
//        } catch(Throwable t) {
//            report.setException(t);
//            t.printStackTrace();
//        } finally {
//            EJBUtil.closeSafe(ctx);
//        }
//    }


//    private static void scopedContextWorksInEAP70(boolean addConnection2) throws Exception {
//        EJBRemoteConfig scopedContextConfig = new EJBRemoteConfig();
//        scopedContextConfig.addConnection("localhost", "8080", "ejbuser", "redhat1!");
//        if(addConnection2) // if true then add 2 connections to the initial invocation
//            scopedContextConfig.addConnection("localhost", "8180", "ejbuser", "redhat1!");
//        scopedContextConfig.addCluster("ejb", "ejbuser", "redhat1!", false, false);
//        scopedContextConfig.setScopedContext(true);
//        scopedContextConfig.listConfiguration(System.out);
//        Context ctx = new InitialContext(scopedContextConfig.getConfiguration());
//        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
//        EJBRequest response = new EJBRequest();
//        for (int i = 0; i < 500; i++)
//            response = clusterEJB.invoke(response);
//        logSummary("TestClient", response);
//        ctx.close();
//
//    }

    private static void logSummary(String msg, EJBRequest response) {
//        EJBInvocationSummary summary = new EJBInvocationSummary(response);
        boolean isSticky = Asserts.isWorkflowSticky(response, 0);
        String format = "%s - Is Sticky: %s - Invocations Per Node: %s - Nodes Invoked: %s\n";
        Collection<Integer> invocationsPerNode = Asserts.getInvocationsPerNode(response.getWorkflow(0).getInvocationPath());
        Collection<String> getNodesInvoked = Asserts.getNodesInvoked(response.getWorkflow(0).getInvocationPath());
        System.out.printf(format, msg, isSticky, invocationsPerNode, getNodesInvoked);
    }


}
