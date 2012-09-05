
package org.transtruct.cmthunes.ircbot.applets;

import java.util.*;

import org.transtruct.cmthunes.weather.*;
import org.transtruct.cmthunes.irc.*;

public class WeatherApplet implements BotApplet {
    private WeatherService weather;

    public WeatherApplet() {
        this.weather = new WeatherService();
    }

    public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
        try {
            boolean useAirport = true;
            StringBuilder query = new StringBuilder();
            
            for(String arg : args) {
                if(arg.equals("-l")) {
                    useAirport = false;
                } else {
                    query.append(arg + " ");
                }
            }
            
            List<Location> locations = this.weather.searchLocation(query.toString().trim(), useAirport, !useAirport);
            
            if(locations.size() == 0) {
                channel.write("No stations found");
            } else {
                Location location = locations.get(0);
                
                if(!useAirport) {
                    for(Location pws_l : locations) {
                        PersonalWeatherStation pws = (PersonalWeatherStation) pws_l;
                        if(pws.getDistance() < ((PersonalWeatherStation) location).getDistance()) {
                            location = pws;
                        }
                    }
                }
                
                WeatherCondition condition = this.weather.getCondition(location);
                String response = null;
                String locString = location.getCity();
                
                if(location instanceof PersonalWeatherStation) {
                    String neighborhood = ((PersonalWeatherStation) location).getNeighborhood();
                    if(neighborhood != null && !neighborhood.equals("")) {
                        locString = neighborhood;
                    }
                }
                
                response = String.format("%s: %dC (%dF) %s", locString, (int) condition.getTempC(),
                                         (int) condition.getTempF(), condition.getConditionString());
                channel.write(response);
            }
        } catch(WeatherException e) {
            channel.write(e.getMessage());
        }
    }
}
