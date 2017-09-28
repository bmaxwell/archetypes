/**
 *
 */
package org.jboss.reproducer.ejb.slsb;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.jboss.reproducer.ejb.api.AbstractEJB;
import org.jboss.reproducer.ejb.api.slsb.ClusterSLSBRemote;

/**
 * @author bmaxwell
 *
 */
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ClusterSLSBEJB extends AbstractEJB implements ClusterSLSBRemote {

//    @Resource(lookup = "java:jboss/clustering/group/default")
//    private Group channelGroup;

    @Resource
    private SessionContext context;

	/**
	 *
	 */
	public ClusterSLSBEJB() {
	}

    @Override
    public String hello(String name) {
        log.info("Hello " + name);
        return "Hello " + name;
    }
}