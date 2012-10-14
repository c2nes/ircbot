package com.brewtab.irc.messages;

import java.util.EnumMap;
import java.util.LinkedList;

/**
 * Represents an IRCMessage. A message is a single request or reply sent by
 * either a client or server. It has three parts: an optional prefix, a type,
 * and a number of arguments.
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 * @see <a href="http://www.irchelp.org/irchelp/rfc/rfc.html">
 *      http://www.irchelp.org/irchelp/rfc/rfc.html</a>
 */
public class IRCMessage {
    /** The messages prefix */
    private String prefix;

    /** The messages type */
    private IRCMessageType type;

    /** The messages arguments */
    private String[] args;

    /**
     * This class represents a constrain placed on the number of arguments for a
     * give type of message. It facilitates rudimentary error checking on
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
         * @param min
         *            The minimum number of arguments or -1
         * @param max
         *            The maximum number of arguments or -1
         */
        private ArgumentConstraint(int min, int max) {
            this.min = min;
            this.max = max;
        }

        /**
         * Return a new constraint requiring at least the given number of
         * arguments
         * 
         * @param n
         *            The least number of arguments
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
         * @param n
         *            The most number of arguments
         * @return the new constraint
         */
        public static ArgumentConstraint atMost(int n) {
            return new ArgumentConstraint(-1, n);
        }

        /**
         * Return a new constraint requiring at least {@code min} arguments and
         * at most {@code max}
         * 
         * @param min
         *            The least number of arguments
         * @param max
         *            The most number of arguments
         * @return the new constraint
         */
        public static ArgumentConstraint between(int min, int max) {
            return new ArgumentConstraint(min, max);
        }

        /**
         * Return a new constraint requiring exactly the given number of
         * arguments
         * 
         * @param n
         *            The number of arguments required
         * @return the new constraint
         */
        public static ArgumentConstraint exactly(int n) {
            return new ArgumentConstraint(n, n);
        }

        /**
         * Check the constraint against the given argument list
         * 
         * @param args
         *            The argument list
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
    private static EnumMap<IRCMessageType, ArgumentConstraint> argumentConstraints;

    static {
        argumentConstraints = new EnumMap<IRCMessageType, ArgumentConstraint>(IRCMessageType.class);
        argumentConstraints.put(IRCMessageType.NICK, ArgumentConstraint.exactly(1));
        argumentConstraints.put(IRCMessageType.PASS, ArgumentConstraint.exactly(1));
        argumentConstraints.put(IRCMessageType.USER, ArgumentConstraint.exactly(4));
        argumentConstraints.put(IRCMessageType.QUIT, ArgumentConstraint.atMost(1));

        argumentConstraints.put(IRCMessageType.JOIN, ArgumentConstraint.between(1, 2));
        argumentConstraints.put(IRCMessageType.PART, ArgumentConstraint.between(1, 2));
        argumentConstraints.put(IRCMessageType.MODE, ArgumentConstraint.between(2, 5));
        argumentConstraints.put(IRCMessageType.TOPIC, ArgumentConstraint.between(1, 2));
        argumentConstraints.put(IRCMessageType.NAMES, ArgumentConstraint.atMost(1));

        argumentConstraints.put(IRCMessageType.PRIVMSG, ArgumentConstraint.exactly(2));
    }

    /**
     * Construct a message with the given type and arguments
     * 
     * @param type
     *            The message type
     * @param args
     *            The message arguments
     * @throws IRCInvalidMessageException
     *             if invalid arguments are given
     */
    public IRCMessage(IRCMessageType type, String... args) throws IRCInvalidMessageException {
        this(null, type, args);
    }

    /**
     * Construct a message with the given prefix, type, and arguments
     * 
     * @param prefix
     *            The message prefix
     * @param type
     *            The message arguments
     * @param args
     *            The message arguments
     * @throws IRCInvalidMessageException
     *             if invalid arguments are given
     */
    public IRCMessage(String prefix, IRCMessageType type, String... args) throws IRCInvalidMessageException {
        this.prefix = prefix;
        this.type = type;
        this.args = args;

        if (this.validArgs() == false) {
            throw new IRCInvalidMessageException("Invalid arguments");
        }
    }

    /**
     * Verify the validity of the constructed message
     * 
     * @return true if the message is valid, false otherwise
     */
    private boolean validArgs() {
        ArgumentConstraint constraint = IRCMessage.argumentConstraints.get(this.type);

        if (constraint != null) {
            return constraint.check(this.args);
        }

        return true;
    }

    /**
     * Format the message as a String. Returns the message formatted as it would
     * be as a newline terminated part of an IRC exchange.
     * 
     * @return the message encoded as a String with \r\n included
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        if (this.prefix != null) {
            buffer.append(":").append(this.prefix).append(" ");
        }

        buffer.append(this.type.toString());

        if (this.args.length > 0) {
            for (int i = 0; i < this.args.length - 1; i++) {
                buffer.append(" ").append(this.args[i]);
            }

            String lastArg = this.args[this.args.length - 1];
            switch (this.type) {
            case PRIVMSG:
            case USER:
            case PART:
            case QUIT:
            case KICK:
                buffer.append(" :").append(lastArg);
                break;

            default:
                buffer.append(" ").append(lastArg);
            }
        }

        buffer.append("\r\n");
        return buffer.toString();
    }

    /**
     * Decode the given IRC message a string into an IRCMessage object
     * 
     * @param messageFrame
     *            The message as read from the connection stream
     * @return a new IRCMessage object
     * @throws IRCInvalidMessageException
     *             if the format of the message is invalid
     */
    public static IRCMessage fromString(String messageFrame) throws IRCInvalidMessageException {
        String prefix = null;
        String trailing = null;

        messageFrame = messageFrame.trim();

        /* Remove prefix */
        if (messageFrame.startsWith(":")) {
            int splitPoint = messageFrame.indexOf(' ');
            prefix = messageFrame.substring(1, splitPoint);
            messageFrame = messageFrame.substring(splitPoint + 1).trim();
        }

        /* Remove trailing part */
        int trailingPoint = messageFrame.indexOf(':');
        if (trailingPoint != -1) {
            trailing = messageFrame.substring(trailingPoint + 1);
            messageFrame = messageFrame.substring(0, trailingPoint).trim();
        }

        /* Remove type */
        int endTypePoint = messageFrame.indexOf(' ');
        String typeString;
        if (endTypePoint == -1) {
            typeString = messageFrame;
            messageFrame = "";
        } else {
            typeString = messageFrame.substring(0, endTypePoint);
            messageFrame = messageFrame.substring(endTypePoint + 1).trim();
        }

        /* Attempt a type lookup for the message type */
        IRCMessageType type = IRCMessageType.fromString(typeString);
        if (type == null) {
            throw new IRCInvalidMessageException("invalid type: " + typeString);
        }

        /*
         * Split the remaining args into an arguments list and append the
         * trailing argument if it exists
         */
        LinkedList<String> argsList = new LinkedList<String>();
        while (messageFrame.length() > 0) {
            int nextSpace = messageFrame.indexOf(' ');
            if (nextSpace == -1) {
                argsList.add(messageFrame);
                break;
            } else {
                argsList.add(messageFrame.substring(0, nextSpace));
                messageFrame = messageFrame.substring(nextSpace + 1).trim();
            }
        }

        if (trailing != null) {
            argsList.add(trailing);
        }

        String[] args = new String[argsList.size()];
        return new IRCMessage(prefix, type, argsList.toArray(args));
    }

    /**
     * Get the message prefix
     * 
     * @return the message prefix
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Get the message type
     * 
     * @return the message type
     */
    public IRCMessageType getType() {
        return this.type;
    }

    /**
     * Get the message arguments
     * 
     * @return the message arguments
     */
    public String[] getArgs() {
        return this.args;
    }
}
