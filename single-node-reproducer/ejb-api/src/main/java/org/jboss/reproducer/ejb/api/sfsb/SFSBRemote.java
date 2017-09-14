/**
 * 
 */
package org.jboss.reproducer.ejb.api.sfsb;

import javax.ejb.Remote;

/**
 * @author bmaxwell
 *
 */
@Remote
public interface SFSBRemote {

	SFSBResponse invoke(SFSBRequest request);
	void remove();

}