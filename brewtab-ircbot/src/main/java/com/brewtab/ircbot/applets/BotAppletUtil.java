/*
 * Copyright (c) 2013 Christopher Thunes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
