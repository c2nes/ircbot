package com.brewtab.irclog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;
import com.brewtab.irc.client.ChannelListener;
import com.google.common.base.Throwables;

public class IRCLogger implements ChannelListener {
    private static final Logger log = LoggerFactory.getLogger(IRCLogger.class);

    private Connection db;
    private PreparedStatement eventStatement;
    private PreparedStatement messageStatement;

    public IRCLogger(Connection dbConnection) throws SQLException {
        this.db = dbConnection;

        this.eventStatement = this.db
            .prepareStatement("INSERT INTO events (event_time, type, nick, extra) VALUES (?, ?, ?, ?)");
        this.messageStatement = this.db
            .prepareStatement("INSERT INTO messages (msg_time, channel, nick, message) VALUES (?, ?, ?, ?)");
    }

    public PreparedStatement prepareQuery(String query) {
        try {
            return db.prepareStatement(query);
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    private List<IRCLogEvent> queryMessages(PreparedStatement stmt, int limit) {
        try {
            ResultSet rows = stmt.executeQuery();
            ResultSetMetaData metadata = rows.getMetaData();
            Set<String> columns = new HashSet<String>();
            List<IRCLogEvent> messages = new ArrayList<IRCLogEvent>();

            for (int i = 0; i < metadata.getColumnCount(); i++) {
                columns.add(metadata.getColumnName(i + 1));
            }

            while (rows.next() && (limit < 0 || messages.size() < limit)) {
                IRCLogEvent event = new IRCLogEvent(IRCLogEvent.MESSAGE_EVENT);

                if (columns.contains("msg_time")) {
                    event.setDate(rows.getTimestamp("msg_time"));
                }

                if (columns.contains("channel")) {
                    event.setChannel(rows.getString("channel"));
                }

                if (columns.contains("nick")) {
                    event.setNick(rows.getString("nick"));
                }

                if (columns.contains("message")) {
                    event.setData(rows.getString("message"));
                }

                messages.add(event);
            }

            return messages;
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    public List<IRCLogEvent> queryMessages(PreparedStatement stmt) {
        return queryMessages(stmt, -1);
    }

    public IRCLogEvent queryMessage(PreparedStatement stmt) {
        List<IRCLogEvent> messages = queryMessages(stmt, 1);

        if (messages.isEmpty()) {
            return null;
        } else {
            return messages.get(0);
        }
    }

    public int countMessages(PreparedStatement stmt) {
        try {
            ResultSet rows = stmt.executeQuery();

            if (rows.next()) {
                return rows.getInt(1);
            }

            return 0;
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    private List<IRCLogEvent> queryEvents(PreparedStatement stmt, int limit) {
        try {
            ResultSet rows = stmt.executeQuery();
            ResultSetMetaData metadata = rows.getMetaData();
            Set<String> columns = new HashSet<String>();
            List<IRCLogEvent> events = new ArrayList<IRCLogEvent>();

            for (int i = 0; i < metadata.getColumnCount(); i++) {
                columns.add(metadata.getColumnName(i + 1));
            }

            if (!columns.contains("type")) {
                throw new IllegalArgumentException("Event query must include `type` column");
            }

            while (rows.next() && (limit < 0 || events.size() < limit)) {
                String type = rows.getString("type");
                IRCLogEvent event;

                if (type.equals("join")) {
                    event = new IRCLogEvent(IRCLogEvent.JOIN_EVENT);
                } else if (type.equals("part")) {
                    event = new IRCLogEvent(IRCLogEvent.PART_EVENT);
                } else if (type.equals("quit")) {
                    event = new IRCLogEvent(IRCLogEvent.QUIT_EVENT);
                } else {
                    log.error("Invalid event type '{}'", type);
                    continue;
                }

                if (columns.contains("event_time")) {
                    event.setDate(rows.getTimestamp("event_time"));
                }

                if (columns.contains("extra")) {
                    event.setChannel(rows.getString("extra"));
                }

                if (columns.contains("nick")) {
                    event.setNick(rows.getString("nick"));
                }

                events.add(event);
            }

            return events;
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    public List<IRCLogEvent> queryEvents(PreparedStatement stmt) {
        return queryEvents(stmt, -1);
    }

    public IRCLogEvent queryEvent(PreparedStatement stmt) {
        List<IRCLogEvent> events = queryEvents(stmt, 1);

        if (events.isEmpty()) {
            return null;
        } else {
            return events.get(0);
        }
    }

    public int countEvent(PreparedStatement stmt) {
        try {
            ResultSet rows = stmt.executeQuery();

            if (rows.next()) {
                return rows.getInt(1);
            }

            return 0;
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    public void openSession(Timestamp timestamp) throws SQLException {
        PreparedStatement statement = this.db
            .prepareStatement("INSERT INTO sessions (start_time, active) VALUES (?, TRUE)");
        statement.setTimestamp(1, timestamp);
        statement.execute();
    }

    public void openSession() throws SQLException {
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

    void logJoin(Timestamp timestamp, String channel, String nick) throws SQLException {
        synchronized (eventStatement) {
            this.eventStatement.setTimestamp(1, timestamp);
            this.eventStatement.setString(2, "join");
            this.eventStatement.setString(3, nick);
            this.eventStatement.setString(4, channel);
            this.eventStatement.executeUpdate();
            this.eventStatement.clearParameters();
        }
    }

    void logPart(Timestamp timestamp, String channel, String nick) throws SQLException {
        synchronized (eventStatement) {
            this.eventStatement.setTimestamp(1, timestamp);
            this.eventStatement.setString(2, "part");
            this.eventStatement.setString(3, nick);
            this.eventStatement.setString(4, channel);
            this.eventStatement.executeUpdate();
            this.eventStatement.clearParameters();
        }
    }

    void logQuit(Timestamp timestamp, String channel, String nick) throws SQLException {
        synchronized (eventStatement) {
            this.eventStatement.setTimestamp(1, timestamp);
            this.eventStatement.setString(2, "quit");
            this.eventStatement.setString(3, nick);
            this.eventStatement.setString(4, channel);
            this.eventStatement.executeUpdate();
            this.eventStatement.clearParameters();
        }
    }

    void logMessage(Timestamp timestamp, String channel, String from, String message) throws SQLException {
        synchronized (messageStatement) {
            this.messageStatement.setTimestamp(1, timestamp);
            this.messageStatement.setString(2, channel);
            this.messageStatement.setString(3, from);
            this.messageStatement.setString(4, message);
            this.messageStatement.executeUpdate();
            this.messageStatement.clearParameters();
        }
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
