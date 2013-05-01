/*
 * Copyright (c) 2013 Christopher Thunes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.brewtab.irc.client;

import com.brewtab.irc.User;

/**
 * Implementing class can listen for IRC channel related events
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public interface ChannelListener {
    /**
     * Called when a user joins the channel
     * 
     * @param channel The channel that was joined
     * @param user The user that joined
     */
    public void onJoin(Channel channel, User user);

    /**
     * Called when a user parts the channel
     * 
     * @param channel The channel that was parted from
     * @param user The user that parted
     */
    public void onPart(Channel channel, User user);

    /**
     * Called when a user quits the channel
     * 
     * @param channel The channel that was quit from
     * @param user The user that parted
     */
    public void onQuit(Channel channel, User user);

    /**
     * Called whenever a message is sent to the channel
     * 
     * @param channel The channel the message was received on
     * @param from The user that sent the message
     * @param message The message
     */
    public void onMessage(Channel channel, User from, String message);
}
