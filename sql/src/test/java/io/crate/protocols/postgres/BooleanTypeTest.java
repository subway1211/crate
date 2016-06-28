/*
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */

package io.crate.protocols.postgres;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BooleanTypeTest {

    private PGType.BooleanType booleanType;

    @Before
    public void setUp() throws Exception {
        booleanType = new PGType.BooleanType();
    }

    @Test
    public void writeTextValue() throws Exception {
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        int bytesWritten = booleanType.writeTextValue(buffer, true);
        assertThat(bytesWritten, is(5));

        byte[] bytes = new byte[bytesWritten];
        buffer.getBytes(0, bytes);
        byte[] expectedTrueBytes = new byte[]{ 0, 0, 0, 1, 't' };
        assertThat(bytes, is(expectedTrueBytes));

        buffer = ChannelBuffers.dynamicBuffer();
        booleanType.writeTextValue(buffer, false);
        buffer.getBytes(0, bytes);
        byte[] expectedFalseBytes = new byte[]{ 0, 0, 0, 1, 'f' };
        assertThat(bytes, is(expectedFalseBytes));
    }

    @Test
    public void readTextValue() throws Exception {
        ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(new byte[] { 'f' });
        boolean value = (boolean) booleanType.readTextValue(buffer, 1);
        assertThat(value, is(false));

        buffer = ChannelBuffers.wrappedBuffer(new byte[] { 'T', 'R', 'U', 'E' });
        value = (boolean) booleanType.readTextValue(buffer, 4);
        assertThat(value, is(true));
    }

    @Test
    public void writeBinaryValue() throws Exception {
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        int bytesWritten = booleanType.writeBinaryValue(buffer, true);
        assertThat(bytesWritten, is(5));

        byte[] bytes = new byte[bytesWritten];
        buffer.getBytes(0, bytes);
        byte[] expectedTrueBytes = new byte[]{ 0, 0, 0, 1, 1 };
        assertThat(bytes, is(expectedTrueBytes));
    }

    @Test
    public void readBinaryValue() throws Exception {
        ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(new byte[] { 1 });
        boolean value = (boolean) booleanType.readBinaryValue(buffer, 1);
        assertThat(value, is(true));
    }
}