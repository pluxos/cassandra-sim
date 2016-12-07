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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.TypeSizes;
import org.apache.cassandra.utils.concurrent.WrappedSharedCloseable;
import org.apache.cassandra.utils.obs.IBitSet;
import org.apache.cassandra.utils.obs.LocalitySensitiveBitSet;

public class SimilarityBloomFilter extends WrappedSharedCloseable implements IFilter
{
    private static final Logger logger = LoggerFactory.getLogger(SimilarityBloomFilter.class);

    public static final float R = 2F;

    private static final int BLOOM_L = 4;    // the number of bloom(parameter L in lsh)
    private static final int W = 4;        // The default value for lsh algorithm parameter W

    private static final long UH_PRIME_DEFAULT = 4294967291L & 0xffffffff;   // 2^29
    // private static final long MAX_HASH_RND = 536870912L & 0xffffffff;     // lsh_r(1,MAX_HASH_RND)

    private long lsh_r[];    //used to calculate the gindex of lsh, gindex=((lsh_r*a)mod prime)mod tableSize
    public final IBitSet bitset[];
    public final int hashCount;

    public static int getBitSetNum()
    {
        return BLOOM_L;
    }

    SimilarityBloomFilter(int hashCount, IBitSet[] bitset)
    {
        super(bitset);
        this.hashCount = hashCount;
        this.bitset = bitset;

        this.lsh_r = SimilarityHashUtil.RANDOM_INTS;
    }

    SimilarityBloomFilter(SimilarityBloomFilter copy)
    {
        super(copy);
        this.hashCount = copy.hashCount;
        this.bitset = copy.bitset;

        this.lsh_r = copy.lsh_r;
    }

    public static final SimilarityBloomFilterSerializer serializer = new SimilarityBloomFilterSerializer();

    public long serializedSize()
    {
        return serializer.serializedSize(this, TypeSizes.NATIVE);
    }

    public void add(FilterKey key)
    {
        float data[] = getData(key);
        long index, temp[];
        for (int i = 0; i < BLOOM_L; i++)
        {
            temp = getVector((LocalitySensitiveBitSet) bitset[i], data, R);
            index = getIndex((LocalitySensitiveBitSet) bitset[i], temp);
            bitset[i].set(index);
        }
    }

    private float[] getData(FilterKey key)
    {
        String token = ((DecoratedKey) key).getToken().toString();
        float[] data = new float[SimilarityHashUtil.dimention];

        int i = 0;
        for (char bit : token.toCharArray()) {
            data[i] = (bit == '1') ? 1 : 0;
            i++;
        }

        return data;
    }

    private long[] getVector(LocalitySensitiveBitSet lsbs, float[] f, float R)
    {
        long[] temp = new long[lsbs.getnFuncs()];
        float result;
        for (int i = 0; i < lsbs.getnFuncs(); i++)
        {
            result = lsbs.getLshParamB()[i];
            for (int k = 0; k < SimilarityHashUtil.dimention; k++)
            {
                result += f[k] * (lsbs.getLshParamA()[i][k] / R);
            }
            result /= W;
            temp[i] = (long) Math.floor(result); // h(v) = (a.v+b)/w
            temp[i] = unsignedRepresentation(temp[i]);
        }
        return temp;
    }

    private long unsignedRepresentation(long l)
    {
        long uMax = 1l<<32;
        l %= uMax;
        l += uMax;
        l %= uMax;
        return l;
    }

    private long getIndex(LocalitySensitiveBitSet lsbs, long[] temp)
    {
        long index = 0;
        for (int i = 0; i < lsbs.getnFuncs(); i++)
        {
            index += temp[i] * lsh_r[i];
            index %= 2L<<32;
        }
        index %= UH_PRIME_DEFAULT;
        index %= lsbs.getBitsSize();
        return index;    //gIndex = g(v) =((lsh_r*h(v))mod prime)mod tableSize
    }

    public boolean isPresent(FilterKey key)
    {
        // check whether a point is similar to a Set for one bloom,
        // if the point's index +1 or -1 bit is 1, then we can say this bloom is true
        // if all bloom are true, then return true
        float data[] = getData(key);
        long temp[], index;
        int j;

        for (int i = 0; i < BLOOM_L; i++)
        {
            LocalitySensitiveBitSet bitset = (LocalitySensitiveBitSet) this.bitset[i];

            temp = getVector(bitset, data, R);
            index = getIndex(bitset, temp);

            if (bitset.get(index))
            {
                continue;
            }
            else
            {
                for (j = 0; j < bitset.getnFuncs(); j++)
                {
                    temp[j] -= 1;
                    index = getIndex(bitset, temp);
                    if (bitset.get(index))
                    {
                        break;
                    }
                    temp[j] += 2;
                    index = getIndex(bitset, temp);
                    if (bitset.get(index))
                    {
                        break;
                    }
                    temp[j] -= 1;
                }
                if (j == bitset.getnFuncs())
                {
                    return false;
                }
            }
        }

        return true;
    }

    public void clear()
    {
        for (IBitSet bs : bitset)
        {
            bs.clear();
        }
    }

    public IFilter sharedCopy()
    {
        return new SimilarityBloomFilter(this);
    }

    @Override
    public long offHeapSize()
    {
        long offHeapSize = 0L;
        for (IBitSet bs : bitset)
        {
            offHeapSize += bs.offHeapSize();
        }
        return offHeapSize;
    }
}
