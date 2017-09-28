/**
 *
 */
package org.jboss.reproducer.ejb.api.sfsb;

import javax.ejb.Local;

import org.jboss.reproducer.ejb.api.EJBRemote;

/**
 * @author bmaxwell
 *
 */
@Local
public interface SFSBLocal extends EJBRemote {

	void remove();

}