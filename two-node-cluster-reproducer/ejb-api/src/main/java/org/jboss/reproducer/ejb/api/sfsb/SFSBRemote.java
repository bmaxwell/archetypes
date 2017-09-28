/**
 *
 */
package org.jboss.reproducer.ejb.api.sfsb;

import javax.ejb.Remote;

import org.jboss.reproducer.ejb.api.EJBRemote;

/**
 * @author bmaxwell
 *
 */
@Remote
public interface SFSBRemote extends EJBRemote {

	void remove();
}