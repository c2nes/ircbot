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

package com.brewtab.irc;

/**
 * Thrown to indicate that an operation was request that could be not be
 * completed because a connection has not been established
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public class NotConnectedException extends IllegalStateException {
    private static final long serialVersionUID = 4861994587138843189L;

    /**
     * Construct a new IRCNotConnectedException with the default message
     */
    public NotConnectedException() {
        super("Not connected");
    }

    /**
     * Construct a new IRCNotConnectedException with the give message
     * 
     * @param reason
     *            The message to store along with the exception
     */
    public NotConnectedException(String reason) {
        super(reason);
    }
}
