package com.brewtab.irc.client;

import com.brewtab.irc.Connection;
import com.brewtab.irc.User;

public interface Client {

    public String getUsername();

    public String getHostname();

    public String getRealName();

    public void setNick(String nick);

    public String getNick();

    /**
     * Quit and disconnect from the server
     * 
     * @param message
     */
    public void quit(String message);

    /**
     * Quit and disconnect from the server using a default message.
     */
    public void quit();

    /**
     * Join a channel
     * 
     * @param channelName
     * @return
     */
    public Channel join(String channelName);

    /**
     * Send a message to a user
     * 
     * @param nick
     * @param message
     */
    public void sendMessage(User user, String message);

    /**
     * Get the connection used by the client object
     * 
     * @return
     */
    public Connection getConnection();
}
