package org.transtruct.cmthunes.weather;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class PersonalWeatherStation extends Location {
    private String id;
    private String neighborhood;
    private int distance;

    public PersonalWeatherStation() {
        super();
        
        this.id = "";
        this.neighborhood = "";
        this.setDistance(0);
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDistance() {
        return distance;
    }
    
    @Override
    public URL getQueryURL() {
        String baseURL = "http://api.wunderground.com/weatherstation/WXCurrentObXML.asp?ID=";

        try {
            return new URL(baseURL + URLEncoder.encode(this.id, "UTF-8"));
        } catch (MalformedURLException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
