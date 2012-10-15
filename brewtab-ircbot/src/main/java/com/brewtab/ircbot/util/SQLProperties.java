package com.brewtab.ircbot.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLProperties<V extends Serializable> {
    private Connection connection;

    public SQLProperties(Connection connection) {
        this.connection = connection;
        try {
            this.connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS properties (k VARCHAR PRIMARY KEY, v BLOB)"
                ).execute();

            this.connection.prepareStatement(
                "CREATE INDEX IF NOT EXISTS idxprops ON properties(k)"
                ).execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public V get(String key) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                "SELECT v FROM properties WHERE k = ?"
                );

            statement.setString(1, key);

            ResultSet results = statement.executeQuery();
            if (results.next()) {
                Blob valueBlob = results.getBlob(1);
                InputStream stream = valueBlob.getBinaryStream();
                ObjectInputStream objectStream = new ObjectInputStream(stream);

                return (V) objectStream.readObject();
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(String key, V value) {
        try {
            Blob blob = connection.createBlob();

            ObjectOutputStream valueStream = new ObjectOutputStream(blob.setBinaryStream(1));
            valueStream.writeObject(value);
            valueStream.close();

            PreparedStatement statement = connection.prepareStatement(
                "UPDATE properties SET v = ? WHERE k = ? LIMIT 1"
                );

            statement.setBlob(1, blob);
            statement.setString(2, key);

            // If no update was performed instead insert the value
            if (statement.executeUpdate() == 0) {
                statement = connection.prepareStatement(
                    "INSERT INTO properties (k, v) VALUES (?, ?)"
                    );

                statement.setString(1, key);
                statement.setBlob(2, blob);
                statement.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
