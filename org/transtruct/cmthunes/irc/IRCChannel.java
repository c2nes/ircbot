package org.transtruct.cmthunes.irc;

import java.util.*;

import org.transtruct.cmthunes.irc.messages.*;
import org.transtruct.cmthunes.irc.messages.filter.*;
import org.transtruct.cmthunes.util.*;

public class IRCChannel implements IRCMessageHandler {
    private String name;
    private IRCClient client;
    private Flag joined;
    private LinkedList<IRCChannelListener> listeners;
    
    private class NamesRequestHandler implements IRCMessageHandler {
        private LinkedList<String> names;
        private Flag done;

        public NamesRequestHandler(){
            this.names = new LinkedList<String>();
            this.done = new Flag();
        }

        @Override
        public void handleMessage(IRCMessage message) {
            switch(message.getType()) {
            case RPL_NAMREPLY:
                String[] args = message.getArgs();
                for(String name : args[args.length - 1].split(" ")) {
                    names.add(name);
                }
                break;
                
            case RPL_ENDOFNAMES:
                this.done.set();
                break;
            }
        }
        
        public String[] getNames() {
            this.done.waitUninterruptiblyFor(true);
            
            String[] tempNames = new String[this.names.size()];
            tempNames = this.names.toArray(tempNames);
            return tempNames;
        }
    }

    public IRCChannel(IRCClient client, String name) {
        this.client = client;
        this.name = name;
        this.joined = new Flag();
        this.listeners = new LinkedList<IRCChannelListener>();

        IRCMessageFilter filter = IRCMessageFilters.newChannelFilter(name);
        this.client.addHandler(filter, this);
    }

    public String getName() {
        return this.name;
    }

    public IRCClient getClient() {
        return this.client;
    }

    public boolean doJoin() {
        IRCMessage joinMessage = new IRCMessage(IRCMessageType.JOIN, this.name);
        this.client.getConnection().sendMessage(joinMessage);
        this.joined.waitUninterruptiblyFor(true);

        return true;
    }

    public void part(String reason) {
        IRCMessage partMessage = new IRCMessage(IRCMessageType.PART, this.name, reason);
        this.client.getConnection().sendMessage(partMessage);
        this.joined.clear();
    }

    public void write(String text) {
        IRCMessage privMessage = new IRCMessage(IRCMessageType.PRIVMSG, this.name, text);
        this.client.getConnection().sendMessage(privMessage);
    }

    public String[] getNames() {
        IRCMessage namesMessage = new IRCMessage(IRCMessageType.NAMES, this.name);
        IRCMessageFilter filter = new IRCMessageFilter() {
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
        };

        NamesRequestHandler handler = new NamesRequestHandler();

        this.client.addHandler(filter, handler);
        this.client.getConnection().sendMessage(namesMessage);
        
        String[] names = handler.getNames();
        this.client.removeHandler(filter);
        
        return names;
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
             * Once we've received a join message from the server we have
             * actually joined
             */
            if(this.joined.isSet() == false) {
                this.joined.set();
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

        default:
            break;
        }
    }

    public void addListener(IRCChannelListener listener) {
        this.listeners.add(listener);
    }
}
