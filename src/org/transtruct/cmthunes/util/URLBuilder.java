package org.transtruct.cmthunes.util;

import java.io.*;
import java.net.*;
import java.util.*;

public class URLBuilder {
    private String protocol;
    private String userInfo;
    private String host;
    private int port;
    private String path;
    private String query;
    private String ref;
    private HashMap<String,String> queryItems;

    public URLBuilder(URL url) {
        /* Extract URL parts */
        this.protocol = url.getProtocol();
        this.userInfo = url.getUserInfo();
        this.host = url.getHost();
        this.port = url.getPort();
        this.path = url.getPath();
        this.query = url.getQuery();
        this.ref = url.getRef();

        this.queryItems = new HashMap<String, String>();

        if(this.query != null && !this.query.equals("")) {
            this.extractQueryItems(this.query);
        }
    }

    public URLBuilder(String url) throws MalformedURLException {
        this(new URL(url));
    }

    private String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF8");
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("Runtime does not support UTF8 encoding");
        }
    }
    
    private String decode(String s) {
        try {
            return URLDecoder.decode(s, "UTF8");
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("Runtime does not support UTF8 encoding");
        }
    }
    
    public String getQueryParameter(String key) {
        return this.queryItems.get(key);
    }
    
    private void extractQueryItems(String query) {
        String[] items = query.split("&");

        this.queryItems.clear();
        
        for(String item : items) {
            String[] split = item.split("=", 2);
            
            if(split.length == 2) {
                String key = this.decode(split[0]);
                String value = this.decode(split[1]);
                
                this.queryItems.put(key, value);
            }
        }
    }

    public void setParameter(String key, String value) {
        this.queryItems.put(key, value);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append(this.protocol);
        buffer.append("://");

        if(this.userInfo != null && !this.userInfo.equals("")) {
            buffer.append(this.userInfo);
            buffer.append("@");
        }

        buffer.append(this.host);
        
        if(this.port != -1) {
            buffer.append(":");
            buffer.append(this.port);
        }

        buffer.append(this.path);

        if(this.queryItems.size() > 0) {
            int i = 0;

            buffer.append("?");

            for(String key : this.queryItems.keySet()) {
                String value = this.queryItems.get(key);

                buffer.append(this.encode(key));
                buffer.append("=");
                buffer.append(this.encode(value));

                if(++i < this.queryItems.size()) {
                    buffer.append("&");
                }
            }
        }

        if(this.ref != null && !this.ref.equals("")) {
            buffer.append("#");
            buffer.append(this.ref);
        }

        return buffer.toString();
    }
}
