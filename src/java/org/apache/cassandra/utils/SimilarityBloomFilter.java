/*
 * Licensed to the Apache Software Foundation (ASF) under one
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

package org.apache.cassandra.utils;

import org.apache.cassandra.utils.concurrent.WrappedSharedCloseable;
import org.apache.cassandra.utils.obs.IBitSet;

public class SimilarityBloomFilter extends WrappedSharedCloseable implements IFilter
{
    SimilarityBloomFilter(int hashCount, IBitSet bitset)
    {
        super(bitset);
//        this.hashCount = hashCount;
//        this.bitset = bitset;
    }

    SimilarityBloomFilter(BloomFilter copy)
    {
        super(copy);
//        this.hashCount = copy.hashCount;
//        this.bitset = copy.bitset;
    }

    public static final SimilarityBloomFilterSerializer serializer = new SimilarityBloomFilterSerializer();

    public void add(FilterKey key)
    {

    }

    public boolean isPresent(FilterKey key)
    {
        return false;
    }

    public void clear()
    {

    }

    public long serializedSize()
    {
        return 0;
    }

    public IFilter sharedCopy()
    {
        return null;
    }

    public long offHeapSize()
    {
        return 0;
    }
}
