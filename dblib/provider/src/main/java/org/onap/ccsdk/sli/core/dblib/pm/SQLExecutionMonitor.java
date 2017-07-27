/*-
 * ============LICENSE_START=======================================================
 * onap
 * ================================================================================
 * Copyright (C) 2016 - 2017 ONAP
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.core.dblib.pm;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import org.onap.ccsdk.sli.core.dblib.DBResourceObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLExecutionMonitor extends Observable
{
	private static Logger LOGGER = LoggerFactory.getLogger(SQLExecutionMonitor.class);
	
	final static long MILISECOND = 1000000L;
	final static long SECOND = 1000L*MILISECOND;
	
	private final Timer timer;
	// collection
	private final SortedSet<TestObject> innerSet;
	private SQLExecutionMonitorObserver parent = null; 
	private final AtomicLong completionCounter;
	private boolean activeState = false;
	private final long interval;
	private final long initialDelay;
	private final long EXPECTED_TIME_TO_COMPLETE;
	private final long UNPROCESSED_FAILOVER_THRESHOLD;

	private final class MonitoringTask extends TimerTask 
	{
		
		public void run() 
		{
			try {
				TestObject testObj = new TestObject();
				testObj.setStartTime(testObj.getStartTime() - EXPECTED_TIME_TO_COMPLETE);

				// take a snapshot of the current task list
				TestObject[] array = innerSet.toArray(new TestObject[0]);
				SortedSet<TestObject> copyCurrent = new TreeSet<TestObject>(Arrays.asList(array));
				// get the list of the tasks that are older than the specified
				// interval.
				SortedSet<TestObject> unprocessed = copyCurrent.headSet(testObj);

				long succesfulCount = completionCounter.get();
				int unprocessedCount = unprocessed.size();
				
				if (!unprocessed.isEmpty() && unprocessedCount > UNPROCESSED_FAILOVER_THRESHOLD && succesfulCount == 0)
				{
					// switch the Connection Pool to passive
					setChanged();
					notifyObservers("Open JDBC requests=" + unprocessedCount+" in "+SQLExecutionMonitor.this.parent.getDbConnectionName());
				}
			} catch (Exception exc) {
				LOGGER.error("", exc);
			} finally {
				completionCounter.set(0L);
			}
		}
	}

	public static class TestObject implements Comparable<TestObject>, Serializable 
	{

		private static final long serialVersionUID = 1L;
		private long starttime;
		private long randId;

		public TestObject()
		{
			starttime = System.nanoTime();
		}

		public long getStartTime()
		{
			return starttime;
		}

		public void setStartTime(long newTime) 
		{
			starttime = newTime;
		}

		public int compareTo(TestObject o)
		{
			if( this == o)
				return 0;
			if(this.starttime > o.getStartTime())
				return 1;
			if(this.starttime < o.getStartTime())
				return -1;

			if(this.hashCode() > o.hashCode())
				return 1;
			if(this.hashCode() < o.hashCode())
				return -1;

			return 0;
		}

		public String toString()
		{
			return Long.toString(starttime)+"#"+ this.hashCode();
		}
	
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;

			return (obj instanceof TestObject 
		            && starttime == ((TestObject) obj).getStartTime()
		            && hashCode() == ((TestObject) obj).hashCode());		
		}
	}

	public SQLExecutionMonitor(SQLExecutionMonitorObserver parent)
	{
		this.parent = parent;
		completionCounter = new AtomicLong(0L);
		interval = parent.getInterval();
		initialDelay = parent.getInitialDelay();
		this.UNPROCESSED_FAILOVER_THRESHOLD = parent.getUnprocessedFailoverThreshold();
		this.EXPECTED_TIME_TO_COMPLETE = parent.getExpectedCompletionTime()*MILISECOND;
		
		innerSet = Collections.synchronizedSortedSet(new TreeSet<TestObject>());
		timer = new Timer();
	}
	
	public void cleanup()
	{
		timer.cancel();
	}
	
	// registerRequest
	public TestObject registerRequest()
	{
		if(activeState)
		{
			TestObject test = new TestObject();
			if(innerSet.add(test))
				return test;
		}
		return null;
	}

	// deregisterSuccessfulReguest
	public boolean deregisterReguest(TestObject test)
	{
		if(test == null)
			return false;
		// remove from the collection
		if(innerSet.remove(test) && activeState)
		{
			completionCounter.incrementAndGet();
			return true;
		}
		return false; 
	}

	public void terminate() {
		timer.cancel();
	}

	/**
	 * @return the parent
	 */
	public final Object getParent() {
		return parent;
	}
	
	public void addObserver(Observer observer)
	{
		if(observer instanceof DBResourceObserver)
		{
			DBResourceObserver dbObserver = (DBResourceObserver)observer;
			if(dbObserver.isMonitorDbResponse())
			{
				if(countObservers() == 0)
				{
					TimerTask remindTask = new MonitoringTask();
					timer.schedule(remindTask, initialDelay, interval);
					activeState = true;
				}
			}
		}
		super.addObserver(observer);
	}
	
	public void deleteObserver(Observer observer)
	{
		super.deleteObserver(observer);
		if(observer instanceof DBResourceObserver)
		{
			DBResourceObserver dbObserver = (DBResourceObserver)observer;
			if(dbObserver.isMonitorDbResponse())
			{
				if(countObservers() == 0)
				{
					timer.cancel();
					activeState = false;
				}
			}
		}
	}
	
	public final int getPorcessedConnectionsCount() {
		return innerSet.size();
	}
}
