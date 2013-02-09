package com.brewtab.irclog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;
import com.brewtab.irc.client.ChannelListener;

public class IRCLogger implements ChannelListener {
    private static final Logger log = LoggerFactory.getLogger(IRCLogger.class);

    private Connection db;
    private PreparedStatement eventStatement;
    private PreparedStatement messageStatement;

    public IRCLogger(Connection dbConnection) throws SQLException {
        this.db = dbConnection;

        this.initTables();
        this.openSession();

        this.eventStatement = this.db
                .prepareStatement("INSERT INTO event_log (event_time, type, user, extra) VALUES (?, ?, ?, ?)");
        this.messageStatement = this.db
                .prepareStatement("INSERT INTO message_log (msg_time, channel, from_nick, message) VALUES (?, ?, ?, ?)");
    }

    private void initTables() throws SQLException {
        Statement statement = db.createStatement();

        statement.addBatch("CREATE TABLE IF NOT EXISTS sessions"
                + " (id INT AUTO_INCREMENT PRIMARY KEY, start_time"
                + " TIMESTAMP, end_time TIMESTAMP, active BOOLEAN);");

        statement.addBatch("CREATE TABLE IF NOT EXISTS event_log"
                + " (id INT AUTO_INCREMENT PRIMARY KEY, event_time TIMESTAMP,"
                + " type CHAR(8), user VARCHAR(32), extra VARCHAR(64));");

        statement.addBatch("CREATE TABLE IF NOT EXISTS message_log"
                + " (id INT AUTO_INCREMENT PRIMARY KEY, msg_time TIMESTAMP,"
                + " channel VARCHAR(32), from_nick VARCHAR(32), message VARCHAR);");

        statement.addBatch("CREATE INDEX IF NOT EXISTS idxmessages ON message_log(from_nick, channel)");

        statement.executeBatch();
    }

    public PreparedStatement prepareQuery(String query) throws SQLException {
        return this.db.prepareStatement(query);
    }

    private void openSession(Timestamp timestamp) throws SQLException {
        PreparedStatement statement = this.db
                .prepareStatement("INSERT INTO sessions (start_time, active) VALUES (?, TRUE)");
        statement.setTimestamp(1, timestamp);
        statement.execute();
    }

    private void openSession() throws SQLException {
        this.openSession(this.getCurrentTimestamp());
    }

    private void closeSession(Timestamp timestamp) throws SQLException {
        PreparedStatement statement = this.db
                .prepareStatement("UPDATE sessions SET active=FALSE, end_time=? WHERE active=TRUE");
        statement.setTimestamp(1, timestamp);
    }

    @Override
    protected void finalize() throws Exception {
        if (!this.db.isClosed()) {
            this.closeSession(this.getCurrentTimestamp());
            this.db.close();
        }
    }

    public void addFromIrssiLog(String channel, FileReader log) throws IOException, ParseException, SQLException {
        BufferedReader reader = new BufferedReader(log);
        SimpleDateFormat dayChangedFormat = new SimpleDateFormat("'--- Day changed' EEE MMM dd yyyy");
        SimpleDateFormat logOpenedFormat = new SimpleDateFormat("'--- Log opened' EEE MMM dd HH:mm:ss yyyy");
        SimpleDateFormat logClosedFormat = new SimpleDateFormat("'--- Log closed' EEE MMM dd HH:mm:ss yyyy");
        Calendar timestamp = Calendar.getInstance();

        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith("--- Day changed")) {
                timestamp.setTime(dayChangedFormat.parse(line));

            } else if (line.startsWith("--- Log opened")) {
                timestamp.setTime(logOpenedFormat.parse(line));
                this.openSession(new Timestamp(timestamp.getTimeInMillis()));

            } else if (line.startsWith("--- Log closed")) {
                timestamp.setTime(logClosedFormat.parse(line));
                this.closeSession(new Timestamp(timestamp.getTimeInMillis()));

            } else if (line.matches("^\\d\\d:\\d\\d .*")) {
                int hour = Integer.valueOf(line.substring(0, 2));
                int minute = Integer.valueOf(line.substring(3, 5));
                String rest = line.substring(6).trim();
                String nick = "";
                String message = "";

                timestamp.set(Calendar.HOUR_OF_DAY, hour);
                timestamp.set(Calendar.MINUTE, minute);

                Timestamp ts = new Timestamp(timestamp.getTimeInMillis());
                if (rest.startsWith("* ")) {
                    String[] parts = rest.split("\\s", 3);
                    nick = parts[1];

                    message = "\001ACTION ";
                    if (parts.length == 3) {
                        message += parts[2];
                    }
                    this.logMessage(ts, channel, nick, message);

                } else if (rest.startsWith("<")) {
                    int endOfNick = rest.indexOf('>');
                    nick = rest.substring(2, endOfNick);
                    message = rest.substring(endOfNick + 1).trim();
                    this.logMessage(ts, channel, nick, message);

                } else if (rest.startsWith("-!-")) {
                    rest = rest.substring(4);
                    nick = rest.split("\\s", 2)[0];

                    if (rest.contains("has quit")) {
                        this.logQuit(ts, channel, nick);
                    } else if (rest.contains("has joined")) {
                        this.logJoin(ts, channel, nick);
                    } else if (rest.contains("has left")) {
                        this.logPart(ts, channel, nick);
                    }
                }
            } else {
                IRCLogger.log.warn("Unknown line: {}", line);
            }
            line = reader.readLine();
        }
    }

    private void logJoin(Timestamp timestamp, String channel, String nick) throws SQLException {
        this.eventStatement.setTimestamp(1, timestamp);
        this.eventStatement.setString(2, "join");
        this.eventStatement.setString(3, nick);
        this.eventStatement.setString(4, channel);
        this.eventStatement.executeUpdate();
        this.eventStatement.clearParameters();
    }

    private void logPart(Timestamp timestamp, String channel, String nick) throws SQLException {
        this.eventStatement.setTimestamp(1, timestamp);
        this.eventStatement.setString(2, "part");
        this.eventStatement.setString(3, nick);
        this.eventStatement.setString(4, channel);
        this.eventStatement.executeUpdate();
        this.eventStatement.clearParameters();
    }

    private void logQuit(Timestamp timestamp, String channel, String nick) throws SQLException {
        this.eventStatement.setTimestamp(1, timestamp);
        this.eventStatement.setString(2, "quit");
        this.eventStatement.setString(3, nick);
        this.eventStatement.setString(4, channel);
        this.eventStatement.executeUpdate();
        this.eventStatement.clearParameters();
    }

    private void logMessage(Timestamp timestamp, String channel, String from, String message) throws SQLException {
        this.messageStatement.setTimestamp(1, timestamp);
        this.messageStatement.setString(2, channel);
        this.messageStatement.setString(3, from);
        this.messageStatement.setString(4, message);
        this.messageStatement.executeUpdate();
        this.messageStatement.clearParameters();
    }

    private Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    @Override
    public void onJoin(Channel channel, User user) {
        try {
            this.logJoin(this.getCurrentTimestamp(), channel.getName(), user.getNick());
        } catch (SQLException e) {
            log.error("SQL error while logging join", e);
        }
    }

    @Override
    public void onPart(Channel channel, User user) {
        try {
            this.logPart(this.getCurrentTimestamp(), channel.getName(), user.getNick());
        } catch (SQLException e) {
            log.error("SQL error while loggin part", e);
        }
    }

    @Override
    public void onQuit(Channel channel, User user) {
        try {
            this.logQuit(this.getCurrentTimestamp(), channel.getName(), user.getNick());
        } catch (SQLException e) {
            log.error("SQL error while logging quit", e);
        }
    }

    @Override
    public void onMessage(Channel channel, User from, String message) {
        try {
            this.logMessage(this.getCurrentTimestamp(), channel.getName(), from.getNick(), message);
        } catch (SQLException e) {
            log.error("SQL error while logging message", e);
        }
    }
}
