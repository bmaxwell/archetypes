/**
 *
 */
package org.jboss.reproducer.ejb.slsb;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import org.jboss.logging.Logger;
import org.jboss.reproducer.ejb.api.slsb.ClusterSLSBRemote;
import org.jboss.reproducer.ejb.api.slsb.EJBRequest;
import org.jboss.reproducer.ejb.api.slsb.InvocationPath;

/**
 * @author bmaxwell
 *
 */
@Stateless
public class ClusterSLSBEJB implements ClusterSLSBRemote {

    private Logger log = Logger.getLogger(this.getClass().getName());
    private String nodeName = System.getProperty("jboss.node.name");
    // TODO how to get the cluster name?

    @Resource
    private SessionContext context;

	/**
	 *
	 */
	public ClusterSLSBEJB() {
	}

	@Override
	public EJBRequest invoke(EJBRequest request) {
	    EJBRequest response = request;
	    InvocationPath path = new InvocationPath();
	    path.setNodeName(nodeName);
	    path.setPrincipalName(context.getCallerPrincipal().getName());
	    response.getInvocationPath().add(path);
	    return response;
	}
}