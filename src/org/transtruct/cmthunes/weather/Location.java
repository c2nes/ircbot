package org.transtruct.cmthunes.weather;

import java.net.URL;

import org.transtruct.cmthunes.util.State;

public abstract class Location {
    private String city;
    private State state;
    private String country;

    public Location() {
        this.city = "";
        this.state = null;
        this.country = "";
    }
    
    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setState(String state) {
        this.state = State.fromString(state);
    }

    public State getState() {
        return state;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public abstract URL getQueryURL();
}
