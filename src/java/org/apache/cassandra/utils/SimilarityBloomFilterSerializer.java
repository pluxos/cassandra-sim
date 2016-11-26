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

import java.io.DataInput;
import java.io.IOException;

import org.apache.cassandra.db.TypeSizes;
import org.apache.cassandra.io.ISerializer;
import org.apache.cassandra.io.util.DataOutputPlus;
import org.apache.cassandra.utils.obs.IBitSet;
import org.apache.cassandra.utils.obs.LocalitySensitiveBitSet;

public class SimilarityBloomFilterSerializer implements ISerializer<SimilarityBloomFilter>
{
    public void serialize(SimilarityBloomFilter bf, DataOutputPlus out) throws IOException
    {
        out.writeInt(bf.hashCount);
        out.writeInt(bf.bitset.length);
        for (IBitSet bs : bf.bitset)
        {
            bs.serialize(out);
        }
    }

    public SimilarityBloomFilter deserialize(DataInput in) throws IOException
    {
        int hashes = in.readInt();
        int length = in.readInt();
        IBitSet[] bs = new IBitSet[length];
        for (int i = 0; i < length; i++)
        {
            bs[i] = LocalitySensitiveBitSet.deserialize(in);
        }
        return createFilter(hashes, bs);
    }

    SimilarityBloomFilter createFilter(int hashes, IBitSet[] bs)
    {
        return new SimilarityBloomFilter(hashes, bs);
    }

    public long serializedSize(SimilarityBloomFilter bf, TypeSizes typeSizes)
    {
        int size = typeSizes.sizeof(bf.hashCount); // hash count
        size += typeSizes.sizeof(bf.bitset.length);
        for (IBitSet bs : bf.bitset)
        {
            size += bs.serializedSize(typeSizes);
        }
        return size;
    }
}
