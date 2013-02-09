package com.brewtab.ircbot.applets;

import java.util.Random;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;

public class EightBallApplet implements BotApplet {
    private Random random;

    public EightBallApplet() {
        this.random = new Random();
    }

    @Override
    public void run(Channel channel, User from, String command, String[] args, String unparsed) {
        final String[] answers = { "It is certain", "It is decidedly so", "Without a doubt", "Yes \u2013 definitely",
                "You may rely on it", "As I see it, yes", "Most likely", "Outlook good", "Signs point to yes", "Yes",
                "Reply hazy, try again", "Ask again later", "Better not tell you now", "Cannot predict now",
                "Concentrate and ask again", "Don't count on it", "My reply is no", "My sources say no",
                "Outlook not so good", "Very doubtful" };

        channel.write(answers[this.random.nextInt(answers.length)]);
    }
}
