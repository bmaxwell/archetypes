/**
 *
 */
package org.jboss.reproducer.ejb.api.slsb;

import javax.ejb.Remote;

import org.jboss.reproducer.ejb.api.EJBRemote;

/**
 * @author bmaxwell
 *
 */
@Remote
public interface SLSBRemote extends EJBRemote {

	String hello(String name);

	String sleep(long sleepMilliSeconds);
}