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

import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.sdk.SourceRunner;
import com.streamsets.pipeline.sdk.StageRunner;
import com.streamsets.tibcorv.stage.origin.TibcorvDSubscriber;

import org.junit.Test;

import java.util.List;

public class TestTibcorvSubscriber {
	private static final int MAX_BATCH_SIZE = 5;

	@Test
	public void testOrigin() throws Exception {
		SourceRunner runner = new SourceRunner.Builder(TibcorvDSubscriber.class).addConfiguration("subjects", "a.b.c,e.f.g")
				.addConfiguration("queueBufferSize", 100).addConfiguration("queuePollWaitMilliseconds", 5000)
				.addOutputLane("lane").build();

		try {
			runner.runInit();

			final String lastSourceOffset = null;
			StageRunner.Output output = runner.runProduce(lastSourceOffset, MAX_BATCH_SIZE);
			List<Record> records = output.getRecords().get("lane");
			records.forEach(e -> System.out.println("record: " + e));
		} finally {
			runner.runDestroy();
		}
	}

}
