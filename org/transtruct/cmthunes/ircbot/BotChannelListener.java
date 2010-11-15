package org.transtruct.cmthunes.ircbot;

import java.util.*;

import org.transtruct.cmthunes.irc.*;

public class BotChannelListener implements IRCChannelListener {
    private GroupHug grouphug;
    
    public BotChannelListener() {
        this.grouphug = new GroupHug();
    }
    
    @Override
    public void onJoin(IRCChannel channel, IRCUser user) {
        if(user.getNick().equals(channel.getClient().getUser().getNick()) == false) {
            channel.write("Hello " + user.getNick());
        }
    }

    @Override
    public void onPart(IRCChannel channel, IRCUser user) {
        channel.write("We will miss them dearly");
    }
    
    public String doMath(String expression) {
        try {
            double result = Calc.evaluateExpression(expression);
            String sResult = String.format("%.10f", result);
            sResult = sResult.replaceFirst("0*$", "");
            sResult = sResult.replaceFirst("\\.$", "");
            
            if(sResult.equals("42")) {
                sResult = "The meaning of life";
            }
            
            return sResult;
        } catch(Exception e) {
            return e.getMessage();
        } 
    }
    
    public static String[] blockFormat(String text, int width, int play) {
        ArrayList<String> messages = new ArrayList<String>(); 
        String[] lines = text.split("\n");
        
        for(String line : lines) {
            line = line.trim();
            
            int i = 0;
            int length = 0;
            
            while(line.length() - i > width) {
                length = width - play;
                while(length < width) {
                    if(line.charAt(i + length) == ' ') {
                        break;
                    }
                    
                    length++;
                }
                                        
                String part = line.substring(i, i + length);
                i += length;
                
                messages.add(part.trim());
            }
            messages.add(line.substring(i).trim());
        }
        
        String[] messagesArray = new String[messages.size()];
        messagesArray = messages.toArray(messagesArray);
        return messagesArray;
    }

    @Override
    public void onPrivateMessage(IRCChannel channel, String message, IRCUser from) {
        String myNick = channel.getClient().getUser().getNick();

        if(message.equals("fuck off " + myNick) && from.getNick().equals("c2nes")) {
            channel.write("Fine.");
            channel.part("Fuck you");
            channel.getClient().quit("Leaving");
            
        } else if(message.toLowerCase().matches(".*\bhi\b.*") && message.contains(myNick)) {
            channel.write("Hello " + from.getNick());
            
        } else if(message.toLowerCase().contains("fuck you") && message.contains(myNick)) { 
            channel.write(String.format("%s: No, fuck you. You'll get more pussy", from.getNick()));
            
        } else if(message.equals("names")) {
            String[] names = channel.getNames();
            StringBuffer reply = new StringBuffer();
            for(String name : names) {
                reply.append(name).append(" ");
            }
            channel.write(reply.toString().trim());
            
        } else if(message.startsWith(".gh")) {
            try {
                String confession = this.grouphug.getConfession();
                String[] parts = blockFormat(confession, 100, 10);
                for(String part : parts) {
                    channel.write(part);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            
        } else if(message.startsWith(".m")) {
            String reply = this.doMath(message.replaceFirst(".m", "").trim());
            channel.write(String.format("%s: %s", from.getNick(), reply));

        } else if(message.matches(myNick + "[,:].*")) {
            String request = message.replaceFirst(myNick + "[:,]", "").trim();

            if(request.length() == 0) {
                channel.write("wtf do you want?");
                
            } else if(request.toLowerCase().startsWith("tell")) {
                channel.write(String.format("%s: Don't be a lazy bitch; you do it", from.getNick()));
                
            } else if(request.matches("([0-9.()*+-^/% ]|sin|cos|tan|sqrt|log|log2|log10)+")) {
                request = request.trim();
                String reply = this.doMath(request);
                channel.write(String.format("%s: %s", from.getNick(), reply));
                
            } else {
                channel.write("wtf does \"" + request + "\" mean?");
            }
        }
    }
}
