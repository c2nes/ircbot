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

import org.junit.Test;


import static org.junit.Assert.*;

public class TestIRCUser {
    @Test
    public void testFromPrefix0() {
        User user = User.fromPrefix("nick!user@host");

        assertEquals("nick", user.getNick());
        assertEquals("user", user.getUser());
        assertEquals("host", user.getHost());
    }

    @Test
    public void testFromPrefix1() {
        User user = User.fromPrefix("nick@host");

        assertEquals("nick", user.getNick());
        assertNull(user.getUser());
        assertEquals("host", user.getHost());
    }

    @Test
    public void testFromPrefix2() {
        User user = User.fromPrefix("nick");

        assertEquals("nick", user.getNick());
        assertNull(user.getUser());
        assertNull(user.getHost());
    }

    @Test
    public void testInvalidFromPrefix3() {
        User user = User.fromPrefix("!user@host");
        assertNull(user);
    }

    @Test
    public void testInvalidFromPrefix4() {
        User user = User.fromPrefix("@host");
        assertNull(user);
    }

    @Test
    public void testInvalidFromPrefix5() {
        User user = User.fromPrefix("nick!user@");
        assertNull(user);
    }

    @Test
    public void testInvalidFromPrefix6() {
        User user = User.fromPrefix("nick!@host");
        assertNull(user);
    }

    @Test
    public void testInvalidFromPrefix7() {
        User user = User.fromPrefix("");
        assertNull(user);
    }

    @Test
    public void testInvalidFromPrefix8() {
        User user = User.fromPrefix("!@");
        assertNull(user);
    }

    @Test
    public void testArguments0() {
        try {
            new User("nick", "user", "host");
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void testArguments1() {
        try {
            new User("nick", null, null);
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void testArguments2() {
        try {
            new User("nick", null, "host");
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void testInvalidArguments0() {
        try {
            new User(null, "user", "host");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testInvalidArguments1() {
        try {
            new User("nick", "user", null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testToPrefix0() {
        User user = new User("nick", "user", "host");
        assertEquals("nick!user@host", user.toPrefix());
    }

    @Test
    public void testToPrefix1() {
        User user = new User("nick", null, null);
        assertEquals("nick", user.toPrefix());
    }

    @Test
    public void testToPrefix2() {
        User user = new User("nick", null, "host");
        assertEquals("nick@host", user.toPrefix());
    }
}
