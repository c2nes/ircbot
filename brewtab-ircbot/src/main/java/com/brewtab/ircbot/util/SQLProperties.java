package com.brewtab.ircbot.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.base.Throwables;

/**
 * Simple key-value store backed by a SQL database
 *
 * @author Chris Thunes <cthunes@brewtab.com>
 */
public class SQLProperties {
    private Connection connection;

    public SQLProperties(Connection connection) {
        this.connection = connection;
    }

    @SuppressWarnings("unchecked")
    private <V extends Serializable> V cast(Object obj) {
        return (V) obj;
    }

    private byte[] serialize(Object obj) {
        try {
            ByteArrayOutputStream bstream = new ByteArrayOutputStream();
            ObjectOutputStream valueStream = new ObjectOutputStream(bstream);
            valueStream.writeObject(obj);
            valueStream.close();

            byte[] objBytes = bstream.toByteArray();
            bstream.close();

            return objBytes;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private Object deserialize(byte[] objBytes) {
        try {
            ByteArrayInputStream bstream = new ByteArrayInputStream(objBytes);
            ObjectInputStream valueStream = new ObjectInputStream(bstream);
            Object obj = valueStream.readObject();
            valueStream.close();
            bstream.close();

            return obj;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        } catch (ClassNotFoundException e) {
            throw Throwables.propagate(e);
        }
    }

    public <V extends Serializable> V get(String key) {
        return this.<V> get(key, null);
    }

    public <V extends Serializable> V get(String key, V defaultValue) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                "SELECT v FROM properties WHERE k = ?"
                );

            statement.setString(1, key);

            ResultSet results = statement.executeQuery();
            if (results.next()) {
                Object value = deserialize(results.getBytes(1));

                return this.<V> cast(value);
            } else {
                return defaultValue;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <V extends Serializable> void set(String key, V value) {
        try {
            byte[] valueBytes = serialize(value);

            PreparedStatement statement = connection.prepareStatement(
                "UPDATE properties SET v = ? WHERE k = ?"
                );

            statement.setBytes(1, valueBytes);
            statement.setString(2, key);

            // If no update was performed instead insert the value
            if (statement.executeUpdate() == 0) {
                statement = connection.prepareStatement(
                    "INSERT INTO properties (k, v) VALUES (?, ?)"
                    );

                statement.setString(1, key);
                statement.setBytes(2, valueBytes);
                statement.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
