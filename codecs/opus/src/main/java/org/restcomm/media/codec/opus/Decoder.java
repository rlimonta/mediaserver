/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2017, Telestax Inc and individual contributors
 * by the @authors tag. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.restcomm.media.codec.opus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.restcomm.media.spi.dsp.Codec;
import org.restcomm.media.spi.format.Format;
import org.restcomm.media.spi.format.FormatFactory;
import org.restcomm.media.spi.memory.Frame;
import org.restcomm.media.spi.memory.Memory;

/**
 * Implements Opus decoder.
 * 
 * @author Vladimir Morosev (vladimir.morosev@telestax.com)
 * 
 */
public class Decoder implements Codec {

    private final static Logger log = Logger.getLogger(Encoder.class);

    private final static Format opus = FormatFactory.createAudioFormat("opus", 48000, 8, 1);
    private final static Format linear = FormatFactory.createAudioFormat("linear", 48000, 16, 1);

	private OpusJni opusJni = new OpusJni();
    String decoderId = RandomStringUtils.random(10, true, true);
    
    public Decoder() {
    	opusJni.initDecoderNative(decoderId);
    }
    
    @Override
    protected void finalize() throws Throwable {
    	super.finalize();
    	opusJni.closeDecoderNative(decoderId);
    }

    @Override
    public Format getSupportedInputFormat() {
        return opus;
    }

    @Override
    public Format getSupportedOutputFormat() {
        return linear;
    }

    @Override
    public Frame process(Frame frame) {
    	
		short[] decodedData = opusJni.decodeNative(decoderId, frame.getData());
		byte[] output = new byte[2 * decodedData.length];
		ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(decodedData);
    	
        Frame res = Memory.allocate(output.length);
        System.arraycopy(output, 0, res.getData(), 0, output.length);
        
        res.setOffset(0);
        res.setLength(output.length);
        res.setTimestamp(frame.getTimestamp());
        res.setDuration(frame.getDuration());
        res.setSequenceNumber(frame.getSequenceNumber());
        res.setEOM(frame.isEOM());
        res.setFormat(linear);
        res.setHeader(frame.getHeader());
        return res;
    }
}