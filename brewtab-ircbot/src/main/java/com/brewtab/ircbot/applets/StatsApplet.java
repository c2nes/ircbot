package com.brewtab.ircbot.applets;

import java.text.SimpleDateFormat;
import java.util.List;

import com.brewtab.irc.IRCChannel;
import com.brewtab.irc.IRCUser;
import com.brewtab.irclog.IRCLogEvent;
import com.brewtab.irclog.IRCLogger;
import com.brewtab.irclog.IRCStatistics;

public class StatsApplet implements BotApplet {
    private IRCStatistics stats;
    private SimpleDateFormat dateFormat;

    public StatsApplet(IRCLogger logger) {
        this.stats = new IRCStatistics(logger);
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
    }

    @Override
    public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
        if (command.equals("last") && args.length > 0) {
            String nick = args[0];
            IRCLogEvent event = this.stats.getLastEvent(channel.getName(), nick);
            String formattedDate;

            if (event == null) {
                channel.write(String.format("Never seen %s", nick));
            } else {
                formattedDate = this.dateFormat.format(event.getDate());

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
        } else if (command.equals("bored")) {
            final String[] boredCommands = { ".bored", ".gh", ".grouphug", ".tfln", ".texts", ".bash", ".tumblr", ".a",
                    ".alpha" };
            String nick = from.getNick();
            List<IRCLogEvent> messages;
            int count = 0;

            if (args.length > 0) {
                nick = args[0];
            }

            messages = stats.findMessages(channel.getName(), nick, ".", IRCStatistics.SEARCH_STARTSWITH);

            for (IRCLogEvent message : messages) {
                String data = message.getData();

                for (String boredCommand : boredCommands) {
                    if (data.startsWith(boredCommand)) {
                        count++;
                        break;
                    }
                }
            }

            channel.write(String.format("%s is %d many bored", nick, count));
        } else if (command.equals("tired")) {
            String nick = from.getNick();
            List<IRCLogEvent> messages;

            if (args.length > 0) {
                nick = args[0];
            }

            messages = stats.findMessages(channel.getName(), nick, "*yawn*", IRCStatistics.SEARCH_CONTAINS);

            channel.write(String.format("%s is %d many tired", nick, messages.size()));
        }
    }
}
