package com.brewtab.irc.client;

import com.brewtab.irc.Connection;
import com.brewtab.irc.User;

public interface Client {
    public void setUsername(String username);

    public String getUsername();

    public void setHostname(String hostname);

    public String getHostname();

    public void setRealName(String realName);

    public String getRealName();

    public void setNick(String nick);

    public String getNick();

    public void setPassword(String password);

    public String getPassword();

    /**
     * Connect to a server.
     * 
     * @param uri a URI specifying an irc: or ircs: scheme
     */
    public void connect(String uri);

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
