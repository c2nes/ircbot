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
public class Message {
    /** The messages prefix */
    private String prefix;

    /** The messages type */
    private MessageType type;

    /** The messages arguments */
    private String[] args;

    /**
     * Construct a message with the given type and arguments
     * 
     * @param type The message type
     * @param args The message arguments
     * @throws InvalidMessageException if invalid arguments are given
     */
    public Message(MessageType type, String... args) throws InvalidMessageException {
        this(null, type, args);
    }

    /**
     * Construct a message with the given prefix, type, and arguments
     * 
     * @param prefix The message prefix
     * @param type The message arguments
     * @param args The message arguments
     * @throws InvalidMessageException if invalid arguments are given
     */
    public Message(String prefix, MessageType type, String... args) throws InvalidMessageException {
        this.prefix = prefix;
        this.type = type;
        this.args = args;

        if (!MessageValidator.isValid(this)) {
            throw new InvalidMessageException("Invalid arguments");
        }
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
    public MessageType getType() {
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
     * @param messageFrame The message as read from the connection stream
     * @return a new IRCMessage object
     * @throws InvalidMessageException if the format of the message is
     *             invalid
     */
    public static Message fromString(String messageFrame) throws InvalidMessageException {
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
        MessageType type = MessageType.fromString(typeString);
        if (type == null) {
            throw new InvalidMessageException("invalid type: " + typeString);
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
        return new Message(prefix, type, argsList.toArray(args));
    }
}
