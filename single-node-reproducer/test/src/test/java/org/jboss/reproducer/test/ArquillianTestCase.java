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
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ContainerResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.reproducer.ejb.api.mdb.JMSUtil;
import org.jboss.reproducer.ejb.api.mdb.MDBStats;
import org.jboss.reproducer.ejb.api.mdb.MonitorMDBSingletonRemote;
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

	@ContainerResource("server")
	protected ManagementClient server;

    private static String applicationName = "SLSBTest";
    private static EJBInfo slsbInfo = new EJBInfo(applicationName, "SLSBEJB", "SLSBEJB", "org.jboss.reproducer.ejb.slsb", SLSBRemote.class);
    private static EJBInfo mdbInfo = new EJBInfo(applicationName, "MDB", "MDB", "org.jboss.reproducer.ejb.mdb", null);


	// Setup
	// run standalone-full.xml
	// configure arquillian.xml
	// <jms-queue name="TestQueue" entries="java:/jms/queue/TestQueue,java:jboss/exported/queue/TestQueue"/>

	@Deployment
    public static Archive<?> createEarDeployment() {
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

	private static Context getInitialContext(String host, Integer port, String username, String password) throws NamingException {
	    Properties props = new Properties();
	    props.put(Context.INITIAL_CONTEXT_FACTORY,  "org.wildfly.naming.client.WildFlyInitialContextFactory");
	    props.put(Context.PROVIDER_URL, String.format("%s://%s:%d", "remote+http", host, port));
	    props.put(Context.SECURITY_PRINCIPAL, username);
	    props.put(Context.SECURITY_CREDENTIALS, password);
	    return new InitialContext(props);
	 }

	public static final String TEST_QUEUE = "/queue/TestQueue";

	@Test
	public void test() throws Exception {
	    System.out.println("ArquillianTestCase"); System.out.flush();
		// open 1 WildflyInitialContext
		Context ctx = getInitialContext("localhost", 8080, "jbossuser", "redhat1!");

		// JMSUtil take the InitialContext and will lookup the various JMS objects needed to send a message
		JMSUtil jmsUtil = new JMSUtil(ctx);
		jmsUtil.setJMSCredentials("jbossuser", "redhat1!");
		jmsUtil.setDestinationLookup("queue/TestQueue");
		jmsUtil.sendTextMessage("Hello");

		// Invoke a remote EJB as well
		EJBInfo monitorMDBSingleton = new EJBInfo("SLSBTest", "MDB", "MonitorMDBSingleton", "org.jboss.reproducer.ejb.mdb", MonitorMDBSingletonRemote.class);
		MonitorMDBSingletonRemote mdbMonitorEJB = (MonitorMDBSingletonRemote) ctx.lookup(monitorMDBSingleton.getRemoteLookupPath());
		MDBStats mdbStats = mdbMonitorEJB.getMDBStats();
		System.out.println("MDBStats Response: " + mdbStats);

		// Inovke SLSB also
		SLSBRemote slsbEjb = (SLSBRemote) ctx.lookup(slsbInfo.getRemoteLookupPath());
		System.out.println(slsbEjb.hello("ArquillianTest"));
		System.out.flush();


		// Try using UserTransaction

//		CLI cli = CLI.newInstance();
//		String cliCommand = "";
//		CommandContext cctx = CommandContextFactory.getInstance().newCommandContext();
//		ModelNode request = cctx.buildRequest(cliCommand);
//		server.getControllerClient().execute(request);
//
//		CommandContextFactory.getInstance().newCommandContex
//		org.jboss.as.controller.client.ModelControllerClient//
//		CommandContextFactory.getInstance()

	}
}