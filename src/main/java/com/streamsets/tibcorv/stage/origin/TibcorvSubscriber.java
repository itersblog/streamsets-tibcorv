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

import com.streamsets.pipeline.api.BatchMaker;
import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.base.BaseSource;
import com.streamsets.tibcorv.stage.lib.Errors;
import com.streamsets.tibcorv.stage.lib.TibcorvListener;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgField;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This source is an example and does not actually read from anywhere. It does
 * however, generate generate a simple record with one field.
 */
public abstract class TibcorvSubscriber extends BaseSource {
	private static final Logger log = LoggerFactory.getLogger(TibcorvSubscriber.class);

	/**
	 * Gives access to the UI configuration of the stage provided by the
	 * {@link TibcorvDSubscriber} class.
	 */
	public abstract String getSubjects();

	public abstract String getService();

	public abstract String getNetwork();

	public abstract String getDaemon();

	public abstract Integer getQueueBufferSize();

	public abstract Integer getQueuePollWaitMilliseconds();

	private TibcorvListener tibcorvListener = null;

	@Override
	protected List<ConfigIssue> init() {
		// Validate configuration values and open any required resources.
		List<ConfigIssue> issues = super.init();

		boolean valid = true;
		if (!getSubjects().matches("[^,]+(,[^,]+)*")) {
			issues.add(getContext().createConfigIssue(Groups.Subscription.name(), "subjects", Errors.INVALID_FORMAT,
					"subject1,subject2,subject3"));
			valid = false;
		}
		if (getQueueBufferSize() < 100 || getQueueBufferSize() > 100000000) {
			issues.add(getContext().createConfigIssue(Groups.Subscription.name(), "queueBufferSize",
					Errors.OUT_OF_RANGE, 100, 100000000));
			valid = false;
		}
		if (getQueuePollWaitMilliseconds() < 1000 || getQueuePollWaitMilliseconds() > 60000) {
			issues.add(getContext().createConfigIssue(Groups.Subscription.name(), "queuePollWaitMilliseconds",
					Errors.OUT_OF_RANGE, 1000, 60000));
			valid = false;
		}
		if (valid) {
			String subjects = getSubjects();
			String service = getService() != null && getService().trim().length() == 0 ? null : getService().trim();
			String network = getNetwork() != null && getNetwork().trim().length() == 0 ? null : getNetwork().trim();
			String daemon = getDaemon() != null && getDaemon().trim().length() == 0 ? null : getDaemon().trim();
			Integer queueBufferSize = getQueueBufferSize();
			Integer queuePollWaitMilliseconds = getQueuePollWaitMilliseconds();
			tibcorvListener = new TibcorvListener(subjects, service, network, daemon, queueBufferSize,
					queuePollWaitMilliseconds);
			if (!tibcorvListener.start()) {
				throw new IllegalStateException("Start " + TibcorvListener.class.getSimpleName() + " failed");
			}
		}

		// If issues is not empty, the UI will inform the user of each configuration
		// issue in the list.
		return issues;
	}

	/** {@inheritDoc} */
	@Override
	public void destroy() {
		// Clean up any open resources.
		super.destroy();
		if (tibcorvListener != null) {
			tibcorvListener.stop();
			tibcorvListener = null;
		}
	}

	private Field convert(Object value) {
		if (value instanceof Byte)
			return Field.create((Byte) value);
		else if (value instanceof Boolean)
			return Field.create((Boolean) value);
		else if (value instanceof Short)
			return Field.create((Short) value);
		else if (value instanceof Integer)
			return Field.create((Integer) value);
		else if (value instanceof Float)
			return Field.create((Float) value);
		else if (value instanceof Long)
			return Field.create((Long) value);
		else if (value instanceof Double)
			return Field.create((Double) value);
		else if (value instanceof Character)
			return Field.create((Character) value);
		else if (value instanceof BigDecimal)
			return Field.create((BigDecimal) value);
		else if (value instanceof byte[])
			return Field.create((byte[]) value);
		else if (value instanceof Date)
			return Field.createDate((Date) value);
		else
			return Field.create(value.toString());
	}

	/** {@inheritDoc} */
	@Override
	public String produce(String lastSourceOffset, int maxBatchSize, BatchMaker batchMaker) throws StageException {
		int numRecords = 0;
		TibrvMsg msg = null;
		while (numRecords++ < maxBatchSize && (msg = tibcorvListener.poll()) != null) {
			// the unique ID to identify the record
			Record record = getContext().createRecord(lastSourceOffset = UUID.randomUUID().toString());
			Map<String, Field> map = new HashMap<>();
			Map<String, Field> data = new HashMap<>();
			try {
				for (int i = 0; i < msg.getNumFields(); i++) {
					TibrvMsgField field = msg.getFieldByIndex(i);
					data.put(field.name, convert(field.data));
				}
			} catch (TibrvException e) {
				log.error(e.getMessage(), e);
			}
			map.put("subject", Field.create(msg.getSendSubject()));
			map.put("data", Field.create(data));
			record.set(Field.create(map));
			batchMaker.addRecord(record);
		}

		return lastSourceOffset;
	}

}
