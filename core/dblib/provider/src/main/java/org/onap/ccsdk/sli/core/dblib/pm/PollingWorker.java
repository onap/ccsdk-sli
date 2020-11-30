/*-
 * ============LICENSE_START=======================================================
 * onap
 * ================================================================================
 * Copyright (C) 2016 - 2017 ONAP
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Revision: 1.15 $
 * Change Log
 * Author         Date     Comments
 * ============== ======== ====================================================
 * Rich Tabedzki
 */
public class PollingWorker implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(PollingWorker.class);

	private static PollingWorker self = null;

	private LinkedBlockingQueue tasks = new LinkedBlockingQueue(100);
	private long interval = 1000L;
	private Thread worker = null;
	private AtomicLong[] counters = null;
	private int[] bucketUnit = null;
	private static boolean enabled = false;
	private Timer timer = null;

	private PollingWorker(Properties ctxprops) {
		if (ctxprops == null || ctxprops.getProperty("org.onap.ccsdk.dblib.pm") == null) {
			enabled = false;
		} else {
			if ("true".equalsIgnoreCase((String) ctxprops.getProperty("org.onap.ccsdk.dblib.pm"))) {
				enabled = true;
			} else {
				enabled = false;
			}
		}

		interval = Long.parseLong((ctxprops == null || ctxprops.getProperty("org.onap.ccsdk.dblib.pm.interval") == null)
				? "60" : (String) ctxprops.getProperty("org.onap.ccsdk.dblib.pm.interval"));
		// '0' bucket is to count exceptions
		String[] sampling = ((ctxprops == null || ctxprops.getProperty("org.onap.ccsdk.dblib.pm.sampling") == null)
				? "0,2,5,10,20,50,100" : (String) ctxprops.getProperty("org.onap.ccsdk.dblib.pm.sampling")).split(",");

		if (enabled) {
			bucketUnit = new int[sampling.length];
			for (int i = 0, max = bucketUnit.length; i < max; i++) {
				bucketUnit[i] = Integer.parseInt(sampling[i].trim());
			}
			counters = new AtomicLong[bucketUnit.length + 1];
			for (int i = 0, max = counters.length; i < max; i++) {
				counters[i] = new AtomicLong();
			}
			worker = new Thread(this);
			worker.setDaemon(true);
			worker.start();
			timer = new Timer(true);
			timer.schedule(new MyTimerTask(), interval * 1000L, interval * 1000L);
		}
	}

	public static void post(long starttime) {
		PollingWorker temp = self;
		if (temp != null && enabled) {
			temp.register(new TestSample(starttime));
		}
	}

	public static void createInistance(Properties props) {
		self = new PollingWorker(props);
	}

	private void register(TestSample object) {
		try {
			tasks.add(object);
		} catch (Throwable exc) {
			// if cannot add an object to the queue, do nothing
		}
	}

	private void deRegister(TestSample object) {
		tasks.remove(object);
	}

	@Override
	public void run() {
		for (;;) {
			Set data = new TreeSet();
			tasks.drainTo(data);
			for (Iterator it = data.iterator(); it.hasNext();) {
				Object next = it.next();
				if (next instanceof TestSample) {
					consume((TestSample) next);
				} else {
					System.out.println(next.getClass().getName());
					LOGGER.error(next.getClass().getName());
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();

			}
		}

	}

	public void clearReqister() {
		AtomicLong[] tmp = new AtomicLong[counters.length];
		for (int i = 0, max = tmp.length; i < max; i++) {
			tmp[i] = new AtomicLong();
		}
		AtomicLong[] tmp2 = counters;
		synchronized (tmp2) {
			counters = tmp;
		}
		StringBuffer sb = new StringBuffer("CPM: ");
		for (int i = 0, max = tmp2.length; i < max; i++) {
			if (i == 0 && bucketUnit[0] == 0) {
				sb.append("[Exc]=");
			} else {
				sb.append("[");
				if (i == bucketUnit.length) {
					sb.append("Other]=");
				} else {
					sb.append(bucketUnit[i]).append(" ms]=");
				}
			}
			sb.append(tmp2[i].get()).append("\t");
		}
		LOGGER.info(sb.toString());
	}

	class MyTimerTask extends TimerTask {

		@Override
		public void run() {

			clearReqister();
		}

	}

	private void consume(TestSample probe) {
		AtomicLong[] tmp = counters;
		synchronized (tmp) {
			counters[getBucket(probe.getDuration())].incrementAndGet();
		}
	}

	/*
	 * This method is used to find the offset of the bucket in counters.
	 * 'counters' array is 1 size longer than bucketUnit, hence by default it
	 * returns 'bucketUnit.length'
	 */
	private int getBucket(long difftime) {
		for (int i = 0; i < bucketUnit.length; i++) {
			if (difftime < bucketUnit[i]) {
				return i;
			}
		}
		return bucketUnit.length;
	}

	private static boolean isEnabled() {
		return enabled;
	}

	/**
	 * @author Rich Tabedzki
	 *  A helper class to pass measured parameter to the counter.
	 */
	static class TestSample implements Comparable {
		private long starttime;
		private long endtime;

		public TestSample(long starttime) {
			this.endtime = System.currentTimeMillis();
			this.starttime = starttime;
		}

		public long getDuration() {
			return endtime - starttime;
		}

		@Override
		public int compareTo(Object o) {
			if (o instanceof TestSample) {
				TestSample x = (TestSample) o;
				if (starttime < x.starttime)
					return 1;
				if (endtime < x.endtime)
					return 1;
				if (starttime > x.starttime)
					return -1;
				if (endtime > x.endtime)
					return -1;
				return 0;
			}
			return 1;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (endtime ^ (endtime >>> 32));
			result = prime * result + (int) (starttime ^ (starttime >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TestSample other = (TestSample) obj;
			if (endtime != other.endtime)
				return false;
			if (starttime != other.starttime)
				return false;
			return true;
		}
	}
}
