/**
 * Copyright 2015 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.tibcorv.stage.origin;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.ExecutionMode;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.StageDef;

@StageDef(version = 1, label = "Tibcorv Subscriber", description = "", icon = "default.png", execution = ExecutionMode.STANDALONE, recordsByRef = true, onlineHelpRefUrl = "")
@ConfigGroups(value = Groups.class)
@GenerateResourceBundle
public class TibcorvDSubscriber extends TibcorvSubscriber {

	@ConfigDef(required = true, type = ConfigDef.Type.STRING, label = "Subjects", description = "List of Subject to listen, multiple separated by commas", displayPosition = 0, group = "Subscription")
	public String subjects;

	/** {@inheritDoc} */
	@Override
	public String getSubjects() {
		return subjects;
	}

	@ConfigDef(required = false, type = ConfigDef.Type.STRING, label = "Service", description = "The service name, number, or default entry that the daemon listens on. You can provide the port number for communication from the daemon to the network. The default value is 7500", displayPosition = 0, group = "Subscription")
	public String service;

	/** {@inheritDoc} */
	@Override
	public String getService() {
		return service;
	}

	@ConfigDef(required = false, type = ConfigDef.Type.STRING, label = "Network", description = "The network interface that the daemon connects on. You can provide the multicast-address or ethernet-address for communication from the daemon to the network. This field is empty, by default", displayPosition = 0, group = "Subscription")
	public String network;

	/** {@inheritDoc} */
	@Override
	public String getNetwork() {
		return network;
	}

	@ConfigDef(required = false, type = ConfigDef.Type.STRING, label = "Daemon", description = "Information on how to connect to the Rendezvous daemon. For example, tcp:hostname:port-number or tcp:port-number . The default value is tcp:7500. The TibcorvSubscriber attempts to connect to the daemon that runs on the specified hostname and port number. If the daemon field is left blank, or if the hostname is not specified, the TibcorvSubscriber attempts to start the local Rendezvous daemon. If the hostname is specified, TibcorvSubscriber attempts to connect to the daemon, but does not start the daemon. For example, using tcp:localhost:7500 will cause the TibcorvSubscriber to connect to the local daemon but it will not start the daemon if it is not already started.", displayPosition = 0, group = "Subscription")
	public String daemon;

	/** {@inheritDoc} */
	@Override
	public String getDaemon() {
		return daemon;
	}

	@ConfigDef(required = true, type = ConfigDef.Type.NUMBER, defaultValue = "1000000", label = "Queue Buffer Size", description = "Maximum buffer size of TibcorvListener queue, Range: 100 - 100000000", displayPosition = 0, group = "Subscription")
	public Integer queueBufferSize;

	/** {@inheritDoc} */
	@Override
	public Integer getQueueBufferSize() {
		return queueBufferSize;
	}

	@ConfigDef(required = true, type = ConfigDef.Type.NUMBER, defaultValue = "5000", label = "Queue Poll Wait Milliseconds", description = "Maximum wait milliseconds for Streamsets to poll data from the TibcorvListener queue, Range: 1000 - 60000", displayPosition = 0, group = "Subscription")
	public Integer queuePollWaitMilliseconds;

	/** {@inheritDoc} */
	@Override
	public Integer getQueuePollWaitMilliseconds() {
		return queuePollWaitMilliseconds;
	}

}
