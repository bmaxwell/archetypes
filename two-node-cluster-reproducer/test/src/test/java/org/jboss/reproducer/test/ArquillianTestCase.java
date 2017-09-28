/**c
 *
 */
package org.jboss.reproducer.test;

import java.io.InputStream;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ContainerResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.reproducer.ejb.api.EJBInfo;
import org.jboss.reproducer.ejb.api.EJBInvocationSummary;
import org.jboss.reproducer.ejb.api.EJBRemoteConfig;
import org.jboss.reproducer.ejb.api.EJBRemoteNamingConfig;
import org.jboss.reproducer.ejb.api.EJBRemoteNamingConfig.Version;
import org.jboss.reproducer.ejb.api.EJBRemoteScopedContextConfig;
import org.jboss.reproducer.ejb.api.EJBRequest;
import org.jboss.reproducer.ejb.api.EJBUtil;
import org.jboss.reproducer.ejb.api.Results;
import org.jboss.reproducer.ejb.api.TestConfig;
import org.jboss.reproducer.ejb.api.path.EJBAction;
import org.jboss.reproducer.ejb.api.path.Workflow;
import org.jboss.reproducer.ejb.api.slsb.ClusterSLSBRemote;
import org.jboss.reproducer.ejb.api.slsb.SLSBRemote;
import org.jboss.reproducer.test.Deployments.DeploymentDescriptor;
import org.jboss.reproducer.test.TestReport.ClientType;
import org.jboss.reproducer.test.TestReport.EjbConfigMethod;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;



/**
 * @author bmaxwell
 *
 */
//TODO Add byteman example
@RunWith(Arquillian.class)
//@ServerSetup(ConfigureServer.class)
@RunAsClient
public class ArquillianTestCase {

	@ContainerResource("node1")
	protected ManagementClient node1;

	@ContainerResource("node2")
    protected ManagementClient node2;

    private static String applicationName = "SLSBTest";
    private static EJBInfo slsbInfo = new EJBInfo(applicationName, "SLSBEJB", "SLSBEJB", "org.jboss.reproducer.ejb.slsb", SLSBRemote.class);
    private static EJBInfo mdbInfo = new EJBInfo(applicationName, "MDB", "MDB", "org.jboss.reproducer.ejb.mdb", null);

    private static EJBInfo clusterEJBInfo = new EJBInfo(applicationName, "SLSBEJB", "ClusterSLSBEJB", "org.jboss.reproducer.ejb.slsb", ClusterSLSBRemote.class);

    public static final String TEST_QUEUE = "/queue/TestQueue";

	// Setup
	// run standalone-full.xml
	// configure arquillian.xml
	// <jms-queue name="TestQueue" entries="java:/jms/queue/TestQueue,java:jboss/exported/queue/TestQueue"/>

    private static Archive<?> createEarDeployment1() {
        Archive<?>[] earLib = new Archive<?>[] { Deployments.SLSB_API, Deployments.MDB_API, Deployments.EJB_API };


        Archive<?>[] subDeployments = new Archive<?>[] {
            Deployments.createEjbSubDeployment(slsbInfo), Deployments.createEjbSubDeployment(mdbInfo)
        };

        return Deployments.createEarDeployment(TestConfig.APPLICATION.SLSBTest.name, earLib, subDeployments);
    }

    private static Archive<?> createEarDeployment2_JBossEjbClientXml() {
        Archive<?>[] earLib = new Archive<?>[] { Deployments.SLSB_API, Deployments.MDB_API, Deployments.EJB_API };

        DeploymentDescriptor jbossEjbClientXml = new Deployments.JBossEjbClientXml(Deployments.getEjbClientXml(false, "remote-ejb-connection"));

        Archive<?>[] subDeployments = new Archive<?>[] {
            Deployments.createEjbSubDeployment(slsbInfo), Deployments.createEjbSubDeployment(mdbInfo), Deployments.createWarDeployment("", true, TestConfig.SERVLET.CLIENT2.info)
        };

        return Deployments.createEarDeployment(TestConfig.APPLICATION.SLSBTest_JBOSS_EJB_CLIENT_XML.name, earLib, subDeployments, jbossEjbClientXml);
    }

    private static Archive<?> createEarDeployment3_WildflyClientXml() {
        Archive<?>[] earLib = new Archive<?>[] { Deployments.SLSB_API, Deployments.MDB_API, Deployments.EJB_API };

        DeploymentDescriptor jbossEjbClientXml = new Deployments.JBossEjbClientXml(Deployments.getEjbClientXml(false, "remote-ejb-connection"));

        Archive<?>[] subDeployments = new Archive<?>[] {
            Deployments.createEjbSubDeployment(slsbInfo), Deployments.createEjbSubDeployment(mdbInfo), Deployments.createWarDeployment("", true, TestConfig.SERVLET.CLIENT3.info)
        };

        return Deployments.createEarDeployment(TestConfig.APPLICATION.SLSBTest_WILDFLY_CLIENT_XML.name, earLib, subDeployments, jbossEjbClientXml);
    }



	@Deployment(name="EAR1-Node1", testable = false)
	@TargetsContainer("node1")
    public static Archive<?> createEarDeployment1_Node1() {
	    return createEarDeployment1();
    }

	@Deployment(name="EAR1-Node2", testable = false)
    @TargetsContainer("node2")
    public static Archive<?> createEarDeployment1_Node2() {
	    return createEarDeployment1();
	}


    @Deployment(name = "EAR2-Node1", testable = false)
    @TargetsContainer("node1")
    public static Archive<?> createEarDeployment2_Node1() {
        return createEarDeployment2_JBossEjbClientXml();
    }

    @Deployment(name = "EAR2-Node2", testable = false)
    @TargetsContainer("node2")
    public static Archive<?> createEarDeployment2_Node2() {
        return createEarDeployment2_JBossEjbClientXml();
    }


    @Deployment(name = "EAR3-Node1", testable = false)
    @TargetsContainer("node1")
    public static Archive<?> createEarDeployment3_Node1() {
        return createEarDeployment3_WildflyClientXml();
    }

    @Deployment(name = "EAR3-Node2", testable = false)
    @TargetsContainer("node2")
    public static Archive<?> createEarDeployment3_Node2() {
        return createEarDeployment3_WildflyClientXml();
    }

	/**
	 *
	 */
	public ArquillianTestCase() {
	}

	private static Context getInitialContext(String host, Integer port, String username, String password, String clusterAffinity) throws NamingException {
	    Properties props = new Properties();
	    props.put(Context.INITIAL_CONTEXT_FACTORY,  "org.wildfly.naming.client.WildFlyInitialContextFactory");
	    props.put(Context.PROVIDER_URL, String.format("%s://%s:%d", "remote+http", host, port));
	    props.put(Context.SECURITY_PRINCIPAL, username);
	    props.put(Context.SECURITY_CREDENTIALS, password);
	    if(clusterAffinity != null)
	        props.put("jboss.cluster-affinity", clusterAffinity);
	    props.list(System.out);
	    return new InitialContext(props);
	 }

	public static class InvocationMethods {

	    public interface Invocation<T> {
	        public TestReport invoke(Context context, T ejbProxy, TestReport report);
	    }

	    private EJBInfo ejbInfo;
	    private EJBInfo jbossEjbClientXmlEjb;
	    private EJBInfo wildflyClientXmlEjb;
	    private Invocation invocation;
        private TestConfig.SERVER server;
        private TestConfig.CLUSTER cluster;
        private TestConfig.CREDENTIAL credential;

        public InvocationMethods(EJBInfo ejbInfo, EJBInfo jbossEjbClientXmlEjb, EJBInfo wildflyClientXmlEjb,  Invocation invocation) {
            this(ejbInfo, jbossEjbClientXmlEjb, wildflyClientXmlEjb, invocation, null, null, null);
        }

	    public InvocationMethods(EJBInfo ejbInfo, EJBInfo jbossEjbClientXmlEjb, EJBInfo wildflyClientXmlEjb, Invocation invocation, TestConfig.SERVER server, TestConfig.CLUSTER cluster, TestConfig.CREDENTIAL credential) {
	        this.ejbInfo = ejbInfo;
	        this.jbossEjbClientXmlEjb = jbossEjbClientXmlEjb;
	        this.wildflyClientXmlEjb = wildflyClientXmlEjb;
	        this.invocation = invocation;
	        this.server = server;
	        this.cluster = cluster;
	        this.credential = credential;
	    }

//	    Option 1 - Configuring using scoped context in the client code
	    public TestReport option1_EjbClientScopedContext() throws NamingException {
	        // Scoped Context
	        EJBRemoteScopedContextConfig scopedContextConfig = new EJBRemoteScopedContextConfig();
	        scopedContextConfig.addConnection(server.host, server.remotingPort, credential.username, credential.password);//
	        scopedContextConfig.addCluster(cluster.name, cluster.username, cluster.password, false, false);
	        scopedContextConfig.setScopedContext(true);
	        scopedContextConfig.getConfiguration().list(System.out);
	        TestReport report = new TestReport("option1_EjbClientScopedContext", ClientType.StandaloneApp, EjbConfigMethod.EjbClientScopedContext, ejbInfo.getEJBClientLookupPath());
	        report.setConfiguration(scopedContextConfig.getConfiguration());
	        Context ctx = null;
	        try {
	            ctx = new InitialContext(scopedContextConfig.getConfiguration());
	            invocation.invoke(ctx, ctx.lookup(ejbInfo.getEJBClientLookupPath()), report);
	        } finally {
	            EJBUtil.closeSafeScopedContext(ctx);
	        }
	        return report;
	    }
//	    Option 2 - Configuring using remote: protocol in the client code RemoteNamingInitialContext (EAP 7.0/6.x)
	    public TestReport option2_RemoteNaming() throws NamingException {
	        EJBRemoteNamingConfig remoteNaming = new EJBRemoteNamingConfig(Version.RemoteNamingHttpInitialContextFactory);
            // set provider
            remoteNaming.setHost(server.host);
            remoteNaming.setPort(server.remotingPort);

            // set user / pass
            remoteNaming.setUsername(credential.username);
            remoteNaming.setPassword(credential.password);

            TestReport report = new TestReport("option2_RemoteNaming", ClientType.StandaloneApp, EjbConfigMethod.RemoteNaming, ejbInfo.getRemoteNamingLookupPath());
            report.setConfiguration(remoteNaming.getConfiguration());
	        Context ctx = null;
	        try {
	            ctx = remoteNaming.getInitialContext();
	            remoteNaming.getConfiguration().list(System.out);
	            invocation.invoke(ctx, ctx.lookup(ejbInfo.getRemoteNamingLookupPath()), report);
	        } catch(Throwable t) {
	            report.setException(t);
	        } finally {
	            EJBUtil.closeSafe(ctx);
	        }
	        return report;
	    }

//	    Option 3 - Configuring using WildflyInitialContextFactory (EAP 7.1) with credentials
	    public TestReport option3_WildflyNaming() throws NamingException {
	            EJBRemoteNamingConfig remoteNaming = new EJBRemoteNamingConfig(Version.WildflyInitialContextFactory);
	            // set provider
	            remoteNaming.setHost(server.host);
	            remoteNaming.setPort(server.remotingPort);

	            // set user / pass
	            remoteNaming.setUsername(credential.username);
	            remoteNaming.setPassword(credential.password);

	            TestReport report = new TestReport("option3_WildflyNaming", ClientType.StandaloneApp, EjbConfigMethod.WildflyNaming, ejbInfo.getRemoteLookupPath());
	            report.setConfiguration(remoteNaming.getConfiguration());
	            Context ctx = null;
	            try {
	                ctx = remoteNaming.getInitialContext();
	                invocation.invoke(ctx, ctx.lookup(ejbInfo.getRemoteLookupPath()), report);
	            } finally {
	                // EJBUtil.closeSafe(ctx); // no need to close
	            }
	            return report;
	        }

//	    Option 4 - Configuring remote servers in the JBoss profile (remote-ejb-connection) wildfly-client.xml
	    // method that creates InitialContext using the particular method, then invokes some code to test which is passed in
	    // have a method that tests all
        public TestReport option4_WildflyClientXml() throws NamingException {
            EJBRemoteNamingConfig remoteNaming = new EJBRemoteNamingConfig(Version.WildflyInitialContextFactory);
            // set nothing on the configuration as the wildfly-client.xml will point to a remote-ejb-connection in the JBoss profile for connections
            Context ctx = null;
            TestReport report = new TestReport("option4_WildflyClientXml", ClientType.Deployment, EjbConfigMethod.WildflyConfigXml, wildflyClientXmlEjb.getRemoteLookupPath());
            report.setConfiguration(remoteNaming.getConfiguration());
            try {

                // to use wildfly-client.xml have to invoke the servlet which invokes the remote ejb
                EJBRequest ejbRequest = new EJBRequest("wildfly-config.xml client");
                EJBRemoteConfig remoteEJBConfig = new EJBRemoteNamingConfig(TestConfig.SERVER.NODE2, TestConfig.CREDENTIAL.EJBUSER);
//                ejbRequest.getActions().add(new EJBAction(remoteEJBConfig, TestConfig.EJBS.CLUSTERED_EJB1_JBOSS_EJB_CLIENT_XML.info));
                Results results = invokeViaServlet(TestConfig.SERVER.NODE1, TestConfig.SERVLET.CLIENT3, TestConfig.CREDENTIAL.SERVLET1, ejbRequest);

                // ctx = remoteNaming.getInitialContext();
                // invocation.invoke(ctx, ctx.lookup(wildflyClientXmlEjb.getRemoteLookupPath()), report);
            } finally {
                // EJBUtil.closeSafe(ctx); // no need to close458971 need jboss-ejb-client.properties and
            }
            return report;
        }

//      Option 5 - Configuring remote servers in the JBoss profile (remote-ejb-connection) jboss-ejb-client.xml
//      jboss-ejb-client.xml goes in the WEB-INF if the top level deployment is a war or in the top level deployment's META-INF if not a war
        public TestReport option5_EjbClientJBossEjbClientXml() throws NamingException {
            EJBRemoteNamingConfig remoteNaming = new EJBRemoteNamingConfig(Version.None);
            // set nothing on the configuration as the wildfly-client.xml will point to a remote-ejb-connection in the JBoss profile for connections
            TestReport report = new TestReport("option1_EjbClientJBossEjbClientXml", ClientType.Deployment, EjbConfigMethod.JBossEjbClientXml, jbossEjbClientXmlEjb.getRemoteLookupPath());
            report.setConfiguration(remoteNaming.getConfiguration());
            Context ctx = null;
            try {

//                Results results = invokeViaServlet(TestConfig.SERVER.NODE1, TestConfig.SERVLET.CLIENT2, TestConfig.CREDENTIAL.SERVLET1, ejbRequest);

                ctx = remoteNaming.getInitialContext();
                invocation.invoke(ctx, ctx.lookup(jbossEjbClientXmlEjb.getRemoteLookupPath()), report);
            } finally {
                // EJBUtil.closeSafe(ctx); // no need to close
            }
            return report;
        }

        public TestReport option6_standaloneClientPropertiesFile() throws NamingException {
            // set nothing on the configuration as the jboss-ejb-client.properties
            Context ctx = null;
            TestReport report = new TestReport("option6_standaloneClientPropertiesFile", ClientType.Deployment, EjbConfigMethod.WildflyConfigXml, ejbInfo.getEJBClientLookupPath());

            try {
                Properties env = new Properties();
                env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
                report.setConfiguration(env);
                ctx = new InitialContext(env);
                invocation.invoke(ctx, ctx.lookup(ejbInfo.getEJBClientLookupPath()), report);
            } finally {
                // EJBUtil.closeSafe(ctx); // no need to close
            }
            return report;
        }
        // standalone clients need EJBClientContext.setSelector
//        public Object option7_legacy_eap70_standaloneClientEJBClientAPI() {
//
//            EJBRemoteConfig config = new EJBRemoteConfig();
//            config.addConnection(server.host, server.remotingPort, credential.username, credential.password);//
//            config.addCluster(cluster.name, cluster.username, cluster.password, false, false);
//            config.setScopedContext(true);
//            config.getConfiguration().list(System.out);
//
//            // these 2 lines require the legacy EJB client jar or need to be run in EAP 7.0 / 6.x
//            EJBClientConfiguration cc = new PropertiesBasedEJBClientConfiguration(config.getConfiguration());
//            ContextSelector<EJBClientContext> selector = new ConfigBasedEJBClientContextSelector(cc);
//            EJBClientContext.setSelector(selector);
//
//            Context ctx = null;
//            try {
//                Properties env = new Properties();
//                env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
//                ctx = new InitialContext(env);
//                return invocation.invoke(ctx, ctx.lookup(ejbInfo.getEJBClientLookupPath()));
//            } finally {
//                // EJBUtil.closeSafe(ctx); // no need to close
//            }
//        }
	}


    private InvocationMethods.Invocation TEST_EJB_INVOCATIONS_INVOKE_CLUSTERED = new InvocationMethods.Invocation<ClusterSLSBRemote>() {
        @Override
        public TestReport invoke(Context context, ClusterSLSBRemote ejbProxy, TestReport report) {

//            EJBRequest response = new EJBRequest("testEjbInvocationsInvokeClustered");
//            RemoteEJBConfig remoteEJBConfig = new RemoteEJBConfig(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.EJBUSER);
//            response.addPath(remoteEJBConfig, TestConfig.EJBS.CLUSTERED_EJB1);

            EJBRequest response = new EJBRequest("testEjbInvocationsInvokeClustered");
            EJBRemoteConfig remoteEJBConfig = new EJBRemoteNamingConfig(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.EJBUSER);
            EJBAction action = new EJBAction(remoteEJBConfig, TestConfig.EJBS.CLUSTERED_EJB1.info);

            // use case EJBRequest has an Action to invoke another EJB
            // use case Action that invokes
            // action.invoke() - could call a remote ejb or invoke something repeatidly - issue is when to remove action
            // the remove on the EJB side was to validate the roles, just send this has a map node=>{roles} - this is a problem if we change users in an action
            // request.add(
            // The result should contain the full invocation path
            // EJBRequest can have a currentAction#, Action is not needed at the end as ExpectedPath contains the info we will compare against.
            // action.invoke() can invoke an EJB, the EJB can call getExpectedRoles() -> getExpected(actionNumber)
            // now the action should be removed when it is invoked not on the EJB and the actionIndex should be incremented
            // how to handle invoking 100 times? add action over and over? or make a special one ?
            // set to level expectation of clustered or sticky at some point, need transaction level test, so
            // Wolf test client -> EJB1 -> EJB2 should be sticky
            // we need Invocation path separated by action
            //
            // examples:
            // new Request().addInvocationWorkFlow(
            // right now we have single actions which are not grouped
            // add ActionGroup
            // Use cases:
            // - single ejb invocation
            // - workflow that invokes more than 1 ejb
            // - a repeated invocation of an ejb
            // - repeated invocation of a workflow
            // a workflow has multiple actions

            Workflow callServer1ThenServer2 = new Workflow();


            //System.out.println("Brad: EJBRequest actions: " + response.getActions().size()); System.out.flush();
            for (int i = 0; i < 100; i++) {
//                response.getActions().add(action);
                response = ejbProxy.invoke(response);
            }
            logSummary("testCluster", response);
//            Asserts.failIfNotClustered(response.getInvocationPath());

            report.setResponse(response);
            return report;
        }
    };

    private InvocationMethods.Invocation TEST_EJB_INVOCATIONS_INVOKE_CLUSTERED_NEW = new InvocationMethods.Invocation<ClusterSLSBRemote>() {
        @Override
        public TestReport invoke(Context context, ClusterSLSBRemote ejbProxy, TestReport report) {

//            EJBRequest response = new EJBRequest("testEjbInvocationsInvokeClustered");
//            RemoteEJBConfig remoteEJBConfig = new RemoteEJBConfig(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.EJBUSER);
//            response.addPath(remoteEJBConfig, TestConfig.EJBS.CLUSTERED_EJB1);

            // try workflow of 1 action
            EJBRequest response = new EJBRequest("testEjbInvocationsInvokeClustered");
            EJBRemoteConfig remoteEJBConfig = new EJBRemoteNamingConfig(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.EJBUSER);
            response.addWorkflow(remoteEJBConfig, TestConfig.EJBS.CLUSTERED_EJB1);

            // use case EJBRequest has an Action to invoke another EJB
            // use case Action that invokes
            // action.invoke() - could call a remote ejb or invoke something repeatidly - issue is when to remove action
            // the remove on the EJB side was to validate the roles, just send this has a map node=>{roles} - this is a problem if we change users in an action
            // request.add(
            // The result should contain the full invocation path
            // EJBRequest can have a currentAction#, Action is not needed at the end as ExpectedPath contains the info we will compare against.
            // action.invoke() can invoke an EJB, the EJB can call getExpectedRoles() -> getExpected(actionNumber)
            // now the action should be removed when it is invoked not on the EJB and the actionIndex should be incremented
            // how to handle invoking 100 times? add action over and over? or make a special one ?
            // set to level expectation of clustered or sticky at some point, need transaction level test, so
            // Wolf test client -> EJB1 -> EJB2 should be sticky
            // we need Invocation path separated by action
            //
            // examples:
            // new Request().addInvocationWorkFlow(
            // right now we have single actions which are not grouped
            // add ActionGroup
            // Use cases:
            // - single ejb invocation
            // - workflow that invokes more than 1 ejb
            // - a repeated invocation of an ejb
            // - repeated invocation of a workflow
            // a workflow has multiple actions

            //System.out.println("Brad: EJBRequest actions: " + response.getActions().size()); System.out.flush();
            try {
                for (int i = 0; i < 100; i++) {
                    response = response.invokeWorkflow(0);
    //                response = ejbProxy.invoke(response);
                }
            } catch(Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            logSummary("testCluster", response);
//            Asserts.failIfNotClustered(response.getInvocationPath());

            report.setResponse(response);
            return report;
        }
    };

    @Ignore @Test // working
    public void testWorkflow() throws Exception {
        EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;
        EJBInfo ejbJBossEjbClientXml = TestConfig.EJBS.CLUSTERED_EJB1_JBOSS_EJB_CLIENT_XML.info;
        EJBInfo ejbWildflyClientXml = TestConfig.EJBS.CLUSTERED_EJB1_WILDFLY_CLIENT_XML.info;
        TestConfig.SERVER server = TestConfig.SERVER.NODE1;
        TestConfig.CLUSTER cluster = TestConfig.CLUSTER.CLUSTER1;
        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;

        System.out.println("testWorkFlow"); System.out.flush();
        // try workflow of 1 action
        EJBRequest response = new EJBRequest("testEjbInvocationsInvokeClustered");
        EJBRemoteConfig remoteEJBConfig = new EJBRemoteNamingConfig(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.EJBUSER);
        response.addWorkflow(remoteEJBConfig, TestConfig.EJBS.CLUSTERED_EJB1);

        // this will invoke the single workflow over and over, which just calls the clustered EJB
        // the response will accumulate the InvocationPath
        for (int i = 0; i < 100; i++) {
            response = response.invokeWorkflow(0);
        }

        System.out.println(response.getResponseInvocationPath());
        System.out.println("Invocation Path by Workflow");
        System.out.println(response.getResponseInvocationPathByWorkflow());
    }

    @Ignore @Test // Works
    public void testTwoWorkflows() throws Exception {
        EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;
        EJBInfo ejbJBossEjbClientXml = TestConfig.EJBS.CLUSTERED_EJB1_JBOSS_EJB_CLIENT_XML.info;
        EJBInfo ejbWildflyClientXml = TestConfig.EJBS.CLUSTERED_EJB1_WILDFLY_CLIENT_XML.info;
        TestConfig.SERVER server = TestConfig.SERVER.NODE1;
        TestConfig.CLUSTER cluster = TestConfig.CLUSTER.CLUSTER1;
        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;

        System.out.println("testWorkFlow"); System.out.flush();
        // try workflow of 1 action
        EJBRequest response = new EJBRequest("testEjbInvocationsInvokeClustered");

        // call clustered EJB
        EJBRemoteConfig remoteEJBConfigNode1 = new EJBRemoteNamingConfig(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.EJBUSER);
        EJBRemoteConfig remoteEJBConfigNode2 = new EJBRemoteNamingConfig(TestConfig.SERVER.NODE2, TestConfig.CREDENTIAL.EJBUSER);

        response.addWorkflow(remoteEJBConfigNode1, TestConfig.EJBS.CLUSTERED_EJB1);

        // call SLSB from Clustered SLSB
        response.addWorkflow(remoteEJBConfigNode2, TestConfig.EJBS.SLSB);

        System.out.println("Workflows:" + response.getWorkflowsList());

        // this will invoke the single workflow over and over, which just calls the clustered EJB
        // the response will accumulate the InvocationPath
        for (int i = 0; i < 10; i++) {
            response = response.invoke();
        }

        System.out.println(response.getResponseInvocationPath());
        System.out.println("Invocation Path by Workflow");
        System.out.println(response.getResponseInvocationPathByWorkflow());
    }

    @Ignore @Test
    public void testOneWorkflowWithTwoActions() throws Exception {
        EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;
        EJBInfo ejbJBossEjbClientXml = TestConfig.EJBS.CLUSTERED_EJB1_JBOSS_EJB_CLIENT_XML.info;
        EJBInfo ejbWildflyClientXml = TestConfig.EJBS.CLUSTERED_EJB1_WILDFLY_CLIENT_XML.info;
        TestConfig.SERVER server = TestConfig.SERVER.NODE1;
        TestConfig.CLUSTER cluster = TestConfig.CLUSTER.CLUSTER1;
        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;

        System.out.println("testWorkFlow"); System.out.flush();
        // try workflow of 1 action
        EJBRequest response = new EJBRequest("testEjbInvocationsInvokeClustered");

        // call clustered EJB
        EJBRemoteConfig remoteEJBConfigNode1 = new EJBRemoteNamingConfig(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.EJBUSER);
        EJBRemoteConfig remoteEJBConfigNode2 = new EJBRemoteNamingConfig(TestConfig.SERVER.NODE2, TestConfig.CREDENTIAL.EJBUSER);

        response.addWorkflow()
            .addAction(remoteEJBConfigNode1, TestConfig.EJBS.CLUSTERED_EJB1)
            .addAction(remoteEJBConfigNode2, TestConfig.EJBS.SLSB);

        // do we need expected invocation path? I think we can just loop through the Workflow/Actions and compare the invocation Path to it
        // nodeName/user will be there

        System.out.println("Workflows:" + response.getWorkflowsList());

        // this will invoke the single workflow over and over, which just calls the clustered EJB
        // the response will accumulate the InvocationPath
        for (int i = 0; i < 10; i++) {
            response = response.invoke();
        }

        System.out.println(response.getResponseInvocationPath());
        System.out.println("Invocation Path by Workflow");
        System.out.println(response.getResponseInvocationPathByWorkflow());
    }

    @Ignore @Test
    public void testEjbInvocationsInvokeClustered() throws Exception {
        EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;
        EJBInfo ejbJBossEjbClientXml = TestConfig.EJBS.CLUSTERED_EJB1_JBOSS_EJB_CLIENT_XML.info;
        EJBInfo ejbWildflyClientXml = TestConfig.EJBS.CLUSTERED_EJB1_WILDFLY_CLIENT_XML.info;
        TestConfig.SERVER server = TestConfig.SERVER.NODE1;
        TestConfig.CLUSTER cluster = TestConfig.CLUSTER.CLUSTER1;
        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;

        InvocationMethods test = new InvocationMethods(ejbInfo, ejbJBossEjbClientXml, ejbWildflyClientXml, TEST_EJB_INVOCATIONS_INVOKE_CLUSTERED_NEW, server,
                cluster, credential);
        test.option1_EjbClientScopedContext();
//        test.option2_RemoteNaming();
//        test.option3_WildflyNaming();
    }

    @Test
    public void testClustering() throws Exception {
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
        EJBRemoteConfig remoteEJBConfigNode1 = new EJBRemoteNamingConfig(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.EJBUSER);

        response.addWorkflow()
            .addAction(remoteEJBConfigNode1, TestConfig.EJBS.CLUSTERED_EJB1);

        // we expect to see node1 and node2 invoked
        for (int i = 0; i < 100; i++) {
            response = response.invoke();
        }

        Asserts.assertWorkflowClustered(response, 0);
        System.out.println(response.getResponseInvocationPath());
    }

    @Ignore @Test
    public void testXmlConfigs() throws Exception {

        // configure jboss instances, 2 node cluster, we probably want to run a 1 node test and a 2 node test as to test clustered and not clustered
        // deploy 3 identical EJBs, the only different is no xml, jboss-ejb-client.xml, wildfly-client.xml
        // run tests and report results, fail if expected throws exception

        EJBInfo ejbInfo = TestConfig.EJBS.CLUSTERED_EJB1.info;
        EJBInfo ejbJBossEjbClientXml = TestConfig.EJBS.CLUSTERED_EJB1_JBOSS_EJB_CLIENT_XML.info;
        EJBInfo ejbWildflyClientXml = TestConfig.EJBS.CLUSTERED_EJB1_WILDFLY_CLIENT_XML.info;
        TestConfig.SERVER server = TestConfig.SERVER.NODE1;
        TestConfig.CLUSTER cluster = TestConfig.CLUSTER.CLUSTER1;
        TestConfig.CREDENTIAL credential = TestConfig.CREDENTIAL.EJBUSER;

        // need to catch exceptions and log at the end which worked and which did not
        // need to invoke all with an expected to fail / expected to work, only fail the test if something is expected to work
//        InvocationMethods.Invocation invocation = new InvocationMethods.Invocation<ClusterSLSBRemote>() {
//            @Override
//            public TestReport invoke(Context context, ClusterSLSBRemote ejbProxy, TestReport report) {
//                EJBRequest response = new EJBRequest();
//                for(int i=0; i<500; i++)
//                    response = ejbProxy.invoke(response);
//                logSummary("testCluster", response);
//                return report;
//            }
//        };

//        InvocationMethods test = new InvocationMethods(ejbInfo, ejbJBossEjbClientXml, ejbWildflyClientXml, invocation, server, cluster, credential);
        TestReport report = null;
//        test.option1_EjbClientJBossEjbClientXml(); this needs to be invoked inside of JBoss not standalone - need to invoke the servlet and then invoke ejb -> ejb
//        handleReport(test.option2_EjbClientScopedContext()); // no errors
//        handleReport(test.option3_RemoteNaming()); // working
////        if(report.shouldFail())
////            Assert.fail(report.isExpectedSuccess() + " != " + report.isSuccess());
//        handleReport(test.option4_WildflyNaming()); // no errors
//        test.option5_WildflyClientXml(); this needs to be invoked inside of JBOss also I believe
//        test.option6_standaloneClientPropertiesFile();
//        test.option7_legacy_eap70_standaloneClientEJBClientAPI();

        // invoke jboss-ejb-client.xml via servlet (Node1) -> ejb (Node2)
        // junit -> servlet (Node1) [NONE] => EJB (Node2)
        // verify invocation path [0] = Servlet on node 1 and [1] is EJB on node 2

        System.out.println("Brad: running correct test");


//        EJBRequest ejbRequest = new EJBRequest("Standalone Client");
//        RemoteEJBConfig remoteEJBConfig = new RemoteEJBConfig(TestConfig.SERVER.NODE2, TestConfig.CREDENTIAL.EJBUSER);
//        ejbRequest.getActions().add(new EJBAction(remoteEJBConfig, TestConfig.EJBS.CLUSTERED_EJB1_JBOSS_EJB_CLIENT_XML.info));

        // This runs it using jboss-ejb-client.xml
        // CLIENT3 should be wildfly-client.xml
//        Results results = invokeViaServlet(TestConfig.SERVER.NODE1, TestConfig.SERVLET.CLIENT3, TestConfig.CREDENTIAL.SERVLET1, ejbRequest);

        // Build an EJBRequest
        // - include the EJB Action(s) to do
        // - set at the same time the expected user/node on the action level
        // - have call to fail if InvocationPath does not match

        // build the expected behavior
//        ExpectedResult er = ExpectedResult.builder();
//        er.addInvocationPath(TestConfig.SERVER.NODE1, TestConfig.CREDENTIAL.NONE);
//        er.addInvocationPath(TestConfig.SERVER.NODE2, TestConfig.CREDENTIAL.ANONYMOUS);
//
//        Asserts.assertExpectedResult(er, results.getResponse());

//
//        Asserts.assertNodeName(results.getResponse().getInvocationPath().get(1), TestConfig.SERVER.NODE1);
//        Asserts.assertNodeName(results.getResponse().getInvocationPath().get(2), TestConfig.SERVER.NODE2);

        // the configs that use xml are different from the ones that can be configured on the fly - they would need the config setup in standalone.xml before running tests
        // so we would want some tests with remote outbound connection to each n

//        System.out.println("Brad: invocationPath: " + results.getResponse().getResponseInvocationPath());

        // how to invoke the wildfly-config.xml using same flow as the standalone versions? Have a 3rd node to act as the client?

//            results.getResponse().getInvocationPath()
            // fail if the web / ejb user are not correct
            // path: 0 (servlet) , 1 (ejb)
//            results.failIfCallerIsNot(1, TestConfig.WEB_USERNAME);
//            results.failIfEJBNodeNameIsNot(1, TestConfig.MULTINODE_CLIENT);
//
//            results.failIfCallerIsNot(2, TestConfig.EJB_USERNAME);
//            results.failIfEJBNodeNameIsNot(2, TestConfig.MULTINODE_SERVER);
    }

    private static Results invokeViaServlet(TestConfig.SERVER server, TestConfig.SERVLET servlet, TestConfig.CREDENTIAL credential, EJBRequest ejbRequest) {
        InputStream is = null;
        Results results = null;
        try {
            is = URLUtil.openConnectionWithBasicAuth(server, servlet, credential, ejbRequest);
            String response = URLUtil.readInputStreamToString(is);
            results = Results.unmarshall(response);
        } catch(Throwable t) {
            if(results == null)
                results = new Results();
            results.setException(t);
        } finally {
            URLUtil.close(is);
        }

        System.out.println("Brad: returning results: " + results); System.out.flush();
        return results;
    }

    private static void handleReport(TestReport report) {
        report.marshallSafe();
        if(report.shouldFail())
            Assert.fail(report.isExpectedSuccess() + " != " + report.isSuccess());
    }


//	@Test
//	public void testCluster() throws NamingException {
//	    Context ctx = getInitialContext("127.0.0.1", 8080, "ejbuser", "redhat1!", null);
//	    ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
//	    EJBRequest response = new EJBRequest();
//	    for(int i=0; i<500; i++)
//	        response = clusterEJB.invoke(response);
//	    logSummary("testCluster", response);
//	    ctx.close();
//	}
//
//	@Test
//    public void testClusterAffinity() throws NamingException {
//        Context ctx = getInitialContext("127.0.0.1", 8080, "ejbuser", "redhat1!", "ejb");
//        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
//        EJBRequest response = new EJBRequest();
//        for(int i=0; i<500; i++)
//            response = clusterEJB.invoke(response);
//        logSummary("testClusterAffinity", response);
//        ctx.close();
//    }
//
//    @Test
//    public void testClusterAffinityAttempt2() throws NamingException {
//        Context ctx = getInitialContext("127.0.0.1", 8080, "ejbuser", "redhat1!", "ejb");
//        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
//        EJBClient.setStrongAffinity(clusterEJB, new ClusterAffinity("ejb"));
//        EJBRequest response = new EJBRequest();
//        for (int i = 0; i < 500; i++)
//            response = clusterEJB.invoke(response);
//        logSummary("testClusterAffinityAttempt2", response);
//        ctx.close();
//    }
//
//    @Test
//    public void testClusterAffinityAttempt3() throws NamingException {
//        // "remote+http://${cluster1-node1.address}:${cluster1-node1.application-port}"
//        String clusterName = String.format("remote+http://%s:%d", "127.0.0.1", 8080);
//        Context ctx = getInitialContext("127.0.0.1", 8080, "ejbuser", "redhat1!", clusterName);
//        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
//        EJBRequest response = new EJBRequest();
//        for (int i = 0; i < 500; i++)
//            response = clusterEJB.invoke(response);
//        logSummary("testClusterAffinityAttempt3", response);
//        ctx.close();
//    }
//
//    @Test
//    public void testClusterAffinityAttempt4() throws NamingException {
//        // "remote+http://${cluster1-node1.address}:${cluster1-node1.application-port}"
//        String clusterName = String.format("remote+http://%s:%d", "127.0.0.1", 8080);
//        Context ctx = getInitialContext("127.0.0.1", 8080, "ejbuser", "redhat1!", clusterName);
//        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
//        EJBClient.setStrongAffinity(clusterEJB, new ClusterAffinity(clusterName));
//        EJBRequest response = new EJBRequest();
//        for (int i = 0; i < 500; i++)
//            response = clusterEJB.invoke(response);
//        logSummary("testClusterAffinityAttempt4", response);
//        ctx.close();
//    }
//
//    @Test
//    public void testClusterAffinityAttempt5() throws NamingException {
//        // "remote+http://${cluster1-node1.address}:${cluster1-node1.application-port}"
//        String clusterName = "server";
//        Context ctx = getInitialContext("127.0.0.1", 8080, "ejbuser", "redhat1!", clusterName);
//        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
//        EJBClient.setStrongAffinity(clusterEJB, new ClusterAffinity(clusterName));
//        EJBRequest response = new EJBRequest();
//        for (int i = 0; i < 500; i++)
//            response = clusterEJB.invoke(response);
//        logSummary("testClusterAffinityAttempt5", response);
//        ctx.close();
//    }
//
//    @Test
//    public void testClusterAffinityAttempt6() throws NamingException {
//        // "remote+http://${cluster1-node1.address}:${cluster1-node1.application-port}"
//        String clusterName = "cluster";
//        Context ctx = getInitialContext("127.0.0.1", 8080, "ejbuser", "redhat1!", clusterName);
//        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
//        EJBClient.setStrongAffinity(clusterEJB, new ClusterAffinity(clusterName));
//        EJBRequest response = new EJBRequest();
//        for (int i = 0; i < 500; i++)
//            response = clusterEJB.invoke(response);
//        logSummary("testClusterAffinityAttempt6", response);
//        ctx.close();
//    }
//
//    @Test
//    public void testClusterAffinityAttempt7() throws NamingException {
//        // "remote+http://${cluster1-node1.address}:${cluster1-node1.application-port}"
//        String clusterName = "ee";
//        Context ctx = getInitialContext("127.0.0.1", 8080, "ejbuser", "redhat1!", clusterName);
//        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
//        EJBClient.setStrongAffinity(clusterEJB, new ClusterAffinity(clusterName));
//        EJBRequest response = new EJBRequest();
//        for (int i = 0; i < 500; i++)
//            response = clusterEJB.invoke(response);
//        logSummary("testClusterAffinityAttempt7", response);
//        ctx.close();
//    }
//
//    @Test
//    public void testClusterAffinityAttempt8() throws NamingException {
//        // "remote+http://${cluster1-node1.address}:${cluster1-node1.application-port}"
//        String clusterName = "ee-ejb";
//        Context ctx = getInitialContext("127.0.0.1", 8080, "ejbuser", "redhat1!", clusterName);
//        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
//        EJBClient.setStrongAffinity(clusterEJB, new ClusterAffinity(clusterName));
//        EJBRequest response = new EJBRequest();
//        for (int i = 0; i < 500; i++)
//            response = clusterEJB.invoke(response);
//        logSummary("testClusterAffinityAttempt8", response);
//        ctx.close();
//    }
//
//    @Test
//    public void testClusterAffinityAttempt9() throws NamingException {
//        EJBRemoteConfig scopedContextConfig = new EJBRemoteConfig();
//        scopedContextConfig.addConnection("localhost", "8080", "ejbuser", "redhat1!");
//        scopedContextConfig.addCluster("ejb", "ejbuser", "redhat1!", false, false);
//        scopedContextConfig.setScopedContext(true);
//        scopedContextConfig.getConfiguration().list(System.out);
//        Context ctx = new InitialContext(scopedContextConfig.getConfiguration());
//        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
//        EJBRequest response = new EJBRequest();
//        for (int i = 0; i < 500; i++)
//            response = clusterEJB.invoke(response);
//        logSummary("testClusterAffinityAttempt9", response);
//        ctx.close();
//    }
//
//    @Test
//    public void testClusterAffinityAttempt10() throws NamingException {
//        EJBRemoteConfig scopedContextConfig = new EJBRemoteConfig();
//        scopedContextConfig.addConnection("localhost", "8080", "ejbuser", "redhat1!");
//        scopedContextConfig.addCluster("ejb", "ejb", "redhat1!", false, false);
//        scopedContextConfig.setScopedContext(true);
//        scopedContextConfig.getConfiguration().list(System.out);
//        Context ctx = new InitialContext(scopedContextConfig.getConfiguration());
//        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
//        EJBRequest response = new EJBRequest();
//        for (int i = 0; i < 500; i++)
//            response = clusterEJB.invoke(response);
//        logSummary("testClusterAffinityAttempt10", response);
//        ctx.close();
//    }



    private static void logSummary(String msg, EJBRequest response) {
        EJBInvocationSummary summary = new EJBInvocationSummary(response);
        String format = "%s - Is Sticky: %s - Invocations Per Node: %s - Nodes Invoked: %s";
//        System.out.printf(format, msg, summary.isSticky(), summary.getInvocationsPerNode(), summary.getNodesInvoked());
    }


//	@Test
//	public void test() throws Exception {
//	    System.out.println("ArquillianTestCase"); System.out.flush();
//		// open 1 WildflyInitialContext
//		Context ctx = getInitialContext("localhost", 8080, "jbossuser", "redhat1!");
//
//		// JMSUtil take the InitialContext and will lookup the various JMS objects needed to send a message
//		JMSUtil jmsUtil = new JMSUtil(ctx);
//		jmsUtil.setJMSCredentials("jbossuser", "redhat1!");
//		jmsUtil.setDestinationLookup("queue/TestQueue");
//		jmsUtil.sendTextMessage("Hello");
//
//		// Invoke a remote EJB as well
//		EJBInfo monitorMDBSingleton = new EJBInfo("SLSBTest", "MDB", "MonitorMDBSingleton", "org.jboss.reproducer.ejb.mdb", MonitorMDBSingletonRemote.class);
//		MonitorMDBSingletonRemote mdbMonitorEJB = (MonitorMDBSingletonRemote) ctx.lookup(monitorMDBSingleton.getRemoteLookupPath());
//		MDBStats mdbStats = mdbMonitorEJB.getMDBStats();
//		System.out.println("MDBStats Response: " + mdbStats);
//
//		// Inovke SLSB also
//		SLSBRemote slsbEjb = (SLSBRemote) ctx.lookup(slsbInfo.getRemoteLookupPath());
//		System.out.println(slsbEjb.hello("ArquillianTest"));
//		System.out.flush();
//	}
}