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

package com.brewtab.irc.messages.filter;

import java.util.concurrent.atomic.AtomicBoolean;

import com.brewtab.irc.User;
import com.brewtab.irc.messages.Message;
import com.brewtab.irc.messages.MessageType;

/**
 * Provides convenient filters
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public class MessageFilters {
    /** A filter that matches any message */
    private static final MessageFilter PASS = new MessageFilter() {
        @Override
        public boolean check(Message message) {
            return true;
        }
    };

    public static MessageFilter pass() {
        return PASS;
    }

    public static MessageFilter all(final MessageFilter... filters) {
        return new MessageFilter() {
            @Override
            public boolean check(Message message) {
                for (MessageFilter filter : filters) {
                    if (!filter.check(message)) {
                        return false;
                    }
                }

                return true;
            }
        };
    }

    public static MessageFilter any(final MessageFilter... filters) {
        return new MessageFilter() {
            @Override
            public boolean check(Message message) {
                for (MessageFilter filter : filters) {
                    if (filter.check(message)) {
                        return true;
                    }
                }

                return false;
            }
        };
    }

    public static MessageFilter not(final MessageFilter filter) {
        return new MessageFilter() {
            @Override
            public boolean check(Message message) {
                return !filter.check(message);
            }
        };
    }

    public static MessageFilter none(MessageFilter... filters) {
        return not(any(filters));
    }

    public static MessageFilter prefix(final User user) {
        return new MessageFilter() {
            @Override
            public boolean check(Message message) {
                User other = User.fromPrefix(message.getPrefix());

                if (other == null) {
                    return false;
                }

                if (user == null) {
                    return true;
                }

                String nick = user.getNick();
                if (nick != null && !nick.equals(other.getNick())) {
                    return false;
                }

                String username = user.getUser();
                if (username != null && !username.equals(other.getUser())) {
                    return false;
                }

                String host = user.getHost();
                if (host != null && !host.equals(other.getHost())) {
                    return false;
                }

                return true;
            }
        };
    }

    public static MessageFilter prefix(final String prefix) {
        return new MessageFilter() {
            @Override
            public boolean check(Message message) {
                return prefix.equals(message.getPrefix());
            }
        };
    }

    public static MessageFilter message(final MessageType type, final String... args) {
        return new MessageFilter() {
            @Override
            public boolean check(Message message) {
                if (type != null && type != message.getType()) {
                    return false;
                }

                if (message.getArgs().length < args.length) {
                    return false;
                }

                int i = 0;
                for (String arg : args) {
                    if (arg != null && !arg.equals(message.getArgs()[i])) {
                        return false;
                    }

                    i++;
                }

                return true;
            }
        };
    }

    public static MessageFilter range(final MessageFilter match, final MessageFilter last) {
        return new MessageFilter() {
            private boolean ended = false;

            @Override
            public boolean check(Message message) {
                if (ended) {
                    return false;
                } else if (last.check(message)) {
                    ended = true;
                    return true;
                } else if (match != null) {
                    return match.check(message);
                } else {
                    return false;
                }
            }
        };
    }

    public static MessageFilter once(final MessageFilter filter) {
        return new MessageFilter() {
            AtomicBoolean match = new AtomicBoolean(true);

            @Override
            public boolean check(Message message) {
                if (match.get() && filter.check(message)) {
                    return match.getAndSet(false);
                } else {
                    return false;
                }
            }
        };
    }
}
