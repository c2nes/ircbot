package com.brewtab.ircbot.applets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;
import com.brewtab.ircbot.applets.stock.Quote;
import com.brewtab.ircbot.applets.stock.QuoteField;

public class StockApplet implements BotApplet {
    private Options options;
    private CommandLineParser parser;

    public StockApplet() {
        options = new Options();
        options.addOption("v", false, "verbose");
        parser = new BasicParser();
    }

    private List<String> getResponse(String[] args) {
        CommandLine cmdline;

        try {
            cmdline = parser.parse(options, args);
        } catch (ParseException e) {
            return Arrays.asList("Error: " + e.getMessage());
        }

        List<String> symbols = Arrays.asList(cmdline.getArgs());

        if (symbols.size() == 0) {
            return Arrays.asList("Error: at least one stock symbol must be given");
        }

        List<QuoteField> fields = Arrays.asList(
            QuoteField.SYMBOL,
            QuoteField.NAME,
            QuoteField.LAST_TRADE_PRICE_ONLY,
            QuoteField.CHANGE,
            QuoteField.PERCENT_CHANGE,

            QuoteField.MARKET_CAP,
            QuoteField.PE_RATIO,
            QuoteField.FIFTY_TWO_WEEK_HIGH,
            QuoteField.FIFTY_TWO_WEEK_LOW
            );

        List<Quote> quotes = Quote.forSymbols(symbols, fields);
        List<String> lines = new ArrayList<String>();

        if (cmdline.hasOption('v')) {
            for (Quote quote : quotes) {
                lines.add(String.format("%s: %s", quote.get(QuoteField.SYMBOL), quote.get(QuoteField.NAME)));
                lines.add(String.format("  Current: %s (%s, %s)",
                    quote.get(QuoteField.LAST_TRADE_PRICE_ONLY),
                    quote.get(QuoteField.CHANGE),
                    quote.get(QuoteField.PERCENT_CHANGE)
                    ));

                for (QuoteField field : fields.subList(5, fields.size())) {
                    lines.add(String.format("  %s %s", field.getDescription() + ":", quote.get(field)));
                }
            }
        } else {
            for (Quote quote : quotes) {
                lines.add(String.format("%s: %s (%s, %s)",
                    quote.get(QuoteField.SYMBOL),
                    quote.get(QuoteField.LAST_TRADE_PRICE_ONLY),
                    quote.get(QuoteField.CHANGE),
                    quote.get(QuoteField.PERCENT_CHANGE)
                    ));
            }
        }

        return lines;
    }

    @Override
    public void run(Channel channel, User from, String arg0, String[] args, String unparsed) {
        List<String> lines = getResponse(args);
        channel.writeMultiple(lines.toArray(new String[lines.size()]));
    }

    public static void main(String[] args) {
        for (String line : new StockApplet().getResponse(args)) {
            System.out.println(line);
        }
    }
}
