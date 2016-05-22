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

import java.util.Random;

public class SimilarityHashUtil
{
    private static final double PI = 3.1415926;
    private static final int RAND_MAX = 32767;

    public static double genGaussianRandom()
    {
        // Use Box-Muller transform to generate a point from normal distribution.
        double x1, x2, z;

        do
        {
            x1 = genUniformRandom(0.0, 1.0);
        } while (x1 == 0); // cannot take log of 0.

        x2 = genUniformRandom(0.0, 1.0);
        z = Math.sqrt(-2.0 * Math.log(x1)) * Math.cos(2.0 * PI * x2);

        return z;
    }

    public static double genUniformRandom(double rangeStart, double rangeEnd)
    {
        double r;

        do
        {
            r = rangeStart + ((rangeEnd - rangeStart) * new Random().nextInt(RAND_MAX + 1) / (double) RAND_MAX);
        } while (r < rangeStart || r > rangeEnd);

        return r;
    }

    public static int genRandomInt(int rangeStart, int rangeEnd)
    {
        int r;

        do
        {
            r = rangeStart + (int) ((rangeEnd - rangeStart + 1.0) * new Random().nextInt(RAND_MAX + 1) / (RAND_MAX + 1.0));
        } while (r < rangeStart || r > rangeEnd);

        return r;
    }
}
