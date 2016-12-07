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

import org.apache.cassandra.config.DatabaseDescriptor;

public class SimilarityHashUtil
{
    public static final long[] RANDOM_INTS = {
        (451072346 & 0xffffffff), (211732722 & 0xffffffff), (420423195 & 0xffffffff)
    };

    // public static final int dimention = 6;    // dimension
    public static final int dimention = DatabaseDescriptor.getIdentifierLength();    // dimension

    public static final float[][] RANDOM_UNIFORM = {
        {3.6f, 3.9f, 3.0f},
        {3.4f, 2.5f, 3.6f},
        {2.3f, 0.9f, 1.8f},
        {2.3f, 0.9f, 1.8f}
    };

    public static final float[][][] RANDOM_GAUSSIAN = {
        {
            {-0.5f, 0.2f, 0.1f, 0.2f, -1.5f, -0.8f, -1.4f, 0.3f, -0.2f, -1.5f, 0.1f, 0.7f, -1.6f, 1.6f, 0.0f, 0.6f, -0.3f, -0.9f, 1.2f, 0.2f, 0.1f, 1.1f, -1.0f, 0.6f, 2.2f, 1.0f, -0.9f, -0.3f, 2.3f, 1.2f, 1.7f, 0.3f},
            {-1.6f, 0.1f, -0.6f, 1.1f, 1.2f, -0.1f, -0.1f, -0.6f, 0.4f, 0.9f, -0.3f, 1.3f, -0.6f, -0.3f, 0.6f, 1.1f, 0.5f, 0.7f, 0.3f, 0.3f, 1.4f, -0.9f, -0.2f, -0.2f, 0.2f, -0.3f, -0.9f, 1.6f, 1.5f, 0.1f, 0.2f, -0.6f},
            {-0.5f, 0.2f, 0.0f, -0.2f, 0.5f, -0.1f, 2.2f, -2.4f, 0.1f, -0.1f, 0.9f, 0.1f, 1.4f, 1.0f, 0.6f, 0.5f, 0.0f, 1.3f, 0.0f, 0.3f, 0.1f, 0.5f, 2.9f, 0.4f, -1.5f, 0.2f, -1.4f, -1.0f, 0.7f, 0.1f, 1.5f, 0.8f}
        },
        {
            {-0.5f, -0.0f, 0.5f, 0.3f, -0.0f, 0.4f, -0.9f, 0.9f, 0.4f, 0.0f, 0.1f, -0.3f, 0.2f, -1.5f, -1.0f, 1.1f, -1.5f, 0.6f, -1.0f, -0.3f, 0.9f, 1.3f, -1.2f, 0.1f, -0.9f, -0.4f, 0.3f, 0.9f, -0.4f, -0.6f, 0.6f, -0.7f},
            {-0.4f, 0.2f, -0.2f, -0.0f, 1.0f, 1.2f, 0.7f, -1.4f, -0.9f, -0.2f, -0.2f, 0.6f, 0.2f, -0.3f, -1.7f, 0.3f, -0.2f, -0.8f, 0.2f, 0.3f, 0.2f, -0.6f, 1.3f, 0.3f, -1.0f, -0.3f, -0.5f, 0.7f, -0.3f, -0.7f, 0.5f, -1.0f},
            {-0.1f, 0.3f, 0.9f, -1.3f, 1.4f, -0.3f, 0.2f, -0.6f, -0.7f, -0.1f, 1.7f, -1.3f, -0.3f, 1.2f, 1.2f, -0.1f, 0.1f, 0.7f, -0.4f, 0.3f, -1.0f, -0.5f, -0.8f, -0.5f, -0.1f, 0.3f, 1.3f, 0.1f, 1.2f, -0.1f, -2.3f, -0.0f}
        },
        {
            {-1.7f, 0.4f, -1.6f, -0.9f, 0.4f, 2.3f, 0.2f, -0.8f, -1.7f, -1.0f, -0.7f, 1.2f, 0.6f, -1.0f, -0.8f, 0.8f, 1.9f, 0.2f, 0.2f, -0.2f, 1.0f, 0.6f, 0.9f, 0.3f, 0.5f, 0.3f, -1.4f, -1.0f, -0.1f, -1.5f, -1.4f, -1.3f},
            {0.6f, 1.1f, -1.0f, -0.1f, -1.0f, 1.0f, 0.3f, -0.3f, 0.9f, 0.6f, -0.6f, -0.9f, -0.3f, -0.3f, 1.9f, -0.9f, -1.3f, -0.8f, 3.7f, -0.1f, 0.6f, -1.0f, 0.7f, -0.4f, 0.2f, -1.9f, -0.3f, 0.2f, -0.4f, -0.5f, 0.7f, 1.5f},
            {1.0f, -1.1f, 0.1f, -0.5f, 0.9f, -0.2f, -0.4f, 0.1f, 0.2f, -1.3f, 0.6f, 0.4f, -0.3f, -1.0f, -0.6f, 0.7f, 1.0f, -0.8f, -0.6f, -2.1f, 1.1f, -0.5f, -0.9f, -1.7f, -1.4f, -0.6f, 0.4f, -1.0f, 1.2f, 2.1f, -0.8f, 0.3f}
        },
        {
            {0.9f, -0.8f, 0.8f, 0.3f, 0.0f, -1.4f, -1.7f, -0.3f, 0.5f, -1.8f, 0.1f, -0.0f, 0.7f, -1.2f, -1.0f, 0.4f, -0.8f, 0.8f, -1.4f, 0.4f, 0.4f, -0.8f, -0.7f, -2.2f, 0.0f, -1.6f, 2.7f, 0.1f, 0.4f, -1.8f, 0.3f, 0.9f},
            {-0.5f, -0.3f, -0.0f, -0.2f, 0.2f, -0.4f, 0.7f, -0.4f, 0.6f, 0.4f, 0.3f, 0.3f, 1.4f, -0.7f, 0.3f, 0.9f, -1.4f, -0.3f, 1.4f, 0.4f, -2.4f, 0.0f, 1.4f, -2.6f, -0.9f, -0.4f, -0.3f, 1.4f, 0.2f, -0.4f, -0.2f, -0.2f},
            {0.2f, 1.0f, 1.2f, 1.1f, 1.0f, 0.5f, 2.1f, -0.1f, 0.5f, -1.0f, 0.6f, -1.0f, 0.1f, -1.1f, 1.1f, -0.2f, -1.8f, -0.5f, -0.5f, -0.2f, -0.4f, 0.1f, -0.7f, 1.6f, -0.5f, -0.7f, 0.7f, -0.1f, -2.3f, -0.4f, 0.5f, -0.0f}
        }
    };

    /*public static final float[][][] RANDOM_GAUSSIAN = {
        {
            {0.5f, -0.9f, -0.1f, -1.0f, -0.6f, 1.1f},
            {-0.2f, 0.5f, 0.9f, 1.0f, 0.3f, 0.9f},
            {2.1f, -1.7f, -0.4f, -1.0f, -1.1f, -0.6f}
        },
        {
            {-0.7f, -0.5f, -0.9f, 0.5f, 0.3f, -0.3f},
            {0.7f, 0.6f, 1.3f, -2.6f, 0.1f, 0.1f},
            {-0.5f, -0.7f, -0.7f, -0.8f, -2.3f, 0.3f}
        },
        {
            {-0.1f, -0.4f, -0.5f, -1.7f, 0.2f, 0.1f},
            {-0.2f, 0.8f, -0.5f, -0.5f, -0.3f, -0.2f},
            {-0.4f, 1.6f, 0.2f, -0.3f, -0.9f, 0.3f}
        },
        {
            {-1.4f, 0.6f, -1.5f, 0.6f, 0.3f, -2.0f},
            {0.7f, -0.1f, -0.1f, -0.3f, -1.4f, 0.9f},
            {0.5f, 0.3f, 0.6f, 0.3f, 0.5f, 0.4f}
        }
    };*/
}
