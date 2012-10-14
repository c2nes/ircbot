package org.transtruct.cmthunes.weather;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Airport extends Location {
    private String icao;

    public Airport() {
        super();
        this.icao = "";
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public String getIcao() {
        return icao;
    }

    @Override
    public URL getQueryURL() {
        String baseURL = "http://api.wunderground.com/auto/wui/geo/WXCurrentObXML/index.xml?query=";

        try {
            return new URL(baseURL + URLEncoder.encode(this.icao, "UTF-8"));
        } catch (MalformedURLException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
