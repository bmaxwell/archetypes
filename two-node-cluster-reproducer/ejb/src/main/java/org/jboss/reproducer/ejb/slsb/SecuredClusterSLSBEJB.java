/**
 *
 */
package org.jboss.reproducer.ejb.slsb;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.reproducer.ejb.api.AbstractEJB;
import org.jboss.reproducer.ejb.api.slsb.ClusterSLSBRemote;

/**
 * @author bmaxwell
 *
 */
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@SecurityDomain("other")
@RolesAllowed({"guest"})
public class SecuredClusterSLSBEJB extends AbstractEJB implements ClusterSLSBRemote {

//    @Resource(lookup = "java:jboss/clustering/group/default")
//    private Group channelGroup;

    @Resource
    private SessionContext context;

	/**
	 *
	 */
	public SecuredClusterSLSBEJB() {
	}

    @Override
    public String hello(String name) {
        log.info("Hello " + name);
        return "Hello " + name;
    }
}