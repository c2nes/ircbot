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

import com.brewtab.irc.Connection;
import com.brewtab.irc.User;

public interface Client {

    public String getUsername();

    public String getHostname();

    public String getRealName();

    public void setNick(String nick);

    public String getNick();

    /**
     * Quit and disconnect from the server
     * 
     * @param message
     */
    public void quit(String message);

    /**
     * Quit and disconnect from the server using a default message.
     */
    public void quit();

    /**
     * Join a channel
     * 
     * @param channelName
     * @return
     */
    public Channel join(String channelName);

    /**
     * Send a message to a user
     * 
     * @param nick
     * @param message
     */
    public void sendMessage(User user, String message);

    /**
     * Get the connection used by the client object
     * 
     * @return
     */
    public Connection getConnection();
}
