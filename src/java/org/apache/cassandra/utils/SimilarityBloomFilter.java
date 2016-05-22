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

import org.apache.cassandra.db.TypeSizes;
import org.apache.cassandra.utils.concurrent.WrappedSharedCloseable;
import org.apache.cassandra.utils.obs.IBitSet;

public class SimilarityBloomFilter extends WrappedSharedCloseable implements IFilter
{
    public static final int W = 4;
    public final int dimention = 6;

    public final IBitSet bitset; // a (size of bitset == asize)
    public final int hashCount; // nfuncs
    public final double[][] paramA; // para_a
    public final double[] paramB; // para_b

    SimilarityBloomFilter(int hashCount, IBitSet bitset)
    {
        super(bitset);
        this.hashCount = hashCount;
        this.bitset = bitset;
        this.paramA = generateParamA();
        this.paramB = generateParamB();
    }

    SimilarityBloomFilter(SimilarityBloomFilter copy)
    {
        super(copy);
        hashCount = copy.hashCount;
        bitset = copy.bitset;
        paramA = copy.paramA;
        paramB = copy.paramB;
    }

    private double[][] generateParamA()
    {
        double[][] a = new double[hashCount][dimention];

        for (int l = 0; l < hashCount; l++)
        {
            for (int k = 0; k < dimention; k++)
            {
                a[l][k] = SimilarityHashUtil.genGaussianRandom();
            }
        }

        return a;
    }

    private double[] generateParamB()
    {
        double[] b = new double[hashCount];

        for (int l = 0; l < hashCount; l++)
        {
            b[l] = SimilarityHashUtil.genUniformRandom(0, W);
        }

        return b;
    }

    public static final SimilarityBloomFilterSerializer serializer = new SimilarityBloomFilterSerializer();

    public long serializedSize()
    {
        return serializer.serializedSize(this, TypeSizes.NATIVE);
    }

    public void add(FilterKey key)
    {

    }

    public boolean isPresent(FilterKey key)
    {
        return false;
    }

    public void clear()
    {
        bitset.clear();
    }

    public IFilter sharedCopy()
    {
        return new SimilarityBloomFilter(this);
    }

    @Override
    public long offHeapSize()
    {
        return bitset.offHeapSize();
    }
}
