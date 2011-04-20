package org.transtruct.cmthunes.ircbot;

import java.io.*;
import java.net.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class Weather {
    public static String getAirport(String location) throws Exception {
        try {
            URL url = new URL("http://api.wunderground.com/auto/wui/geo/GeoLookupXML/index.xml?query="
                    + URLEncoder.encode(location, "UTF-8"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(connection.getInputStream());

            NodeList airports = document.getElementsByTagName("airport");
            if(airports.getLength() == 0) {
                throw new Exception("No nearby airports found");
            }

            NodeList stations = ((Element) airports.item(0)).getElementsByTagName("station");
            String airportCode = "";
            int stationNumber = 0;

            while(airportCode.matches("[A-Z]+") == false && stationNumber < stations.getLength()) {
                NodeList icaoList = ((Element) stations.item(stationNumber)).getElementsByTagName("icao");
                if(icaoList.getLength() == 0) {
                    throw new Exception("No stations found");
                }

                Text airportText = (Text) ((Element) icaoList.item(0)).getFirstChild();
                airportCode = airportText.getData();
                stationNumber++;
            }

            if(airportCode.matches("[A-Z]+")) {
                return airportCode;
            } else {
                throw new Exception("No stations found");
            }
        } catch(MalformedURLException e) {
            throw new Exception("Invalid location");
        } catch(IOException e) {
            throw new Exception("Could not connect to weather service");
        } catch(ParserConfigurationException e) {
            throw new Exception("Parser exception");
        } catch(SAXException e) {
            throw new Exception("Parser exception");
        }
    }

    public static String getWeather(String airport) throws Exception {
        try {
            URL url = new URL("http://api.wunderground.com/auto/wui/geo/WXCurrentObXML/index.xml?query="
                    + URLEncoder.encode(airport, "UTF-8"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(connection.getInputStream());

            NodeList weatherList = document.getElementsByTagName("weather");
            NodeList tempList = document.getElementsByTagName("temperature_string");

            if(weatherList.getLength() != 0 && tempList.getLength() != 0) {
                Text weather = (Text) ((Element) weatherList.item(0)).getFirstChild();
                Text temp = (Text) ((Element) tempList.item(0)).getFirstChild();

                return String.format("(%s) %s %s", airport, weather.getData(), temp.getData());
            } else {
                throw new Exception("Invalid weather data");
            }
        } catch(MalformedURLException e) {
            throw new Exception("Invalid location");
        } catch(IOException e) {
            throw new Exception("Could not connect to weather service");
        } catch(ParserConfigurationException e) {
            throw new Exception("Parser exception");
        } catch(SAXException e) {
            throw new Exception("Parser exception");
        }
    }
}
