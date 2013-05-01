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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;

public class SpellApplet implements BotApplet {
    private static final Logger log = LoggerFactory.getLogger(SpellApplet.class);

    Process ispellProcess;
    PrintWriter ispellOut;
    BufferedReader ispellIn;

    public SpellApplet() {
        try {
            this.ispellProcess = (new ProcessBuilder("ispell", "-a")).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.ispellIn = new BufferedReader(new InputStreamReader(this.ispellProcess.getInputStream()));
        this.ispellOut = new PrintWriter(this.ispellProcess.getOutputStream(), true);

        /* Discard version line */
        try {
            this.ispellIn.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(Channel channel, User from, String command, String[] args, String unparsed) {
        String line;
        int misspelledWordsCount = 0;

        if (unparsed.trim().equals("")) {
            return;
        }

        /* Write out string */
        this.ispellOut.println("^" + unparsed.trim());

        /*
         * Read each returned line until a new line. One word is spell checked
         * per line
         */
        while (true) {
            try {
                line = this.ispellIn.readLine().trim();
            } catch (IOException e) {
                log.error("error while reading from ispell", e);
                return;
            }

            if (line.equals("")) {
                break;
            } else if (line.startsWith("&") || line.startsWith("?")) {
                String original = line.split(" ", 3)[1];
                String guesses = line.split(":")[1].trim();

                channel.write(String.format("%s: %s", original, guesses));
                misspelledWordsCount++;
            } else if (line.startsWith("#")) {
                String original = line.split(" ", 3)[1];

                channel.write(String.format("%s: No suggestsions", original));
                misspelledWordsCount++;
            }
        }

        if (misspelledWordsCount == 0) {
            channel.write("No misspelled words");
        }
    }
}
