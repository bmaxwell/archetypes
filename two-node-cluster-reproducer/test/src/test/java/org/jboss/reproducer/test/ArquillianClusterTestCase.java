/**c
 *
 */
package org.jboss.reproducer.test;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ContainerResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.junit.runner.RunWith;



/**
 * @author bmaxwell
 *
 */
//TODO Add byteman example
@RunWith(Arquillian.class)
//@ServerSetup(ConfigureServer.class)
@RunAsClient
public class ArquillianClusterTestCase {

    /**
     * The controller allows you to start/stop the servers
     */
    @ArquillianResource
    private ContainerController controller;

    @ArquillianResource
    private Deployer deployer;

	@ContainerResource("node1")
	protected ManagementClient node1;

	@ContainerResource("node2")
    protected ManagementClient node2;

	/**
	 *
	 */
	public ArquillianClusterTestCase() {
	}

	public void testClustering() {
//	    controller.start(arg0);
	}

//    @Test
//    @InSequence(1)
//    public void startServerInstances() throws Exception {
//        log.info("Starting " + SERVER_INSTANCE_1 + "...");
//        this.controller.start(SERVER_INSTANCE_1);
//        log.info("Starting " + SERVER_INSTANCE_2 + "...");
//        this.controller.start(SERVER_INSTANCE_2);
//
//        log.info(SERVER_INSTANCE_1 + " running: " + this.controller.isStarted(SERVER_INSTANCE_1));
//        log.info(SERVER_INSTANCE_2 + " running: " + this.controller.isStarted(SERVER_INSTANCE_2));
//    }
//
//    /** Deploy singletons to their server **/
//    @Test
//    @InSequence(2)
//    public void deploySingletons() throws Exception {
//        // this should deploy the deployment to the servers
//        this.deployer.deploy(CLUSTERED_HELLO_EJB_DEPLOYMENT);
//        this.deployer.deploy(CLUSTERED_HELLO_EJB_DEPLOYMENT2);
//    }

}