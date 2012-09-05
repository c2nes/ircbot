
package org.transtruct.cmthunes.ircbot.applets;

import java.util.*;
import java.text.*;

import org.transtruct.cmthunes.weather.*;
import org.transtruct.cmthunes.irc.*;
import org.transtruct.cmthunes.irclog.*;

public class StatsApplet implements BotApplet {
    private IRCLogger logger;
    private IRCStatistics stats;
    private SimpleDateFormat dateFormat;

    public StatsApplet(IRCLogger logger) {
        this.logger = logger;
        this.stats = new IRCStatistics(logger);
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
    }

    public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
        if(command.equals("last") && args.length > 0) {
            String nick = args[0];
            IRCLogEvent event = this.stats.getLastEvent(channel.getName(), nick);
            String formattedDate;

            if(event == null) {
                channel.write(String.format("Never seen %s", nick));
            } else {
                formattedDate = this.dateFormat.format(event.getDate());

                if(event.getEventType() == event.JOIN_EVENT) {
                    channel.write(String.format("%s joined at %s", nick, formattedDate));
                } else if(event.getEventType() == event.PART_EVENT) {
                    channel.write(String.format("%s parted at %s", nick, formattedDate));
                } else if(event.getEventType() == event.QUIT_EVENT) {
                    channel.write(String.format("%s quit at %s", nick, formattedDate));
                } else if(event.getEventType() == event.MESSAGE_EVENT) {
                    channel.write(String.format("%s said \"%s\" at %s", nick, event.getData(), formattedDate));
                }
            }
        } else if(command.equals("bored")) {
            final String[] boredCommands = {".bored", ".gh", ".grouphug", ".tfln",
                                            ".texts", ".bash", ".tumblr", ".a", 
                                            ".alpha"};
            String nick = from.getNick();
            List<IRCLogEvent> messages;
            int count = 0;
            
            if(args.length > 0) {
                nick = args[0];
            }

            messages = stats.findMessages(channel.getName(), nick, ".", IRCStatistics.SEARCH_STARTSWITH);
            
            for(IRCLogEvent message : messages) {
                String data = message.getData();

                for(String boredCommand : boredCommands) {
                    if(data.startsWith(boredCommand)) {
                        count++;
                        break;
                    }
                }
            }

            channel.write(String.format("%s is %d many bored", nick, count));
        } else if (command.equals("tired")) {
            String nick = from.getNick();
            List<IRCLogEvent> messages;

            if(args.length > 0) {
                nick = args[0];
            }

            messages = stats.findMessages(channel.getName(), nick, "*yawn*", IRCStatistics.SEARCH_CONTAINS);
        
            channel.write(String.format("%s is %d many tired", nick, messages.size()));    
        }
    }
}
