package com.brewtab.irc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brewtab.irc.messages.IRCMessage;
import com.brewtab.irc.messages.IRCMessageType;
import com.brewtab.irc.messages.filter.IRCMessageFilter;
import com.brewtab.irc.messages.filter.IRCMessageSimpleFilter;
import com.brewtab.irc.messages.filter.IRCUserPrefixFilter;

/**
 * Have a private conversation
 * 
 * @author Christopher Thunes <cthunes@brewtab.com>
 */
public class IRCPrivateChat implements IRCMessageHandler {
    private final static Logger log = LoggerFactory.getLogger(IRCPrivateChat.class);

    /* Nick of the user receiving the messages */
    private String receiver;

    /* Associated IRCClient object */
    private IRCClient client;

    public IRCPrivateChat(IRCClient client, String nick) {
        this.client = client;
        this.receiver = nick;

        IRCMessageFilter filter = new IRCMessageSimpleFilter(IRCMessageType.PRIVMSG, new IRCUserPrefixFilter(nick));
        this.client.addHandler(filter, this);
    }

    public String getReceiver() {
        return this.receiver;
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
     * Write a message to the channel
     * 
     * @param text
     *            The message to send
     */
    public void write(String text) {
        IRCMessage privMessage = new IRCMessage(IRCMessageType.PRIVMSG, this.receiver, text);
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
        for (int i = 0; i < strings.length; i++) {
            privMessages[i] = new IRCMessage(IRCMessageType.PRIVMSG, this.receiver, strings[i]);
        }
        this.client.getConnection().sendMessages(privMessages);
    }

    @Override
    public void handleMessage(IRCMessage message) {
        IRCUser user = IRCUser.fromString(message.getPrefix());
        String messageText = message.getArgs()[message.getArgs().length - 1];

        log.debug("PM from {}: {}", user.getNick(), messageText);
    }
}
