/**
 * 
 */
package org.jboss.reproducer.ejb.slsb;

import java.util.Timer;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.TimerService;

/**
 * @author bmaxwell
 *
 */
@Stateless
public class EJBTimer {

	private static Logger log = Logger.getLogger(EJBTimer.class.getName());
	
    @Resource
    private TimerService timerService;
	
	/**
	 * 
	 */
	public EJBTimer() {
	}

//	@Timeout
//	public void timerMethod(Timer timer) {
//		
//	}

//	@Schedule(second = "*/5", minute = "*/3", hour = "*", persistent = false)
//	public void scheduleTimeout(Timer timer) {
//		log.info("schedule timer invoked");
//	}
	
}