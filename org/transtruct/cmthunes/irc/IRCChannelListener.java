package org.transtruct.cmthunes.irc;

public interface IRCChannelListener {
    public void onJoin(IRCChannel channel, IRCUser user);

    public void onPart(IRCChannel channel, IRCUser user);

    public void onPrivateMessage(IRCChannel channel, String message, IRCUser from);
}
