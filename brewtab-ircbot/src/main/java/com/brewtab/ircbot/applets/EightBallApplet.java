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
