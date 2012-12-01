package com.brewtab.ircbot.applets;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;
import com.brewtab.ircbot.util.SQLProperties;
import com.brewtab.ircbot.util.URLBuilder;

public class WundergroundApplet implements BotApplet {
    private static final Logger log = LoggerFactory.getLogger(WundergroundApplet.class);

    private static final String AUTOCOMPLETE_URL = "http://autocomplete.wunderground.com/aq";
    private static final String API_URL_TEMPLATE = "http://api.wunderground.com/api/%s/%s%s.xml";

    private SQLProperties properties;
    private DocumentBuilder documentBuilder;
    private XPath xpath;
    private String apikey;

    private Options options;
    private PosixParser parser;

    public WundergroundApplet(SQLProperties properties, String apikey) {
        this.properties = properties;
        this.apikey = apikey;

        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        xpath = XPathFactory.newInstance().newXPath();

        options = new Options();
        options.addOption("c", "metric", false, "use metric units");
        options.addOption("f", "imperial", false, "use imperial units (default)");
        options.addOption("n", "no-save", false, "do not save the query as default");
        parser = new PosixParser();
    }

    private URLBuilder makeUrl(String base) {
        try {
            return new URLBuilder(base);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildKey(Channel channel, User from) {
        return "wunderground:" + channel.getName() + ":" + from.getNick() + ":";
    }

    public String findLocation(Channel channel, User from, String query, boolean saveSettings) throws Exception {
        if (StringUtils.isBlank(query)) {
            String location = properties.<String> get(buildKey(channel, from) + "location");
            return location;
        }

        URLBuilder url = makeUrl(AUTOCOMPLETE_URL);
        url.setParameter("format", "xml");
        url.setParameter("query", query);

        Document doc = documentBuilder.parse(url.toString());
        String location = xpath.evaluate("/RESULTS/l[1]", doc);

        if (StringUtils.isNotBlank(location) && saveSettings) {
            properties.set(buildKey(channel, from) + "location", location);
        }

        return location;
    }

    private Document request(String feature, String location) throws SAXException, IOException {
        String url = String.format(API_URL_TEMPLATE, apikey, feature, location);
        return documentBuilder.parse(url);
    }

    private String formatTemp(String s) {
        int value = (int) Float.parseFloat(s);
        return Integer.toString(value);
    }

    @Override
    public void run(Channel channel, User from, String command, String[] args, String unparsed) {
        try {
            CommandLine commandLine = parser.parse(options, args, true);
            boolean saveSettings = !commandLine.hasOption("no-save");
            boolean useMetric = commandLine.hasOption("metric");
            boolean useImperial = commandLine.hasOption("imperial");
            String query = StringUtils.join(commandLine.getArgs(), " ");

            String locationQuery = findLocation(channel, from, query, saveSettings);

            if (StringUtils.isBlank(locationQuery)) {
                channel.write("No location found");
                return;
            }

            // Get current conditions
            Document result = request("conditions", locationQuery);
            String location = xpath.evaluate("/response/current_observation/display_location/full", result);
            String temp_f = formatTemp(xpath.evaluate("/response/current_observation/temp_f", result));
            String temp_c = formatTemp(xpath.evaluate("/response/current_observation/temp_c", result));
            String condition = xpath.evaluate("/response/current_observation/weather", result);

            // Get todays forecast
            result = request("forecast", locationQuery);
            String forecastBase = "/response/forecast/simpleforecast/forecastdays/forecastday[1]/";
            String high_f = formatTemp(xpath.evaluate(forecastBase + "high/fahrenheit", result));
            String high_c = formatTemp(xpath.evaluate(forecastBase + "high/celsius", result));
            String low_f = formatTemp(xpath.evaluate(forecastBase + "low/fahrenheit", result));
            String low_c = formatTemp(xpath.evaluate(forecastBase + "low/celsius", result));

            if (!useMetric && !useImperial) {
                useMetric = properties.<Boolean> get(buildKey(channel, from) + "metric", false);
            } else if (saveSettings) {
                properties.set(buildKey(channel, from) + "metric", useMetric);
            }

            if (useMetric) {
                channel.write(String.format("%s: %s, %sC, high of %sC, low of %sC",
                    location, condition, temp_c, high_c, low_c));
            } else {
                channel.write(String.format("%s: %s, %sF, high of %sF, low of %sF",
                    location, condition, temp_f, high_f, low_f));
            }
        } catch (Exception e) {
            log.error("Caught exception running Wunderground applet", e);
        }
    }
}
