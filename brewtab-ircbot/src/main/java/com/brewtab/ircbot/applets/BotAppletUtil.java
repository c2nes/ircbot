package com.brewtab.ircbot.applets;

import java.util.ArrayList;

public class BotAppletUtil {
    public static String[] blockFormat(String text, int width, int play) {
        ArrayList<String> messages = new ArrayList<String>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            line = line.trim();

            int i = 0;
            int length = 0;

            while (line.length() - i > width) {
                length = width - play;
                while (length < width) {
                    if (line.charAt(i + length) == ' ') {
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
