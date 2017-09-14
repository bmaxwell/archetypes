/**c
 *
 */
package org.jboss.reproducer.test;

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
import org.jboss.ejb.client.ClusterAffinity;
import org.jboss.ejb.client.EJBClient;
import org.jboss.reproducer.ejb.api.slsb.ClusterSLSBRemote;
import org.jboss.reproducer.ejb.api.slsb.EJBInvocationSummary;
import org.jboss.reproducer.ejb.api.slsb.EJBRequest;
import org.jboss.reproducer.ejb.api.slsb.SLSBRemote;
import org.jboss.shrinkwrap.api.Archive;
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

//	@ContainerResource("node2")
//    protected ManagementClient node2;

    private static String applicationName = "SLSBTest";
    private static EJBInfo slsbInfo = new EJBInfo(applicationName, "SLSBEJB", "SLSBEJB", "org.jboss.reproducer.ejb.slsb", SLSBRemote.class);
    private static EJBInfo mdbInfo = new EJBInfo(applicationName, "MDB", "MDB", "org.jboss.reproducer.ejb.mdb", null);

    private static EJBInfo clusterEJBInfo = new EJBInfo(applicationName, "SLSBEJB", "ClusterSLSBEJB", "org.jboss.reproducer.ejb.slsb", ClusterSLSBRemote.class);

	// Setup
	// run standalone-full.xml
	// configure arquillian.xml
	// <jms-queue name="TestQueue" entries="java:/jms/queue/TestQueue,java:jboss/exported/queue/TestQueue"/>

	@Deployment(name="EAR1", testable = false)
	@TargetsContainer("node1")
    public static Archive<?> createEarDeployment1() {
        Archive<?>[] earLib = new Archive<?>[] { Deployments.SLSB_API, Deployments.MDB_API };
        return Deployments.createEarDeployment(applicationName, earLib, Deployments.createEjbSubDeployment(slsbInfo), Deployments.createEjbSubDeployment(mdbInfo));
    }

	@Deployment(name="EAR2", testable = false)
    @TargetsContainer("node2")
    public static Archive<?> createEarDeployment2() {
        Archive<?>[] earLib = new Archive<?>[] { Deployments.SLSB_API, Deployments.MDB_API };
        return Deployments.createEarDeployment(applicationName, earLib, Deployments.createEjbSubDeployment(slsbInfo), Deployments.createEjbSubDeployment(mdbInfo));
    }

//	@Deployment
//	public static createEJBDeployment() {
//		EJBInfo ejbInfo = new EJBInfo("", "ejb-client", "HelloBean", "HelloBean", null);
//		Deployments.createEjbDeployment(ejbInfo)
//	}

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

	public static final String TEST_QUEUE = "/queue/TestQueue";

	@Test
	public void testCluster() throws NamingException {
	    Context ctx = getInitialContext("127.0.0.1", 8080, "ejbuser", "redhat1!", null);
	    ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
	    EJBRequest response = new EJBRequest();
	    for(int i=0; i<500; i++)
	        response = clusterEJB.invoke(response);

	    EJBInvocationSummary summary = new EJBInvocationSummary(response);
	    System.out.println("Is Sticky: " + summary.isSticky());
	    System.out.println("Invocations Per Node: " + summary.getInvocationsPerNode());
	    ctx.close();
	}

	@Test
    public void testClusterAffinity() throws NamingException {
        Context ctx = getInitialContext("127.0.0.1", 8080, "ejbuser", "redhat1!", "ejb");
        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
        EJBRequest response = new EJBRequest();
        for(int i=0; i<500; i++)
            response = clusterEJB.invoke(response);

        EJBInvocationSummary summary = new EJBInvocationSummary(response);
        System.out.println("Is Sticky: " + summary.isSticky());
        System.out.println("Invocations Per Node: " + summary.getInvocationsPerNode());
        ctx.close();
    }

    @Test
    public void testClusterAffinityAttempt2() throws NamingException {
        Context ctx = getInitialContext("127.0.0.1", 8080, "ejbuser", "redhat1!", "ejb");
        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
        EJBClient.setStrongAffinity(clusterEJB, new ClusterAffinity("ejb"));
        EJBRequest response = new EJBRequest();
        for (int i = 0; i < 500; i++)
            response = clusterEJB.invoke(response);

        EJBInvocationSummary summary = new EJBInvocationSummary(response);
        System.out.println("Is Sticky: " + summary.isSticky());
        System.out.println("Invocations Per Node: " + summary.getInvocationsPerNode());
        ctx.close();
    }

    @Test
    public void testClusterAffinityAttempt3() throws NamingException {
        // "remote+http://${cluster1-node1.address}:${cluster1-node1.application-port}"
        String clusterName = String.format("remote+http://%s:%d", "127.0.0.1", 8080);
        Context ctx = getInitialContext("127.0.0.1", 8080, "ejbuser", "redhat1!", clusterName);
        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
        EJBRequest response = new EJBRequest();
        for (int i = 0; i < 500; i++)
            response = clusterEJB.invoke(response);
        EJBInvocationSummary summary = new EJBInvocationSummary(response);
        System.out.println("Is Sticky: " + summary.isSticky());
        System.out.println("Invocations Per Node: " + summary.getInvocationsPerNode());
        ctx.close();
    }

    @Test
    public void testClusterAffinityAttempt4() throws NamingException {
        // "remote+http://${cluster1-node1.address}:${cluster1-node1.application-port}"
        String clusterName = String.format("remote+http://%s:%d", "127.0.0.1", 8080);
        Context ctx = getInitialContext("127.0.0.1", 8080, "ejbuser", "redhat1!", clusterName);
        ClusterSLSBRemote clusterEJB = (ClusterSLSBRemote) ctx.lookup(clusterEJBInfo.getRemoteLookupPath());
        EJBClient.setStrongAffinity(clusterEJB, new ClusterAffinity(clusterName));
        EJBRequest response = new EJBRequest();
        for (int i = 0; i < 500; i++)
            response = clusterEJB.invoke(response);
        EJBInvocationSummary summary = new EJBInvocationSummary(response);
        System.out.println("Is Sticky: " + summary.isSticky());
        System.out.println("Invocations Per Node: " + summary.getInvocationsPerNode());
        ctx.close();
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