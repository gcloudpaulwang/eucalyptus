/*************************************************************************
 * Copyright 2009-2012 Eucalyptus Systems, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Please contact Eucalyptus Systems, Inc., 6755 Hollister Ave., Goleta
 * CA 93117, USA or visit http://www.eucalyptus.com/licenses/ if you need
 * additional information or have any questions.
 *
 * This file may incorporate work covered under the following copyright
 * and permission notice:
 *
 *   Software License Agreement (BSD License)
 *
 *   Copyright (c) 2008, Regents of the University of California
 *   All rights reserved.
 *
 *   Redistribution and use of this software in source and binary forms,
 *   with or without modification, are permitted provided that the
 *   following conditions are met:
 *
 *     Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *     Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *   COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *   BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *   CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *   LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *   ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGE. USERS OF THIS SOFTWARE ACKNOWLEDGE
 *   THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE LICENSED MATERIAL,
 *   COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS SOFTWARE,
 *   AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
 *   IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA,
 *   SANTA BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY,
 *   WHICH IN THE REGENTS' DISCRETION MAY INCLUDE, WITHOUT LIMITATION,
 *   REPLACEMENT OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO
 *   IDENTIFIED, OR WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT
 *   NEEDED TO COMPLY WITH ANY SUCH LICENSES OR RIGHTS.
 ************************************************************************/

package com.eucalyptus.bootstrap;

import org.apache.log4j.Logger;

import com.eucalyptus.bootstrap.listeners.LogLevelListener;
import com.eucalyptus.bootstrap.listeners.TriggerFaultListener;
import com.eucalyptus.configurable.ConfigurableClass;
import com.eucalyptus.configurable.ConfigurableField;
import com.eucalyptus.empyrean.Empyrean;
import com.eucalyptus.records.Logs;
import com.eucalyptus.troubleshooting.fault.FaultSubsystem;
import com.eucalyptus.troubleshooting.resourcefaults.listeners.DBCheckPollTimeListener;
import com.eucalyptus.troubleshooting.resourcefaults.listeners.DBCheckThresholdListener;
import com.eucalyptus.troubleshooting.resourcefaults.listeners.GarbageCollectionCountCheckNameListener;
import com.eucalyptus.troubleshooting.resourcefaults.listeners.GarbageCollectionCountCheckPollTimeListener;
import com.eucalyptus.troubleshooting.resourcefaults.listeners.GarbageCollectionCountCheckThresholdListener;
import com.eucalyptus.troubleshooting.resourcefaults.listeners.LogFileDiskCheckPollTimeListener;
import com.eucalyptus.troubleshooting.resourcefaults.listeners.LogFileDiskCheckThresholdListener;
import com.eucalyptus.troubleshooting.resourcefaults.listeners.MXBeanMemoryCheckPollTimeListener;
import com.eucalyptus.troubleshooting.resourcefaults.listeners.MXBeanMemoryCheckThresholdListener;
import com.eucalyptus.troubleshooting.resourcefaults.listeners.SimpleMemoryCheckPollTimeListener;
import com.eucalyptus.troubleshooting.resourcefaults.listeners.SimpleMemoryCheckThresholdListener;
import com.eucalyptus.troubleshooting.resourcefaults.schedulers.DBCheckScheduler;
import com.eucalyptus.troubleshooting.resourcefaults.schedulers.GarbageCollectionCountCheckScheduler;
import com.eucalyptus.troubleshooting.resourcefaults.schedulers.LogFileDiskCheckScheduler;
import com.eucalyptus.troubleshooting.resourcefaults.schedulers.MXBeanMemoryCheckScheduler;
import com.eucalyptus.troubleshooting.resourcefaults.schedulers.SimpleMemoryCheckScheduler;

@Provides(Empyrean.class)
@RunDuring(Bootstrap.Stage.CloudServiceInit)
@ConfigurableClass(root = "cloud", description = "Parameters controlling troubleshooting information.")
public class TroubleshootingBootstrapper extends Bootstrapper {

	private static final Logger LOG = Logger
			.getLogger(TroubleshootingBootstrapper.class);

	@Override
	public boolean load() throws Exception {
		return true;
	}

	@Override
	public boolean start() throws Exception {
		LOG.info("Starting troubleshooting interface.");
		LogFileDiskCheckScheduler.resetLogFileDiskCheck();
		DBCheckScheduler.resetDBCheck();
		SimpleMemoryCheckScheduler.resetMemoryCheck();
		MXBeanMemoryCheckScheduler.resetMXBeanMemoryCheck();
		GarbageCollectionCountCheckScheduler.garbageCollectionCountCheck();
		FaultSubsystem.init();
		return true;
	}

	/**
	 * @see com.eucalyptus.bootstrap.Bootstrapper#enable()
	 */
	@Override
	public boolean enable() throws Exception {
		return true;
	}

	/**
	 * @see com.eucalyptus.bootstrap.Bootstrapper#stop()
	 */
	@Override
	public boolean stop() throws Exception {
		return true;
	}

	/**
	 * @see com.eucalyptus.bootstrap.Bootstrapper#destroy()
	 */
	@Override
	public void destroy() throws Exception {
	}

	/**
	 * @see com.eucalyptus.bootstrap.Bootstrapper#disable()
	 */
	@Override
	public boolean disable() throws Exception {
		return true;
	}

	/**
	 * @see com.eucalyptus.bootstrap.Bootstrapper#check()
	 */
	@Override
	public boolean check() throws Exception {
		return true;
	}

	@ConfigurableField(description = "Poll time (ms) for log file disk check", initial = "5000", changeListener = LogFileDiskCheckPollTimeListener.class, displayName = "log.file.disk.check.poll.time")
	public static String LOG_FILE_DISK_CHECK_POLL_TIME = "5000";

	@ConfigurableField(description = "Threshold (bytes or %) for log file disk check", initial = "2.0%", changeListener = LogFileDiskCheckThresholdListener.class, displayName = "log.file.disk.check.threshold")
	public static String LOG_FILE_DISK_CHECK_THRESHOLD = "2.0%";

	@ConfigurableField(description = "Poll time (ms) for db connection check", initial = "60000", changeListener = DBCheckPollTimeListener.class, displayName = "db.check.poll.time")
	public static String DB_CHECK_POLL_TIME = "60000";

	@ConfigurableField(description = "Threshold (num connections or %) for db connection check", initial = "2.0%", changeListener = DBCheckThresholdListener.class, displayName = "db.check.threshold")
	public static String DB_CHECK_THRESHOLD = "2.0%";

	@ConfigurableField(description = "Poll time (ms) for simple memory check", initial = "60000", changeListener = SimpleMemoryCheckPollTimeListener.class, displayName = "simple.memory.check.poll.time")
	public static String SIMPLE_MEMORY_CHECK_POLL_TIME = "60000";

	@ConfigurableField(description = "Threshold (bytes or %) for simple memory check", initial = "2.0%", changeListener = SimpleMemoryCheckThresholdListener.class, displayName = "simple.memory.check.threshold")
	public static String SIMPLE_MEMORY_CHECK_THRESHOLD = "2.0%";

	@ConfigurableField(description = "Poll time (ms) for mxbean memory check", initial = "60000", changeListener = MXBeanMemoryCheckPollTimeListener.class, displayName = "mxbean.memory.check.poll.time")
	public static String MXBEAN_MEMORY_CHECK_POLL_TIME = "60000";

	@ConfigurableField(description = "Threshold (bytes or %) for mxbean memory check", initial = "2.0%", changeListener = MXBeanMemoryCheckThresholdListener.class, displayName = "mxbean.memory.check.threshold")
	public static String MXBEAN_MEMORY_CHECK_THRESHOLD = "2.0%";

	@ConfigurableField(description = "Poll time (ms) for gc count check", initial = "1000", changeListener = GarbageCollectionCountCheckPollTimeListener.class, displayName = "gc.count.check.poll.time")
	public static String GC_COUNT_CHECK_POLL_TIME = "1000";

	@ConfigurableField(description = "Threshold (number of collection counts) for gc count check", initial = "100", changeListener = GarbageCollectionCountCheckThresholdListener.class, displayName = "gc.count.check.threshold")
	public static String GC_COUNT_CHECK_THRESHOLD = "100";

	@ConfigurableField(description = "Name of the garbage collector to check for gc count check", initial = "PS MarkSweep", changeListener = GarbageCollectionCountCheckNameListener.class, displayName = "gc.count.check.name")
	public static String GC_COUNT_CHECK_NAME = "PS MarkSweep";

	@ConfigurableField( description = "Fault id last used to trigger test", initial = "", changeListener = TriggerFaultListener.class, displayName = "trigger.fault" )
	public static String TRIGGER_FAULT = "";

	// TODO: figure out how to link initial value to System.property("euca.log.level")
	@ConfigurableField(description = "Log level for dynamic override.", initial = "", changeListener = LogLevelListener.class, displayName = "euca.log.level")
	public static String EUCA_LOG_LEVEL = Logs.isExtrrreeeme() ? "EXTREME" : System.getProperty("euca.log.level", "");

}
