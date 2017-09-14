/**
 * 
 */
package org.jboss.reproducer.test;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.FAILED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.scriptsupport.CLI;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.xnio.IoUtils;

/**
 * @author bmaxwell
 *
 */
public class ConfigureServer implements ServerSetupTask {

	/**
	 * 
	 */
	public ConfigureServer() {		
	}


	String[] configure = new String[] {
						
			// remove $local from ApplicationRealm
			"/core-service=management/security-realm=ApplicationRealm/authentication=local:remove()",
			// add ssl to ApplicationRealm
			"/core-service=management/security-realm=ApplicationRealm/server-identity=ssl:add(keystore-path=lab.keystore, keystore-relative-to=jboss.server.config.dir, keystore-password=labpassword)",
						
			// set remoting to use https connector
//			"/subsystem=remoting/http-connector=https-remoting-connector:add(connector-ref=https, security-realm=ApplicationRealm)",
			"/subsystem=ejb3/service=remote:write-attribute(name=connector-ref, value=https-remoting-connector)",
			// /subsystem=ejb3/service=remote:add(connector-ref=https-remoting-connector, thread-pool-name=default)

//			// a reload is required in order to add the https-listener
//			"reload",
			
			// add https-listener to undertow
//			"/subsystem=undertow/server=default-server/https-listener=https:add(security-realm=ApplicationRealm, socket-binding=https)",
			
			
//			// reload to pick up the changes			
//			"reload"
			
			// remoting <http-connector name="https-remoting-connector" connector-ref="https" security-realm="ApplicationRealm"/>
			// ejb3: <remote connector-ref="https-remoting-connector" thread-pool-name="default"/>
			};

	String[] unconfigure = new String[] {
			
			"/subsystem=ejb3/service=remote:write-attribute(name=connector-ref, value=http-remoting-connector)",
			
			// remove undertow https-listener
			"/subsystem=undertow/server=default-server/https-listener=https:remove()",
			// remove ApplicationRealm ssl
			"/core-service=management/security-realm=ApplicationRealm/server-identity=ssl:remove()",
			// add $local back to ApplicationRealm
			"/core-service=management/security-realm=ApplicationRealm/authentication=local:add(allowed-users=*, skip-group-loading=true)" 
			};
		
	
	private void sendLine(ManagementClient managementClient, String line) throws CommandLineException {
		CLI cli = CLI.newInstance();	
		CommandContext cctx = CommandContextFactory.getInstance().newCommandContext();
		
		cctx.bindClient(managementClient.getControllerClient());
		System.out.printf("ServerRunning: %s IsClosed: %s Line: %s", managementClient.isServerInRunningState(), managementClient.isClosed(), line);	
		System.out.flush();
		cctx.handle(line);
	}
	
	private void applyConfiguration(String[] configuration, ManagementClient managementClient) throws IOException {
		try {			
			for(String cliCommand : configuration) {
				sendLine(managementClient, cliCommand);				
//				ModelNode request = cctx.buildRequest(cliCommand);
//				managementClient.getControllerClient().execute(request);
			}
		} catch(CommandLineException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public void setup(ManagementClient managementClient, String containerId) throws Exception {		
		System.out.println("Server State: " + managementClient.isServerInRunningState());
		applyConfiguration(configure, managementClient);		
		reloadIfRequired(managementClient);
		sendLine(managementClient, "/subsystem=undertow/server=default-server/https-listener=https:add(security-realm=ApplicationRealm, socket-binding=https)");
	}

	@Override
	public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
		applyConfiguration(unconfigure, managementClient);
	}
	
    public static void reloadIfRequired(final ManagementClient controllerClient) throws Exception {
        String runningState = getContainerRunningState(controllerClient);
        if ("reload-required".equalsIgnoreCase(runningState)) {
            executeReloadAndWaitForCompletion(controllerClient);
        } else {
            Assert.assertEquals("Server state 'running' is expected", "running", runningState);
        }   
    }   
    public static String getContainerRunningState(ManagementClient managementClient) throws IOException {
        return getContainerRunningState(managementClient.getControllerClient());
    }   

    public static String getContainerRunningState(ModelControllerClient modelControllerClient) throws IOException {
        ModelNode operation = new ModelNode();
        operation.get(OP_ADDR).setEmptyList();
        operation.get(OP).set(READ_ATTRIBUTE_OPERATION);
        operation.get(NAME).set("server-state");
        ModelNode rsp = modelControllerClient.execute(operation);
        return SUCCESS.equals(rsp.get(OUTCOME).asString()) ? rsp.get(RESULT).asString() : FAILED;
    }   
    
    public static final int TIMEOUT = 100000;

    public static void executeReloadAndWaitForCompletion(ManagementClient client) {
        executeReloadAndWaitForCompletion(client, TIMEOUT);
    }

    public static void executeReloadAndWaitForCompletion(ManagementClient client, boolean adminOnly) {
        executeReloadAndWaitForCompletion(client, TIMEOUT, adminOnly, null, -1);
    }

    public static void executeReloadAndWaitForCompletion(ManagementClient client, int timeout) {
        executeReloadAndWaitForCompletion(client, timeout, false, null, -1);
    }

    public static void executeReloadAndWaitForCompletion(ManagementClient client, String serverHost, int serverPort) {
        executeReloadAndWaitForCompletion(client, TIMEOUT, false, serverHost, serverPort);
    }

    /**
    *
    * @param client
    * @param timeout
    * @param adminOnly if {@code true}, the server will be reloaded in admin-only mode
    * @param serverAddress if {@code null}, use {@code TestSuiteEnvironment.getServerAddress()} to create the ModelControllerClient
    * @param serverPort if {@code -1}, use {@code TestSuiteEnvironment.getServerPort()} to create the ModelControllerClient
    */
   public static void executeReloadAndWaitForCompletion(ManagementClient client, int timeout, boolean adminOnly, String serverAddress, int serverPort) {
       executeReload(client, adminOnly);
       
       waitForLiveServerToReload(timeout,
               serverAddress != null ? serverAddress : client.getMgmtAddress(),
               serverPort != -1 ? serverPort : client.getMgmtPort());
   }

   private static void executeReload(ManagementClient client, boolean adminOnly) {
       ModelNode operation = new ModelNode();
       operation.get(OP_ADDR).setEmptyList();
       operation.get(OP).set("reload");
       operation.get("admin-only").set(adminOnly);
       try {
           ModelNode result = client.getControllerClient().execute(operation);
           Assert.assertEquals("success", result.get(ClientConstants.OUTCOME).asString());
       } catch(IOException e) {
           final Throwable cause = e.getCause();
           if (!(cause instanceof ExecutionException) && !(cause instanceof CancellationException)) {
               throw new RuntimeException(e);
           } // else ignore, this might happen if the channel gets closed before we got the response
       }
   }
   
   private static void waitForLiveServerToReload(int timeout, String serverAddress, int serverPort) {
       long start = System.currentTimeMillis();
       ModelNode operation = new ModelNode();
       operation.get(OP_ADDR).setEmptyList();
       operation.get(OP).set(READ_ATTRIBUTE_OPERATION);
       operation.get(NAME).set("server-state");
       while (System.currentTimeMillis() - start < timeout) {
           try {
               ModelControllerClient liveClient = ModelControllerClient.Factory.create(
                       serverAddress, serverPort);
               try {
                   ModelNode result = liveClient.execute(operation);
                   if ("running" .equals(result.get(RESULT).asString())) {
                       return;
                   }
               } catch (IOException e) {
               } finally {
                   IoUtils.safeClose(liveClient);
               }
               try {
                   Thread.sleep(100);
               } catch (InterruptedException e) {
               }
           } catch (UnknownHostException e) {
               throw new RuntimeException(e);
           }
       }
       Assert.fail("Live Server did not reload in the imparted time.");
   }

}