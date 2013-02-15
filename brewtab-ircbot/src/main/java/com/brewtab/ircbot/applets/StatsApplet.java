package com.brewtab.ircbot.applets;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;
import com.brewtab.irclog.IRCLogEvent;
import com.brewtab.irclog.IRCLogger;
import com.google.common.base.Throwables;

public class StatsApplet implements BotApplet {
    private IRCLogger logger;
    private SimpleDateFormat dateFormat;

    public StatsApplet(IRCLogger logger) {
        this.logger = logger;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
    }

    private PreparedStatement lastEventStmt(String channel, String nick) {
        String sql = "SELECT * FROM events WHERE extra = ? AND nick = ? ORDER BY event_time DESC LIMIT 1";
        PreparedStatement stmt = logger.prepareQuery(sql);

        try {
            stmt.setString(1, channel);
            stmt.setString(2, nick);
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }

        return stmt;
    }

    private PreparedStatement lastMessageStmt(String channel, String nick) {
        String sql = "SELECT * FROM messages WHERE channel = ? AND nick = ? ORDER BY msg_time DESC LIMIT 1";
        PreparedStatement stmt = logger.prepareQuery(sql);

        try {
            stmt.setString(1, channel);
            stmt.setString(2, nick);
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }

        return stmt;
    }

    private void last(Channel channel, String nick) {
        IRCLogEvent event = logger.queryMessage(lastMessageStmt(channel.getName(), nick));

        if (event == null) {
            logger.queryEvent(lastEventStmt(channel.getName(), nick));
        }

        if (event == null) {
            channel.write(String.format("Never seen %s", nick));
        } else {
            String formattedDate = dateFormat.format(event.getDate());

            if (event.getEventType() == IRCLogEvent.JOIN_EVENT) {
                channel.write(String.format("%s joined at %s", nick, formattedDate));
            } else if (event.getEventType() == IRCLogEvent.PART_EVENT) {
                channel.write(String.format("%s parted at %s", nick, formattedDate));
            } else if (event.getEventType() == IRCLogEvent.QUIT_EVENT) {
                channel.write(String.format("%s quit at %s", nick, formattedDate));
            } else if (event.getEventType() == IRCLogEvent.MESSAGE_EVENT) {
                channel.write(String.format("%s said \"%s\" at %s", nick, event.getData(), formattedDate));
            }
        }
    }

    private void bored(Channel channel, String nick) {
        final String[] boredCommands = { ".bored", ".gh", ".grouphug", ".tfln", ".texts", ".bash", ".tumblr", ".a", ".alpha" };
        int count = 0;

        String sql = "SELECT COUNT(1) FROM messages WHERE channel = ? AND nick = ? AND message LIKE ?";
        PreparedStatement stmt = logger.prepareQuery(sql);

        try {
            stmt.setString(1, channel.getName());
            stmt.setString(2, nick);

            for (String cmd : boredCommands) {
                stmt.setString(3, cmd + "%");
                count += logger.countMessages(stmt);
            }
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }

        channel.write(String.format("%s is %d many bored", nick, count));
    }

    private void tired(Channel channel, String nick) {
        String sql = "SELECT COUNT(1) FROM messages WHERE channel = ? AND nick = ? AND message LIKE '%*yawn*%'";
        PreparedStatement stmt = logger.prepareQuery(sql);

        try {
            stmt.setString(1, channel.getName());
            stmt.setString(2, nick);
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }

        int count = logger.countMessages(stmt);
        channel.write(String.format("%s is %d many tired", nick, count));
    }

    @Override
    public void run(Channel channel, User from, String command, String[] args, String unparsed) {
        if (command.equals("last") && args.length > 0) {
            String nick = args[0];

            last(channel, nick);
        } else if (command.equals("bored")) {
            String nick = from.getNick();

            if (args.length > 0) {
                nick = args[0];
            }

            bored(channel, nick);
        } else if (command.equals("tired")) {
            String nick = from.getNick();

            if (args.length > 0) {
                nick = args[0];
            }

            tired(channel, nick);
        }
    }
}
