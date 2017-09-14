/**
 *
 */
package org.jboss.reproducer.ejb.api.mdb;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author bmaxwell
 *
 */
public class MDBStats implements Serializable {

	private String name;
	private AtomicInteger invocations = new AtomicInteger(0);
	private AtomicLong totalTime = new AtomicLong(0);
	private Integer trackInvocationsLongerThanSeconds = -1;
	private int logAfterNumberOfMessages = 50;

	/**
	 *
	 */
	public MDBStats(String name) {
		this.name = name;
	}

	public void addInvocation(long start, long finish) {
		int currentInvocations = invocations.incrementAndGet();
		long currentTotalTime = totalTime.addAndGet((finish - start));
		if(currentInvocations % logAfterNumberOfMessages == 0) {
			System.out.println(String.format("[MDBStats] name: %s invocations: %d totalTime: %d average: %g", name, invocations.get(), currentTotalTime, (((double)totalTime.get()) / ((double)invocations.get()))));
		}
	}

	public long getTotalTime() {
		return totalTime.get();
	}

	public double getAverage() {
		return ((double)totalTime.get()) / ((double)invocations.get());
	}

	public String getName() {
		return name;
	}

	public Integer getInvocations() {
		return invocations.get();
	}

	@Override
    public String toString() {
	    return String.format("MDBStats name: %s invocations: %d totalTime: %d", name, invocations.get(), totalTime.get());
	}
}