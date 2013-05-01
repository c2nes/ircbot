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

package com.brewtab.irc.messages;

import java.util.EnumMap;

public class MessageValidator {
    /**
     * This class represents a constraint placed on the number of arguments for
     * a give type of message. It facilitates rudimentary error checking on
     * message construction.
     * 
     * @author Christopher Thunes <cthunes@brewtab.com>
     */
    private static class ArgumentConstraint {
        /** Minimum number of arguments or -1 */
        private int min;

        /** Maximum number of argumnets or -1 */
        private int max;

        /**
         * Initialize a new constraint with the given min and max inclusive
         * 
         * @param min The minimum number of arguments or -1
         * @param max The maximum number of arguments or -1
         */
        private ArgumentConstraint(int min, int max) {
            this.min = min;
            this.max = max;
        }

        /**
         * Return a new constraint requiring at least the given number of
         * arguments
         * 
         * @param n The least number of arguments
         * @return the new constraint
         */
        @SuppressWarnings("unused")
        public static ArgumentConstraint atLeast(int n) {
            return new ArgumentConstraint(n, -1);
        }

        /**
         * Return a new constraint requiring at most the given number of
         * arguments
         * 
         * @param n The most number of arguments
         * @return the new constraint
         */
        public static ArgumentConstraint atMost(int n) {
            return new ArgumentConstraint(-1, n);
        }

        /**
         * Return a new constraint requiring at least {@code min} arguments and
         * at most {@code max}
         * 
         * @param min The least number of arguments
         * @param max The most number of arguments
         * @return the new constraint
         */
        public static ArgumentConstraint between(int min, int max) {
            return new ArgumentConstraint(min, max);
        }

        /**
         * Return a new constraint requiring exactly the given number of
         * arguments
         * 
         * @param n The number of arguments required
         * @return the new constraint
         */
        public static ArgumentConstraint exactly(int n) {
            return new ArgumentConstraint(n, n);
        }

        /**
         * Check the constraint against the given argument list
         * 
         * @param args The argument list
         * @return true if valid, false otherwise
         */
        public boolean check(String[] args) {
            if (this.min != -1 && args.length < this.min) {
                return false;
            }

            if (this.max != -1 && args.length > this.max) {
                return false;
            }

            return true;
        }
    }

    /** Mapping of message constraints to allow message formats to be enforced */
    private static EnumMap<MessageType, ArgumentConstraint> argumentConstraints;

    static {
        argumentConstraints = new EnumMap<MessageType, ArgumentConstraint>(MessageType.class);
        argumentConstraints.put(MessageType.NICK, ArgumentConstraint.exactly(1));
        argumentConstraints.put(MessageType.PASS, ArgumentConstraint.exactly(1));
        argumentConstraints.put(MessageType.USER, ArgumentConstraint.exactly(4));
        argumentConstraints.put(MessageType.QUIT, ArgumentConstraint.atMost(1));

        argumentConstraints.put(MessageType.JOIN, ArgumentConstraint.between(1, 2));
        argumentConstraints.put(MessageType.PART, ArgumentConstraint.between(1, 2));
        argumentConstraints.put(MessageType.MODE, ArgumentConstraint.between(2, 5));
        argumentConstraints.put(MessageType.TOPIC, ArgumentConstraint.between(1, 2));
        argumentConstraints.put(MessageType.NAMES, ArgumentConstraint.atMost(1));

        argumentConstraints.put(MessageType.PRIVMSG, ArgumentConstraint.exactly(2));
    }

    public static boolean isValid(Message message) {
        ArgumentConstraint constraint = argumentConstraints.get(message.getType());

        if (constraint == null) {
            return true;
        } else {
            return constraint.check(message.getArgs());
        }
    }
}
