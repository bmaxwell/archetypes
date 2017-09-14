/**
 * 
 */
package org.jboss.reproducer.ejb.singleton;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;

import javax.ejb.Startup;

/**
 * @author bmaxwell
 *
 */
@Startup
@Singleton
public class SingletonEJB {

	private static Logger log = Logger.getLogger(Singleton.class.getName());
	
	/**
	 * 
	 */
	public SingletonEJB() {
	}

	@PostConstruct
	public void postConstruct() {
		log.info("postConstruct");
	}	
}