package com.brewtab.irclog;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IRCStatistics {
    private static final Logger log = LoggerFactory.getLogger(IRCStatistics.class);

    public static int SEARCH_STARTSWITH = 1;
    public static int SEARCH_CONTAINS = 2;
    public static int SEARCH_MATCHES = 3;
    public static int SEARCH_REGEXP = 4;

    private IRCLogger logger;

    public IRCStatistics(IRCLogger logger) {
        this.logger = logger;
    }

    public IRCLogEvent getLastEvent(String channel, String nick) {
        IRCLogEvent messageEvent = this.getLastMessageEvent(channel, nick);
        IRCLogEvent statusEvent = this.getLastStatusEvent(channel, nick);

        if (messageEvent == null) {
            return statusEvent;
        } else if (statusEvent == null) {
            return messageEvent;
        } else if (statusEvent.getDate().after(messageEvent.getDate())) {
            return statusEvent;
        } else {
            return messageEvent;
        }
    }

    public IRCLogEvent getLastMessageEvent(String channel, String nick) {
        PreparedStatement query;
        ResultSet resultSet;

        try {
            query = this.logger
                    .prepareQuery("SELECT msg_time, message FROM message_log WHERE channel = ? AND from_nick = ? ORDER BY msg_time DESC LIMIT 1");
            query.setString(1, channel);
            query.setString(2, nick);
            resultSet = query.executeQuery();

            if (resultSet.next()) {
                return new IRCLogEvent(resultSet.getTimestamp("msg_time"), IRCLogEvent.MESSAGE_EVENT, channel, nick,
                        resultSet.getString("message"));
            }

            return null;
        } catch (SQLException e) {
            log.error("sql error", e);
            return null;
        }
    }

    public IRCLogEvent getLastStatusEvent(String channel, String nick) {
        PreparedStatement query;
        ResultSet resultSet;

        try {
            query = this.logger
                    .prepareQuery("SELECT event_time, type FROM event_log WHERE extra = ? AND user = ? ORDER BY event_time DESC LIMIT 1");
            query.setString(1, channel);
            query.setString(2, nick);
            resultSet = query.executeQuery();

            if (resultSet.next()) {
                if (resultSet.getString("type").equals("join")) {
                    return new IRCLogEvent(resultSet.getTimestamp("event_time"), IRCLogEvent.JOIN_EVENT, channel, nick);
                } else if (resultSet.getString("type").equals("part")) {
                    return new IRCLogEvent(resultSet.getTimestamp("event_time"), IRCLogEvent.PART_EVENT, channel, nick);
                } else if (resultSet.getString("type").equals("quit")) {
                    return new IRCLogEvent(resultSet.getTimestamp("event_time"), IRCLogEvent.QUIT_EVENT, channel, nick);
                }
            }

            return null;
        } catch (SQLException e) {
            log.error("sql error", e);
            return null;
        }
    }

    public List<IRCLogEvent> findMessages(String channel, String nick, String search, int search_type) {
        PreparedStatement query;
        ResultSet resultSet;
        ArrayList<IRCLogEvent> messages = new ArrayList<IRCLogEvent>();

        /* Modify search term */
        if (search_type == SEARCH_STARTSWITH) {
            search = search + "%";
        } else if (search_type == SEARCH_CONTAINS) {
            search = "%" + search + "%";
        }

        try {
            /* Build query */
            if (nick != null && search_type == SEARCH_MATCHES) {
                query = this.logger
                        .prepareQuery("SELECT msg_time, from_nick, message FROM message_log WHERE channel = ? AND from_nick = ? AND message = ? ORDER BY msg_time DESC");
                query.setString(1, channel);
                query.setString(2, nick);
                query.setString(3, search);
            } else if (nick == null && search_type == SEARCH_MATCHES) {
                query = this.logger
                        .prepareQuery("SELECT msg_time, from_nick, message FROM message_log WHERE channel = ? AND message = ? ORDER BY msg_time DESC");
                query.setString(1, channel);
                query.setString(2, search);
            } else if (nick != null && search_type == SEARCH_REGEXP) {
                query = this.logger
                        .prepareQuery("SELECT msg_time, from_nick, message FROM message_log WHERE channel = ? AND from_nick = ? AND message REGEXP ? ORDER BY msg_time DESC");
                query.setString(1, channel);
                query.setString(2, nick);
                query.setString(3, search);
            } else if (nick == null && search_type == SEARCH_REGEXP) {
                query = this.logger
                        .prepareQuery("SELECT msg_time, from_nick, message FROM message_log WHERE channel = ? AND message REGEXP ? ORDER BY msg_time DESC");
                query.setString(1, channel);
                query.setString(2, search);
            } else if (nick != null) {
                query = this.logger
                        .prepareQuery("SELECT msg_time, from_nick, message FROM message_log WHERE channel = ? AND from_nick = ? AND message LIKE ? ORDER BY msg_time DESC");
                query.setString(1, channel);
                query.setString(2, nick);
                query.setString(3, search);
            } else {
                query = this.logger
                        .prepareQuery("SELECT msg_time, from_nick, message FROM message_log WHERE channel = ? AND message LIKE ? ORDER BY msg_time DESC");
                query.setString(1, channel);
                query.setString(2, search);
            }

            resultSet = query.executeQuery();

            while (resultSet.next()) {
                IRCLogEvent message = new IRCLogEvent(resultSet.getTimestamp("msg_time"), IRCLogEvent.MESSAGE_EVENT,
                        channel, resultSet.getString("from_nick"), resultSet.getString("message"));
                messages.add(message);
            }

            return messages;
        } catch (SQLException e) {
            log.error("sql error", e);
            return null;
        }
    }

    public List<IRCLogEvent> findMessages(String channel, String nick, String search) {
        return findMessages(channel, nick, search, IRCStatistics.SEARCH_CONTAINS);
    }
}
