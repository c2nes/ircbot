package com.brewtab.ircbot.applets;

import java.util.List;

import com.brewtab.irc.IRCChannel;
import com.brewtab.irc.IRCUser;
import com.brewtab.ircbot.util.SQLProperties;
import com.brewtab.weather.Location;
import com.brewtab.weather.PersonalWeatherStation;
import com.brewtab.weather.WeatherCondition;
import com.brewtab.weather.WeatherException;
import com.brewtab.weather.WeatherService;

public class WeatherApplet implements BotApplet {
    private WeatherService weather;
    private SQLProperties properties;

    public WeatherApplet(SQLProperties properties) {
        this.weather = new WeatherService();
        this.properties = properties;
    }

    private String buildKey(IRCChannel channel, IRCUser from) {
        return "weather:" + channel.getName() + ":" + from.getNick();
    }

    @Override
    public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
        // Load arguments from properties if we aren't given any
        if (args.length == 0) {
            String[] storedArgs = properties.<String[]> get(buildKey(channel, from));

            if (storedArgs != null) {
                args = storedArgs;
            }
        }

        try {
            boolean useAirport = true;
            StringBuilder query = new StringBuilder();

            for (String arg : args) {
                if (arg.equals("-l")) {
                    useAirport = false;
                } else {
                    query.append(arg + " ");
                }
            }

            List<Location> locations = this.weather.searchLocation(query.toString().trim(), useAirport, !useAirport);

            if (locations.size() == 0) {
                channel.write("No stations found");
            } else {
                Location location = locations.get(0);

                if (!useAirport) {
                    for (Location pws_l : locations) {
                        PersonalWeatherStation pws = (PersonalWeatherStation) pws_l;
                        if (pws.getDistance() < ((PersonalWeatherStation) location).getDistance()) {
                            location = pws;
                        }
                    }
                }

                WeatherCondition condition = this.weather.getCondition(location);
                String response = null;
                String locString = location.getCity();

                if (location instanceof PersonalWeatherStation) {
                    String neighborhood = ((PersonalWeatherStation) location).getNeighborhood();
                    if (neighborhood != null && !neighborhood.equals("")) {
                        locString = neighborhood;
                    }
                }

                response = String.format("%s: %dC (%dF) %s", locString, (int) condition.getTempC(),
                    (int) condition.getTempF(), condition.getConditionString());
                channel.write(response);

                properties.set(buildKey(channel, from), args);
            }
        } catch (WeatherException e) {
            channel.write(e.getMessage());
        }
    }
}
