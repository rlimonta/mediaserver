/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
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

package org.mobicents.media.control.mgcp.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import org.apache.log4j.Logger;
import org.mobicents.media.control.mgcp.MgcpMessage;
import org.mobicents.media.control.mgcp.MgcpMessageParser;
import org.mobicents.media.control.mgcp.listener.MgcpMessageListener;
import org.mobicents.media.server.io.network.UdpManager;
import org.mobicents.media.server.io.network.channel.MultiplexedChannel;
import org.mobicents.media.server.io.network.channel.PacketHandler;

/**
 * UDP channel that handles MGCP traffic.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class MgcpChannel extends MultiplexedChannel implements MgcpMessageListener {

    private static final Logger log = Logger.getLogger(MgcpChannel.class);

    // Core Components
    private final UdpManager networkManager;
    
    // MGCP Components
    private final MgcpMessageParser parser;
    private final PacketHandler mgcpHandler;

    // MGCP Channel
    private final String address;
    private final int port;
    private boolean open;

    public MgcpChannel(String address, int port, UdpManager networkManager) {
        // Core Components
        this.networkManager = networkManager;

        // MGCP Components
        this.parser = new MgcpMessageParser();
        
        // MGCP Channel
        this.address = address;
        this.port = port;
        this.mgcpHandler = new MgcpPacketHandler(this.parser, this);
        this.handlers.addHandler(mgcpHandler);
        this.open = false;
    }

    @Override
    public void open() throws IllegalStateException, IOException {
        if (this.open) {
            throw new IllegalStateException("MGCP channel is already open.");
        } else {
            // Open channel
            this.selectionKey = this.networkManager.open(this);
            this.dataChannel = (DatagramChannel) this.selectionKey.channel();

            // Bind channel
            try {
                this.dataChannel.bind(new InetSocketAddress(this.address, this.port));
            } catch (IOException e) {
                log.error("BOMBED!!");
                close();
                throw e;
            }

            // Declare the channel officially active
            this.open = true;
            if (log.isInfoEnabled()) {
                log.info("MGCP Channel is open on " + this.address + ":" + this.port);
            }
        }
    }

    @Override
    public void close() throws IllegalStateException {
        if (this.open) {
            // Close the channel
            super.close();

            // Declare channel officially inactive
            this.open = false;
            if (log.isInfoEnabled()) {
                log.info("MGCP Channel is closed");
            }
        } else {
            throw new IllegalStateException("MGCP channel is already closed.");
        }
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    public void onMgcpMessage(MgcpMessage request) {
        // TODO Auto-generated method stub
        
    }

}