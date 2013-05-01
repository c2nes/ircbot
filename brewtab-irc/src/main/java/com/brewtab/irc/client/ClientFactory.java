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

import com.brewtab.irc.impl.ClientFactoryImpl;

public abstract class ClientFactory {
    public static final int DEFAULT_PORT = 6667;
    public static final int DEFAULT_SSL_PORT = 6697;

    public static final String DEFAULT_USERNAME = "ircclient";
    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final String DEFAULT_REALNAME = "Brewtab IRC Client";

    public abstract void setUsername(String username);

    public abstract void setHostname(String hostname);

    public abstract void setRealName(String realName);

    public abstract void setNick(String nick);

    public abstract Client connect(String uri);

    public abstract Client connect(String uri, String password);

    public static ClientFactory newInstance() {
        return ClientFactoryImpl.newInstance();
    }
}
