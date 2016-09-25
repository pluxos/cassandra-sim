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

package org.apache.cassandra.utils.obs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.apache.cassandra.db.TypeSizes;

public class LocalitySensitiveBitSet implements IBitSet
{
    private static final int K_hfun = 3;    // the number of h_function(parameter K in lsh)
    private static final int CHAR_BIT = 8;  // 4294967291 = 2^32-5

    private int bitsSize;           // the size of bits
    private short bits[];
    private int nFuncs;             // there are nFuncs function in a bloom(= K)
    private float lshParamA[][];    // parameter a for lsh
    private float lshParamB[];      // parameter b for lsh

    public LocalitySensitiveBitSet(long numBits, float[][] lshParamA, float[] lshParamB)
    {
        this.bitsSize = (int) numBits;
        this.bits = new short[(bitsSize + CHAR_BIT - 1) / CHAR_BIT];
        this.nFuncs = K_hfun;
        this.lshParamA = lshParamA;
        this.lshParamB = lshParamB;
    }

    public LocalitySensitiveBitSet(float[][] lshParamA, float[] lshParamB)
    {
        this(5000L, lshParamA, lshParamB);
    }

    public long capacity()
    {
        return (bitsSize + CHAR_BIT - 1) / CHAR_BIT;
    }

    @Override
    public long offHeapSize()
    {
        return 0;
    }

    public boolean get(long index)
    {
        int pos = (int) (index / CHAR_BIT);
        int posUnsigned = (pos < 0) ? (pos * -1) : pos;
        int result = bits[posUnsigned] & (1 << (index % CHAR_BIT));
        return result != 0;
    }

    public void set(long index)
    {
        int pos = (int) (index / CHAR_BIT);
        int posUnsigned = (pos < 0) ? (pos * -1) : pos;
        bits[posUnsigned] = (short) (bits[posUnsigned] | (1 << (index % CHAR_BIT)));
    }

    public void clear(long index)
    {
        int pos = (int) (index / CHAR_BIT);
        int posUnsigned = (pos < 0) ? (pos * -1) : pos;
        bits[posUnsigned] = (short) 0;
    }

    public void serialize(DataOutput out) throws IOException
    {
        // serialize LSH Param B
        out.writeInt(lshParamB.length);
        for (int i = 0; i < lshParamB.length; i++)
        {
            out.writeFloat(lshParamB[i]);
        }

        // serialize LSH Param A
        out.writeInt(lshParamA.length);
        for (int i = 0; i < lshParamA.length; i++)
        {
            out.writeInt(lshParamA[i].length);
            for (int j = 0; j < lshParamA[i].length; j++)
            {
                out.writeFloat(lshParamA[i][j]);
            }
        }

        // serialize bits
        out.writeInt(bitsSize);
        for (int i = 0; i < bitsSize; i++)
        {
            out.writeShort(bits[i]);
        }
    }

    public long serializedSize(TypeSizes type)
    {
        long size = 0L;

        size += type.sizeof(lshParamB.length) + (lshParamB.length * Float.SIZE);
        size += type.sizeof(lshParamA.length);
        for (int i = 0; i < lshParamA.length; i++)
        {
            size += type.sizeof(lshParamA[i].length) + (lshParamA[i].length * Float.SIZE);
        }
        size += type.sizeof(bitsSize) + (bitsSize * Short.SIZE);

        return size;
    }

    public static LocalitySensitiveBitSet deserialize(DataInput in) throws IOException
    {
        float[][] lshParamA;
        float[] lshParamB;

        // deserialize LSH Param B
        lshParamB = new float[in.readInt()];
        for (int i = 0; i < lshParamB.length; i++)
        {
            lshParamB[i] = in.readFloat();
        }

        // deserialize LSH Param A
        lshParamA = new float[in.readInt()][];
        for (int i = 0; i < lshParamA.length; i++)
        {
            lshParamA[i] = new float[in.readInt()];
            for (int j = 0; j < lshParamA[i].length; j++)
            {
                lshParamA[i][j] = in.readFloat();
            }
        }

        // deserialize bits
        int numBits = in.readInt();
        LocalitySensitiveBitSet bitSet = new LocalitySensitiveBitSet(numBits, lshParamA, lshParamB);
        for (int i = 0; i < bitSet.bits.length; i++)
        {
            bitSet.bits[i] = in.readShort();
        }

        return bitSet;
    }

    public void clear()
    {
        for (long i = 0; i < bitsSize; i++)
        {
            clear(i);
        }
    }

    public void close()
    {
        // noop, let GC do the cleanup.
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalitySensitiveBitSet bitSet = (LocalitySensitiveBitSet) o;

        if (bitsSize != bitSet.bitsSize) return false;
        if (nFuncs != bitSet.nFuncs) return false;
        if (!Arrays.equals(bits, bitSet.bits)) return false;
        if (!Arrays.deepEquals(lshParamA, bitSet.lshParamA)) return false;
        return Arrays.equals(lshParamB, bitSet.lshParamB);
    }

    @Override
    public int hashCode()
    {
        int result = bitsSize;
        result = 31 * result + Arrays.hashCode(bits);
        result = 31 * result + nFuncs;
        result = 31 * result + Arrays.deepHashCode(lshParamA);
        result = 31 * result + Arrays.hashCode(lshParamB);
        return result;
    }

    public int getBitsSize()
    {
        return bitsSize;
    }

    public int getnFuncs()
    {
        return nFuncs;
    }

    public float[][] getLshParamA()
    {
        return lshParamA;
    }

    public float[] getLshParamB()
    {
        return lshParamB;
    }
}
