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

package com.brewtab.irc.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.brewtab.irc.Connection;
import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;
import com.brewtab.irc.client.ChannelListener;
import com.brewtab.irc.client.Client;
import com.brewtab.irc.messages.Message;
import com.brewtab.irc.messages.MessageListener;
import com.brewtab.irc.messages.MessageType;
import com.brewtab.irc.messages.filter.MessageFilters;

/**
 * An IRC channel. Represents a connection to an IRC channel.
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
class ChannelImpl implements MessageListener, Channel {
    /* Channel name */
    private String channelName;

    /* IRC Client */
    private Client client;

    /* Associated IRCConnection object */
    private Connection connection;

    /* Set once the channel has been joined */
    private CountDownLatch joined;

    /* IRCChanneListners for this channel */
    private List<ChannelListener> listeners;

    /* List of nicks in the channel */
    private List<String> names;

    /**
     * Instantiate a new IRCChannel object associated with the given IRCClient
     * 
     * @param client The client to operate with
     * @param channelName The channel to join
     */
    public ChannelImpl(Client client, String channelName) {
        this.client = client;
        this.connection = client.getConnection();
        this.channelName = channelName;
        this.joined = new CountDownLatch(1);
        this.listeners = new LinkedList<ChannelListener>();
        this.names = new ArrayList<String>();

        /*
         * Build a compound filter collecting messages sent regarding this
         * channel and include all QUIT messages. Some QUIT messages will
         * concern this channel if it's a user in this channel QUITing
         */

        connection.addMessageListener(
            MessageFilters.any(
                // Messages targeted to the channel
                MessageFilters.message(MessageType.PRIVMSG, channelName),
                MessageFilters.message(MessageType.JOIN, channelName),
                MessageFilters.message(MessageType.PART, channelName),
                MessageFilters.message(MessageType.RPL_TOPIC, channelName),
                MessageFilters.message(MessageType.RPL_NOTOPIC, channelName),
                MessageFilters.message(MessageType.RPL_NAMREPLY, null, "=", channelName),
                MessageFilters.message(MessageType.RPL_ENDOFNAMES, null, channelName),

                // Messages which may be relevant to our channel
                MessageFilters.message(MessageType.QUIT),
                MessageFilters.message(MessageType.NICK)),
            this);
    }

    /*
     * (non-Javadoc)
     * @see com.brewtab.irc.impl.IRCChannel#getName()
     */
    @Override
    public String getName() {
        return this.channelName;
    }

    /*
     * (non-Javadoc)
     * @see com.brewtab.irc.impl.IRCChannel#getClient()
     */
    @Override
    public Client getClient() {
        return this.client;
    }

    /**
     * Perform the join to the channel. Returns once the join operation is
     * complete
     * 
     * @return true if joined successfully, false otherwise
     */
    public boolean join() {
        Message joinMessage = new Message(MessageType.JOIN, this.channelName);
        List<Message> response;

        try {
            response = connection.request(
                MessageFilters.message(MessageType.RPL_NAMREPLY, null, "=", channelName),
                MessageFilters.message(MessageType.RPL_ENDOFNAMES, null, channelName),
                joinMessage);
        } catch (InterruptedException e) {
            return false;
        }

        List<String> names = new ArrayList<String>();

        for (Message message : response) {
            if (message.getType() == MessageType.RPL_NAMREPLY) {
                String[] args = message.getArgs();

                for (String name : args[args.length - 1].split(" ")) {
                    names.add(name.replaceFirst("^[@+]", ""));
                }
            }
        }

        this.names = names;

        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.brewtab.irc.impl.IRCChannel#part(java.lang.String)
     */
    @Override
    public void part(String reason) {
        Message partMessage = new Message(MessageType.PART, this.channelName, reason);
        this.client.getConnection().send(partMessage);
        this.joined = new CountDownLatch(1);
    }

    /*
     * (non-Javadoc)
     * @see com.brewtab.irc.impl.IRCChannel#write(java.lang.String)
     */
    @Override
    public void write(String text) {
        Message privMessage = new Message(MessageType.PRIVMSG, this.channelName, text);
        this.client.getConnection().send(privMessage);
    }

    /*
     * (non-Javadoc)
     * @see com.brewtab.irc.impl.IRCChannel#writeMultiple(java.lang.String)
     */
    @Override
    public void writeMultiple(String... strings) {
        Message[] privMessages = new Message[strings.length];
        for (int i = 0; i < strings.length; i++) {
            privMessages[i] = new Message(MessageType.PRIVMSG, this.channelName, strings[i]);
        }
        this.client.getConnection().send(privMessages);
    }

    /*
     * (non-Javadoc)
     * @see com.brewtab.irc.impl.IRCChannel#getNames()
     */
    @Override
    public List<String> getNames() {
        return names;
    }

    /**
     * Makes a NAMES request to the server for this channel. Store the result
     * replacing any existing names list. The list can be retrieved with
     * IRCChannel#getNames
     */
    public void refreshNames() {
        Message namesMessage = new Message(MessageType.NAMES, this.channelName);
        List<Message> response;

        try {
            response = connection.request(
                MessageFilters.message(MessageType.RPL_NAMREPLY, null, "=", channelName),
                MessageFilters.message(MessageType.RPL_ENDOFNAMES, null, channelName),
                namesMessage);
        } catch (InterruptedException e) {
            return;
        }

        List<String> names = new ArrayList<String>();

        for (Message message : response) {
            if (message.getType() == MessageType.RPL_NAMREPLY) {
                String[] args = message.getArgs();

                for (String name : args[args.length - 1].split(" ")) {
                    names.add(name.replaceFirst("^[@+]", ""));
                }
            }
        }

        this.names = names;
    }

    /*
     * (non-Javadoc)
     * @see com.brewtab.irc.impl.IRCChannel#addListener(com.brewtab.irc.client.
     * IRCChannelListener)
     */
    @Override
    public void addListener(ChannelListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void onMessage(Message message) {
        User user = User.fromPrefix(message.getPrefix());

        switch (message.getType()) {
        case JOIN:
            /* Must have valid user prefix */
            if (user == null) {
                break;
            }

            /*
             * Once we've received a join message from the server we ourselves
             * have actually joined
             */
            this.joined.countDown();

            /* Add user to names list */
            if (!this.names.contains(user.getNick())) {
                this.names.add(user.getNick());
            }

            /* Call listeners */
            for (ChannelListener listener : this.listeners) {
                listener.onJoin(this, user);
            }
            break;

        case PART:
            /* Must have valid user prefix */
            if (user == null) {
                break;
            }

            /* Remove nick from names list */
            if (this.names.contains(user.getNick())) {
                this.names.remove(user.getNick());
            }

            /* Call listeners */
            for (ChannelListener listener : this.listeners) {
                listener.onPart(this, user);
            }
            break;

        case PRIVMSG:
            /* Must have valid user prefix */
            if (user == null) {
                break;
            }

            /* Call listeners */
            for (ChannelListener listener : this.listeners) {
                listener.onMessage(this, user, message.getArgs()[1]);
            }
            break;

        case QUIT:
            /* Must have valid user prefix */
            if (user == null) {
                break;
            }

            /* Remove nick from names list */
            if (this.names.contains(user.getNick())) {
                this.names.remove(user.getNick());
            }

            /* Call listeners */
            for (ChannelListener listener : this.listeners) {
                listener.onQuit(this, user);
            }
            break;

        case NICK:
            /* Must have valid user prefix */
            if (user == null) {
                break;
            }

            /* Replace nick in names list */
            if (this.names.contains(user.getNick())) {
                this.names.remove(user.getNick());
                this.names.add(message.getArgs()[0]);
            }
            break;

        default:
            break;
        }
    }
}
