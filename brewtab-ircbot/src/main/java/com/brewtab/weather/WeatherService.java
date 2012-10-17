package com.brewtab.weather;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WeatherService {
    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private DocumentBuilderFactory domBuilderFactory;
    private XPath xpath;

    private XPathExpression cityExpr;
    private XPathExpression stateExpr;
    private XPathExpression countryExpr;
    private XPathExpression icaoExpr;
    private XPathExpression idExpr;
    private XPathExpression neighborhoodExpr;
    private XPathExpression distanceExpr;

    public WeatherService() {
        this.domBuilderFactory = DocumentBuilderFactory.newInstance();
        this.xpath = XPathFactory.newInstance().newXPath();

        try {
            this.cityExpr = this.xpath.compile("city/text()");
            this.stateExpr = this.xpath.compile("state/text()");
            this.countryExpr = this.xpath.compile("country/text()");
            this.icaoExpr = this.xpath.compile("icao/text()");
            this.idExpr = this.xpath.compile("id/text()");
            this.neighborhoodExpr = this.xpath.compile("neighborhood/text()");
            this.distanceExpr = this.xpath.compile("distance_km/text()");
        } catch (XPathExpressionException e) {
            log.error("caught exception compiling XPath queries", e);
            throw new RuntimeException(e);
        }
    }

    private String getXPathString(XPathExpression expr, Node node) {
        try {
            return (String) expr.evaluate(node, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            log.error("error while evaluating XPath query", e);
            return "";
        }
    }

    private Airport buildAirport(Node node) {
        Airport airport = new Airport();

        airport.setCity(this.getXPathString(this.cityExpr, node));
        airport.setState(this.getXPathString(this.stateExpr, node));
        airport.setCountry(this.getXPathString(this.countryExpr, node));
        airport.setIcao(this.getXPathString(this.icaoExpr, node));

        return airport;
    }

    private PersonalWeatherStation buildPersonalWeatherStation(Node node) {
        PersonalWeatherStation pws = new PersonalWeatherStation();

        pws.setCity(this.getXPathString(this.cityExpr, node));
        pws.setState(this.getXPathString(this.stateExpr, node));
        pws.setCountry(this.getXPathString(this.countryExpr, node));
        pws.setNeighborhood(this.getXPathString(this.neighborhoodExpr, node));
        pws.setId(this.getXPathString(this.idExpr, node));
        pws.setDistance(Integer.valueOf(this.getXPathString(this.distanceExpr, node)));

        return pws;
    }

    public List<Location> searchLocation(String query) throws WeatherException {
        return searchLocation(query, true, true);
    }

    public List<Location> searchLocation(String query, boolean includeAirports, boolean includeStations)
            throws WeatherException {
        String baseURL = "http://api.wunderground.com/auto/wui/geo/GeoLookupXML/index.xml?query=";
        try {
            URL url = new URL(baseURL + URLEncoder.encode(query, "UTF-8"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            DocumentBuilder builder = this.domBuilderFactory.newDocumentBuilder();
            Document document = null;

            connection.setReadTimeout(5000);
            document = builder.parse(connection.getInputStream());

            ArrayList<Location> locations = new ArrayList<Location>();

            /* Add airports */
            if (includeAirports) {
                NodeList airports = (NodeList) this.xpath.evaluate("//airport/station", document,
                        XPathConstants.NODESET);
                for (int i = 0; i < airports.getLength(); i++) {
                    locations.add(buildAirport(airports.item(i)));
                }
            }

            /* Add personal weather stations */
            if (includeStations) {
                NodeList pws = (NodeList) this.xpath.evaluate("//pws/station", document, XPathConstants.NODESET);
                for (int i = 0; i < pws.getLength(); i++) {
                    locations.add(buildPersonalWeatherStation(pws.item(i)));
                }
            }

            return locations;
        } catch (SocketTimeoutException e) {
            throw new WeatherException("Timed out while retrieving location data");
        } catch (IOException e) {
            throw new WeatherException("Error retrieving location data");
        } catch (SAXException e) {
            throw new WeatherException("Error parsing response");
        } catch (Exception e) {
            log.error("unexpected exception while searching for locations", e);
            throw new WeatherException("Unexpected exception");
        }
    }

    private String getObservationParameter(Node dom, String parameter) throws XPathExpressionException {
        String query = String.format("/current_observation/%s/text()", parameter);
        return (String) this.xpath.evaluate(query, dom, XPathConstants.STRING);
    }

    public WeatherCondition getCondition(Location location) throws WeatherException {
        try {
            URL url = location.getQueryURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            DocumentBuilder builder = this.domBuilderFactory.newDocumentBuilder();
            Document document = null;

            connection.setReadTimeout(5000);
            document = builder.parse(connection.getInputStream());

            WeatherCondition condition = new WeatherCondition();

            String temp_c = this.getObservationParameter(document, "temp_c");
            String cond_s = this.getObservationParameter(document, "weather");
            String wind_dir = this.getObservationParameter(document, "wind_dir");
            String wind_mph = this.getObservationParameter(document, "wind_mph");

            condition.setTempC(Double.valueOf(temp_c));
            condition.setConditionString(cond_s.trim());
            condition.setWindDir(wind_dir);
            condition.setWindSpeed(Double.valueOf(wind_mph));

            return condition;
        } catch (SocketTimeoutException e) {
            throw new WeatherException("Timed out while retrieving condition data");
        } catch (IOException e) {
            throw new WeatherException("Error retrieving location data");
        } catch (SAXException e) {
            throw new WeatherException("Error parsing response");
        } catch (Exception e) {
            log.error("unexpected exception while searching for locations", e);
            throw new WeatherException("Unexpected exception");
        }
    }
}
