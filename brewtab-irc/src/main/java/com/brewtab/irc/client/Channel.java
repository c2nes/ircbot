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

import java.util.List;

public interface Channel {

    /**
     * Get the channel name
     * 
     * @return the channel name
     */
    public String getName();

    /**
     * Get the associated IRCClient
     * 
     * @return the associated IRCClient
     */
    public Client getClient();

    /**
     * Part the channel
     * 
     * @param reason The reason sent with the PART message
     */
    public void part(String reason);

    /**
     * Write a message to the channel
     * 
     * @param text The message to send
     */
    public void write(String text);

    /**
     * Write multiple messages efficiently
     * 
     * @param strings The messages to send
     */
    public void writeMultiple(String... strings);

    /**
     * Retrieve the list of users in the channel. This call returns a cached
     * copy. To request a refresh of the listing from the server call
     * IRChannel#refreshNames.
     * 
     * @return the list of nicks of users in the channel
     */
    public List<String> getNames();

    /**
     * Add an IRCChannelListener to this channel.
     * 
     * @param listener the listener to add
     */
    public void addListener(ChannelListener listener);

}
