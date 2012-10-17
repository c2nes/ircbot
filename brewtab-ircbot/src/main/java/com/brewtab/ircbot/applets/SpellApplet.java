package com.brewtab.ircbot.applets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brewtab.irc.IRCChannel;
import com.brewtab.irc.IRCUser;

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
    public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
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
