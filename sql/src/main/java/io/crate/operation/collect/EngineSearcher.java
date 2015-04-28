/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.operation.collect;

import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.IllegalIndexShardStateException;
import org.elasticsearch.index.shard.IndexShardState;
import org.elasticsearch.index.shard.service.IndexShard;

import javax.annotation.Nullable;

public class EngineSearcher {

    public static Engine.Searcher getSearcherWithRetry(IndexShard indexShard,
                                                       String searcherName,
                                                       @Nullable Engine.Searcher searcher) {
        Engine.Searcher engineSearcher = searcher;
        int retry = 0;
        while (engineSearcher == null) {
            try {
                engineSearcher = indexShard.acquireSearcher(searcherName);
            } catch (IllegalIndexShardStateException e) {
                if (e.currentState() == IndexShardState.POST_RECOVERY && retry < 10) {
                    try {
                        Thread.sleep(retry);
                    } catch (InterruptedException e1) {
                        throw e;
                    }
                    retry++;
                    continue;
                }
                throw e;
            }
        }
        return engineSearcher;
    }
}
