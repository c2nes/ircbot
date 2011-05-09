package org.transtruct.cmthunes.irc;

import java.util.*;

import org.transtruct.cmthunes.irc.messages.*;
import org.transtruct.cmthunes.irc.messages.filter.*;
import org.transtruct.cmthunes.util.*;

/**
 * An IRC channel. Represents a connection to an IRC channel.
 * 
 * @author Christopher Thunes <cthunes@transtruct.org>
 */
public class IRCChannel implements IRCMessageHandler {
    /* Channel name */
    private String name;

    /* Associated IRCClient object */
    private IRCClient client;

    /* Set once the channel has been joined */
    private Flag joined;

    /* IRCChanneListners for this channel */
    private LinkedList<IRCChannelListener> listeners;

    /* List of nicks in the channel */
    private ArrayList<String> names;

    /**
     * Message handler that receives responses from NAMES requests for this
     * channel and creates a list of names. NamesRequestHandler#getNames can be
     * called to wait for the list to be fully populated at which time all names
     * are returned as an array.
     * 
     * @author Christopher Thunes <cthunes@transtruct.org>
     * 
     */
    private class NamesRequestHandler implements IRCMessageHandler, IRCMessageFilter {
        private ArrayList<String> names;
        private Flag done;

        /**
         * Create a new NamesRequestHandler
         */
        public NamesRequestHandler() {
            this.names = new ArrayList<String>();
            this.done = new Flag();
        }

        @Override
        public void handleMessage(IRCMessage message) {
            switch(message.getType()) {
            case RPL_NAMREPLY:
                String[] args = message.getArgs();
                for(String name : args[args.length - 1].split(" ")) {
                    names.add(name.replaceFirst("^[@+]", ""));
                }
                break;

            case RPL_ENDOFNAMES:
                this.done.set();
                break;
            }
        }

        @Override
        public boolean check(IRCMessage message) {
            String channelName = IRCChannel.this.getName();
            String[] args = message.getArgs();

            switch(message.getType()) {
            case RPL_NAMREPLY:
                if(args.length > 2 && args[2].equals(channelName)) {
                    return true;
                }
                break;

            case RPL_ENDOFNAMES:
                if(args.length > 1 && args[1].equals(channelName)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Wait for a full request to be replied to and then return the list of
         * names
         * 
         * @return the list of names in the channel
         */
        public ArrayList<String> getNames() {
            this.done.waitUninterruptiblyFor(true);
            return this.names;
        }
    }

    /**
     * Instantiate a new IRCChannel object associated with the given IRCClient
     * 
     * @param client
     *            The client to operate with
     * @param name
     *            The channel to join
     */
    public IRCChannel(IRCClient client, String name) {
        this.client = client;
        this.name = name;
        this.joined = new Flag();
        this.listeners = new LinkedList<IRCChannelListener>();
        this.names = null;

        /*
         * Build a compound filter collecting messages sent regarding this
         * channel and include all QUIT messages. Some QUIT messages will
         * concern this channel if it's a user in this channel QUITing
         */
        IRCMessageFilter channelFilter = IRCMessageFilters.newChannelFilter(name);
        IRCMessageFilter quitFilter = IRCMessageFilters.newTypeFilter(IRCMessageType.QUIT);
        IRCMessageFilter filter = new IRCMessageOrFilter(channelFilter, quitFilter);

        this.client.addHandler(filter, this);
    }

    /**
     * Get the channel name
     * 
     * @return the channel name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the associated IRCClient
     * 
     * @return the associated IRCClient
     */
    public IRCClient getClient() {
        return this.client;
    }

    /**
     * Perform the join to the channel. Returns once the join operation is
     * complete
     * 
     * @return true if joined successfully, false otherwise
     */
    public boolean doJoin() {
        IRCMessage joinMessage = new IRCMessage(IRCMessageType.JOIN, this.name);
        NamesRequestHandler namesHandler = new NamesRequestHandler();

        this.client.addHandler(namesHandler, namesHandler);
        this.client.getConnection().sendMessage(joinMessage);
        this.joined.waitUninterruptiblyFor(true);
        this.names = namesHandler.getNames();

        this.client.removeHandler(namesHandler);

        return true;
    }

    /**
     * Part the channel
     * 
     * @param reason
     *            The reason sent with the PART message
     */
    public void part(String reason) {
        IRCMessage partMessage = new IRCMessage(IRCMessageType.PART, this.name, reason);
        this.client.getConnection().sendMessage(partMessage);
        this.joined.clear();
    }

    /**
     * Write a message to the channel
     * 
     * @param text
     *            The message to send
     */
    public void write(String text) {
        IRCMessage privMessage = new IRCMessage(IRCMessageType.PRIVMSG, this.name, text);
        this.client.getConnection().sendMessage(privMessage);
    }

    /**
     * Write multiple messages efficiently
     * 
     * @param strings
     *            The messages to send
     */
    public void writeMultiple(String... strings) {
        IRCMessage[] privMessages = new IRCMessage[strings.length];
        for(int i = 0; i < strings.length; i++) {
            privMessages[i] = new IRCMessage(IRCMessageType.PRIVMSG, this.name, strings[i]);
        }
        this.client.getConnection().sendMessages(privMessages);
    }

    /**
     * Retrieve the list of users in the channel. This call returns a cached
     * copy. To request a refresh of the listing from the server call
     * IRChannel#refreshNames.
     * 
     * @return the list of nicks of users in the channel
     */
    public String[] getNames() {
        String[] tempNames = new String[this.names.size()];
        tempNames = this.names.toArray(tempNames);
        return tempNames;
    }

    /**
     * Makes a NAMES request to the server for this channel. Store the result
     * replacing any existing names list. The list can be retrieved with
     * IRCChannel#getNames
     */
    public void refreshNames() {
        IRCMessage message = new IRCMessage(IRCMessageType.NAMES, this.name);
        NamesRequestHandler handler = new NamesRequestHandler();

        this.client.addHandler(handler, handler);
        this.client.getConnection().sendMessage(message);
        this.names = handler.getNames();
        this.client.removeHandler(handler);
    }

    /**
     * Add an IRCChannelListener to this channel.
     * 
     * @param listener
     *            the listener to add
     */
    public void addListener(IRCChannelListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void handleMessage(IRCMessage message) {
        IRCUser user = IRCUser.fromString(message.getPrefix());
        
        switch(message.getType()) {
        case JOIN:
            /* Must have valid user prefix */
            if(user == null) {
                break;
            }

            /*
             * Once we've received a join message from the server we ourselves
             * have actually joined
             */
            if(this.joined.isSet() == false) {
                this.joined.set();
            }

            /* Add user to names list */
            if(!this.names.contains(user.getNick())) {
                this.names.add(user.getNick());
            }

            /* Call listeners */
            for(IRCChannelListener listener : this.listeners) {
                listener.onJoin(this, user);
            }
            break;

        case PART:
            /* Must have valid user prefix */
            if(user == null) {
                break;
            }

            /* Remove nick from names list */
            if(this.names.contains(user.getNick())) {
                this.names.remove(user.getNick());
            }

            /* Call listeners */
            for(IRCChannelListener listener : this.listeners) {
                listener.onPart(this, user);
            }
            break;

        case PRIVMSG:
            /* Must have valid user prefix */
            if(user == null) {
                break;
            }

            /* Call listeners */
            for(IRCChannelListener listener : this.listeners) {
                listener.onPrivateMessage(this, message.getArgs()[1], user);
            }
            break;

        case QUIT:
            /* Must have valid user prefix */
            if(user == null) {
                break;
            }

            /* Remove nick from names list */
            if(this.names.contains(user.getNick())) {
                this.names.remove(user.getNick());
            }

            /* Call listeners */
            for(IRCChannelListener listener : this.listeners) {
                listener.onQuit(this, user);
            }
            break;

        default:
            break;
        }
    }
}
