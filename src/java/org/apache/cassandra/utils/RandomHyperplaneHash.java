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

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Random;

import org.apache.cassandra.config.DatabaseDescriptor;

public class RandomHyperplaneHash
{
    public BitSet rhh(ByteBuffer key)
    {
        return rhh(key, DatabaseDescriptor.getIdentifierLength(), DatabaseDescriptor.getVectors());
    }

    private static BitSet rhh(ByteBuffer key, int bits, double[][] vectors)
    {
        double[] vectorKey = getVectorKey(key);

        BitSet hash = new BitSet(bits);

        double sum;

        int i = 0;
        int j = 0;

        while (i < vectors.length)
        {
            sum = scalarProduct(vectorKey, vectors[i]);
            if (sum >= 0.0)
            {
                hash.set(j, true);
            }
            else
            {
                hash.set(j, false);
            }
            i++;
            j++;
        }

        return hash;
    }

    public BitSet rhh()
    {
        return rhh(DatabaseDescriptor.getIdentifierLength(), DatabaseDescriptor.getVectors());
    }

    private static BitSet rhh(int bits, double[][] vectors)
    {
        int dataSize = 8;
        int size = vectors[0].length;

        Random seed = new Random();
        ByteBuffer key = ByteBuffer.allocate((dataSize * size) + (3 * size));
        for (int i = 0; i < size; i++)
        {
            byte[] bytes = new byte[dataSize];
            seed.nextBytes(bytes);
            ByteBufferUtil.writeShortLength(key, dataSize);
            key.put(ByteBuffer.wrap(bytes));
            key.put((byte) 0);
        }

        key.rewind();

        return rhh(key, bits, vectors);
    }

    private static double[] getVectorKey(ByteBuffer key)
    {
        double[] vector;
        if (key.remaining() < 2)
        {
            vector = getVectorKeyByteByByte(key);
        }
        else
        {
            try
            {
                int length;
                int dataSize = 0;
                int dimension = getKeyDimension(key, dataSize);
                vector = new double[dimension];
                for (int i = 0; i < dimension; i++)
                {
                    length = ByteBufferUtil.readShortLength(key);
                    double value;
                    if (length == 4)
                        value = key.getInt();
                    else if (length == 8)
                        value = key.getDouble();
                    else
                        value = key.get();
                    key.get();
                    vector[i] = value;
                }
            } catch (Exception e) {
                key.rewind();
                vector = getVectorKeyByteByByte(key);
            }
        }
        key.rewind();
        return vector;
    }

    private static double[] getVectorKeyByteByByte(ByteBuffer key)
    {
        double[] vector = new double[key.remaining()];
        int i = 0;
        while (key.hasRemaining())
        {
            vector[i] = key.get();
            i++;
        }
        return vector;
    }

    private static int getKeyDimension(ByteBuffer key, int dataSize)
    {
        int dimension = 0;

        int pos = 0;
        while (key.hasRemaining())
        {
            int length = ByteBufferUtil.readShortLength(key);
            dataSize += length;
            pos += 3 + length;
            key.position(pos);
            dimension++;
        }

        key.rewind();

        return dimension;
    }

    private static double scalarProduct(double[] v1, double[] v2)
    {
        double scalarProduct = 0;
        int length = Math.min(v1.length, v2.length);

        for (int i = 0; i < length; i++)
        {
            scalarProduct = scalarProduct + (v1[i] * v2[i]);
        }

        return scalarProduct;
    }
}
