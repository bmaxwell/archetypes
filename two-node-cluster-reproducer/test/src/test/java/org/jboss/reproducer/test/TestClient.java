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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.reproducer.ejb.api.EJBInfo;
import org.jboss.reproducer.ejb.api.EJBRemoteConfig;
import org.jboss.reproducer.ejb.api.EJBRequest;
import org.jboss.reproducer.ejb.api.EJBUtil;
import org.jboss.reproducer.ejb.api.RemoteEJBConfig;
import org.jboss.reproducer.ejb.api.RemoteNamingConfig;
import org.jboss.reproducer.ejb.api.RemoteNamingConfig.Version;
import org.jboss.reproducer.ejb.api.TestConfig;
import org.jboss.reproducer.ejb.api.slsb.ClusterSLSBRemote;
import org.jboss.reproducer.ejb.api.slsb.SLSBRemote;
import org.jboss.reproducer.test.TestReport.ClientType;
import org.jboss.reproducer.test.TestReport.EjbConfigMethod;

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
    public static void main(String[] args) throws Exception {

        test_JBEAP_12285();

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

    private static void test_JBEAP_12285() {
      System.out.println("test_JBEAP_12285 Test");
      System.out.println("---------------------------------");
      TestConfig.SERVER server1 = TestConfig.SERVER.NODE1;
      TestConfig.SERVER server2 = TestConfig.SERVER.NODE2;
      TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;
      EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;

      RemoteNamingConfig remoteNaming = new RemoteNamingConfig(Version.WildflyInitialContextFactory);
      // set provider
      // set provider
      remoteNaming.setHost(server1.host);
      remoteNaming.setPort(server1.remotingPort);

      // set user / pass
      remoteNaming.setUsername(credential.username);
      remoteNaming.setPassword(credential.password);

      // add a 2nd
      remoteNaming.addProvider(server2.host, server2.remotingPort);

      remoteNaming.listConfiguration(System.out);

      Context ctx = null;
      try {
          ctx = remoteNaming.getInitialContext();

          EJBRequest response = new EJBRequest();
          // we expect to see node1 and node2 invoked
          for (int i = 0; i < 10; i++) {
              System.out.println("Lookup: " + ejbInfo.getRemoteLookupPath());
              ClusterSLSBRemote remote = (ClusterSLSBRemote) ctx.lookup(ejbInfo.getRemoteLookupPath());
              response = remote.invoke(response);
          }
      } catch(Exception e) {
          e.printStackTrace();
      }

      System.out.println("---------------------------------");
    }

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
        RemoteEJBConfig remoteEJBConfigNode1 = new RemoteEJBConfig(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.EJBUSER);

        response.addWorkflow()
            .addAction(remoteEJBConfigNode1, TestConfig.EJBS.CLUSTERED_EJB1);

        // we expect to see node1 and node2 invoked
        for (int i = 0; i < 100; i++) {
            response = response.invoke();
        }

        Asserts.assertWorkflowClustered(response, 0);
        System.out.println(response.getResponseInvocationPath());

    }

    private static void testGetClusterNameHack() {
        // setup InitialContext to call one of the nodes
        // have the EJB use JMX to get the cluster name and return it
        // setup the InitialContext again with the cluster name this time
        // alternatively use JMX or JBoss Management API to retrieve the cluster name of the server instance and then use it in the InitialContext
    }

    private static void testWildflyNamingInvocationTimeout() throws Exception {
        TestConfig.SERVER server = TestConfig.SERVER.NODE1;
        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;
        EJBInfo ejbInfo = TestConfig.EJBS.SLSB.info;
        RemoteNamingConfig remoteNaming = new RemoteNamingConfig(Version.WildflyInitialContextFactory);
        remoteNaming.setClusterName("ejb");
        // set provider
        remoteNaming.setHost(server.host);
        remoteNaming.setPort(server.remotingPort);

        // set user / pass
        remoteNaming.setUsername(credential.username);
        remoteNaming.setPassword(credential.password);

        remoteNaming.setInvocationTimeout(1000L);

        Context ctx = null;
        try {
            ctx = remoteNaming.getInitialContext();
            remoteNaming.getConfiguration().list(System.out);
            System.out.println("Lookup: " + ejbInfo.getRemoteLookupPath());
            SLSBRemote remote = (SLSBRemote) ctx.lookup(ejbInfo.getRemoteLookupPath());
            System.out.println("SleepReturned: " + remote.sleep(5000));

        } catch(Throwable t) {
            t.printStackTrace();
        } finally {
            EJBUtil.closeSafe(ctx);
        }

    }

    private static void wildflyNamingStickyTransactions() throws Exception {
        TestConfig.SERVER server = TestConfig.SERVER.NODE1;
        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;
        EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;

//        RemoteNamingConfig remoteNaming = new RemoteNamingConfig(Version.WildflyInitialContextFactory);
//        remoteNaming.setClusterName("ejb");
//        // set provider
//        remoteNaming.setHost(server.host);
//        remoteNaming.setPort(server.remotingPort);
//
//        // set user / pass
//        remoteNaming.setUsername(credential.username);
//        remoteNaming.setPassword(credential.password);

        RemoteEJBConfig remoteEJBConfigNode1 = new RemoteEJBConfig(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.EJBUSER);
        EJBRequest response = new EJBRequest("testClustering");
        response.addWorkflow().addAction(true, remoteEJBConfigNode1, TestConfig.EJBS.CLUSTERED_EJB1);
        for(int i=0; i<10; i++)
            response.addPreviouWorkflow(true, TestConfig.Tx.REQUIRED); // invoke the same action using the same proxy and context

        // an action goes across multiple nodes
        // a workflow all starts at the origin of the calls

        Context ctx = null;
        try {
            // this will invoke the 2 workflows
            response = response.invoke();
            System.out.println("isAllWorkflowsSticky: " + Asserts.isAllWorkflowsSticky(response));
            System.out.println(response.getResponseInvocationPath());
        } catch(Throwable t) {
            t.printStackTrace();
        } finally {

        }
    }

    private static void wildflyNaming() throws Exception {
        TestConfig.SERVER server = TestConfig.SERVER.NODE1;
        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;
        EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;
        RemoteNamingConfig remoteNaming = new RemoteNamingConfig(Version.WildflyInitialContextFactory);
//        remoteNaming.setClusterName("ejb");
        // set provider
        remoteNaming.setHost(server.host);
        remoteNaming.setPort(server.remotingPort);

        // set user / pass
        remoteNaming.setUsername(credential.username);
        remoteNaming.setPassword(credential.password);

        TestReport report = new TestReport("wildflyNaming", ClientType.StandaloneApp, EjbConfigMethod.RemoteNaming, ejbInfo.getRemoteNamingLookupPath());
        report.setConfiguration(remoteNaming.getConfiguration());
        Context ctx = null;
        try {
            ctx = remoteNaming.getInitialContext();
            remoteNaming.getConfiguration().list(System.out);
            // invocation.invoke(ctx, ctx.lookup(ejbInfo.getRemoteNamingLookupPath()), report);
            System.out.println("Lookup: " + ejbInfo.getRemoteLookupPath());
            ClusterSLSBRemote remote = (ClusterSLSBRemote) ctx.lookup(ejbInfo.getRemoteLookupPath());
            // SLSBTest/SLSBEJB/ClusterSLSBEJB!org.jboss.reproducer.ejb.api.slsb.ClusterSLSBRemote
//            System.out.println("ClusterName: " + remote.getClusterName());
            EJBRequest response = new EJBRequest();

            // call clustered EJB

            for (int i = 0; i < 500; i++)
                response = remote.invoke(response);
            logSummary("TestClient", response);

        } catch(Throwable t) {
            report.setException(t);
            t.printStackTrace();
        } finally {
            EJBUtil.closeSafe(ctx);
        }
    }

//    @BeforeClass
//    public static void beforeClass() throws Exception {
//        // trigger the static init of the correct proeprties file - this also depends on running in forkMode=always
//        JBossEJBProperties ejbProperties = JBossEJBProperties.fromClassPath(SimpleInvocationTestCase.class.getClassLoader(), PROPERTIES_FILE);
//        JBossEJBProperties.getContextManager().setGlobalDefault(ejbProperties);
//    }


    private static void remoteNaming() throws Exception {
        TestConfig.SERVER server = TestConfig.SERVER.NODE1;
        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;
        EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;
        RemoteNamingConfig remoteNaming = new RemoteNamingConfig(Version.RemoteNamingHttpInitialContextFactory);
        remoteNaming.setClusterName("ejb");
        // set provider
        remoteNaming.setHost(server.host);
        remoteNaming.setPort(server.remotingPort);

        // set user / pass
        remoteNaming.setUsername(credential.username);
        remoteNaming.setPassword(credential.password);

        TestReport report = new TestReport("remoteNaming", ClientType.StandaloneApp, EjbConfigMethod.RemoteNaming, ejbInfo.getRemoteNamingLookupPath());
        report.setConfiguration(remoteNaming.getConfiguration());
        Context ctx = null;
        try {
            ctx = remoteNaming.getInitialContext();
            remoteNaming.getConfiguration().list(System.out);
            // invocation.invoke(ctx, ctx.lookup(ejbInfo.getRemoteNamingLookupPath()), report);
            System.out.println("Lookup: " + ejbInfo.getRemoteNamingLookupPath());
            ClusterSLSBRemote remote = (ClusterSLSBRemote) ctx.lookup(ejbInfo.getRemoteNamingLookupPath());
            // SLSBTest/SLSBEJB/ClusterSLSBEJB!org.jboss.reproducer.ejb.api.slsb.ClusterSLSBRemote
            System.out.println("ClusterName: " + remote.getClusterName());
            EJBRequest response = new EJBRequest();
            for (int i = 0; i < 500; i++)
                response = remote.invoke(response);
            logSummary("TestClient", response);

        } catch(Throwable t) {
            report.setException(t);
            t.printStackTrace();
        } finally {
            EJBUtil.closeSafe(ctx);
        }
    }


    private static void scopedContextWorksInEAP70(boolean addConnection2) throws Exception {
        EJBRemoteConfig scopedContextConfig = new EJBRemoteConfig();
        scopedContextConfig.addConnection("localhost", "8080", "ejbuser", "redhat1!");
        if(addConnection2) // if true then add 2 connections to the initial invocation
            scopedContextConfig.addConnection("localhost", "8180", "ejbuser", "redhat1!");
        scopedContextConfig.addCluster("ejb", "ejbuser", "redhat1!", false, false);
        scopedContextConfig.setScopedContext(true);
        scopedContextConfig.listConfiguration(System.out);
        Context ctx = new InitialContext(scopedContextConfig.getConfiguration());
        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
        EJBRequest response = new EJBRequest();
        for (int i = 0; i < 500; i++)
            response = clusterEJB.invoke(response);
        logSummary("TestClient", response);
        ctx.close();

    }

    private static void logSummary(String msg, EJBRequest response) {
//        EJBInvocationSummary summary = new EJBInvocationSummary(response);
        boolean isSticky = Asserts.isWorkflowSticky(response, 0);
        String format = "%s - Is Sticky: %s - Invocations Per Node: %s - Nodes Invoked: %s\n";
        Collection<Integer> invocationsPerNode = Asserts.getInvocationsPerNode(response.getWorkflow(0).getInvocationPath());
        Collection<String> getNodesInvoked = Asserts.getNodesInvoked(response.getWorkflow(0).getInvocationPath());
        System.out.printf(format, msg, isSticky, invocationsPerNode, getNodesInvoked);
    }


}
