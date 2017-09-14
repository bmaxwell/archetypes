/**
 * 
 */
package org.jboss.reproducer.ejb.api.sfsb;

import javax.ejb.Local;

/**
 * @author bmaxwell
 *
 */
@Local
public interface SFSBLocal {

	SFSBResponse invoke(SFSBRequest request);
	void remove();
	
}