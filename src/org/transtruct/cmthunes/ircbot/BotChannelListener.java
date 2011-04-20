package org.transtruct.cmthunes.ircbot;

import java.util.*;

import org.transtruct.cmthunes.irc.*;

public class BotChannelListener implements IRCChannelListener {
    private GroupHug grouphug;
    private TextsFromLastNight tfln;

    public BotChannelListener() {
        this.grouphug = new GroupHug();
        this.tfln = new TextsFromLastNight();
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

    @Override
    public void onPrivateMessage(IRCChannel channel, String message, IRCUser from) {
        String myNick = channel.getClient().getUser().getNick();
        message = message.trim();

        if(message.matches("\\.quit") && from.getNick().equals("c2nes")) {
            channel.getClient().quit("Leaving");

        } else if(message.matches("\\.gh")) {
            String confession = this.grouphug.getConfession();
            String[] parts = blockFormat(confession, 100, 10);
            channel.writeMultiple(parts);

        } else if(message.matches("\\.tfln")) {
            String text = this.tfln.getText();
            String[] parts = blockFormat(text, 300, 10);
            channel.writeMultiple(parts);

        } else if(message.startsWith("\\.m")) {
            String reply = this.doMath(message.replaceFirst(".m", "").trim());
            channel.write(String.format("%s: %s", from.getNick(), reply));

        } else if(message.matches("\\.w\\s+.+")) {
            try {
                String location = message.substring(2).trim();
                location = Weather.getAirport(location);
                String weather = Weather.getWeather(location);
                channel.write(weather);
            } catch(Exception e) {
                channel.write(e.getMessage());
                e.printStackTrace();
            }

        } else if(message.matches(String.format("^%s\\b.*", myNick))) {
            String request = message.replaceFirst(myNick + "[:, ]+", "").trim();

            if(request.length() == 0) {
                channel.write(from.getNick() + ": Yes?");

            } else if(request.toLowerCase().matches("hello|hi")) {
            	channel.write(from.getNick() + ": Hello");

            } else {
                channel.write(from.getNick() + ": That's not what your mother said");

            }
        }
    }

    private String doMath(String expression) {
        try {
            String outFormat = "float";
            if(expression.contains("as")) {
                String[] parts = expression.split("as");
                expression = parts[0].trim();
                outFormat = parts[1].trim().toLowerCase();
            }

            double result = Calc.evaluateExpression(expression);
            String sResult = null;

            if(result == 42 && outFormat.equals("float")) {
                sResult = "The meaning of life";
            } else if(outFormat.equals("float")) {
                sResult = String.format("%.10f", result);
                sResult = sResult.replaceFirst("0*$", "");
                sResult = sResult.replaceFirst("\\.$", "");
            } else if(outFormat.equals("hex")) {
                sResult = "0x" + Integer.toString((int) result, 16);
            } else if(outFormat.equals("bin")) {
                sResult = "0b" + Integer.toString((int) result, 2);
            } else {
                sResult = "Invalid conversion specifier";
            }

            return sResult;
        } catch(Exception e) {
            return e.getMessage();
        }
    }

    private static String[] blockFormat(String text, int width, int play) {
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

}
