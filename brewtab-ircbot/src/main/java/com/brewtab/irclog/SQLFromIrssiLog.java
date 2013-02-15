package com.brewtab.irclog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

public class SQLFromIrssiLog {
    private static final Logger log = LoggerFactory.getLogger(SQLFromIrssiLog.class);

    private static final Pattern metaPattern = Pattern.compile("^--- (.*)");
    private static final Pattern eventPattern = Pattern.compile("^(\\d\\d):(\\d\\d) (.*)");
    private static final Pattern messagePattern = Pattern.compile("<(.)(.*?)> (.*)");
    private static final Pattern actionPattern = Pattern.compile(" \\* (.*?) (.*)");

    @Argument(required = true)
    private String logFile;

    @Argument
    private String channel;

    private BufferedReader input;
    private DateTime currentDate;

    private static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("EEE MMM dd yyyy");
    private static final DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss yyyy");

    private DateTime sessionStart = null;

    private void unexepectedLine(String line) {
        log.warn("Unexpected log line: {}", line);
    }

    private String timestampFormat(DateTime t) {
        return t.toString("yyyy-MM-dd HH:mm:ss");
    }

    private String prepare(String format, Object... args) {
        List<String> newArgs = new ArrayList<String>(args.length);

        for (Object arg : args) {
            if (arg instanceof DateTime) {
                newArgs.add("'" + timestampFormat((DateTime) arg) + "'");
            } else if (arg instanceof Integer || arg instanceof Long || arg instanceof Double || arg instanceof Float) {
                newArgs.add(arg.toString());
            } else if (arg instanceof String) {
                newArgs.add("'" + ((String)arg).replace("'", "''") + "'");
            } else {
                throw new IllegalArgumentException("Can not convert type " + arg);
            }
        }

        return String.format(format.replace("?", "%s"), newArgs.toArray());
    }

    private void startSession(DateTime startTime) {
        if (sessionStart != null) {
            throw new IllegalStateException("Session already in progress");
        }

        this.sessionStart = startTime;
    }

    private void endSession(DateTime endTime) {
        if (sessionStart == null) {
            throw new IllegalStateException("Session ended without starting");
        }

        String sql = "INSERT INTO sessions (start_time, end_time, active) VALUES (?, ?, FALSE);";
        String prepared = prepare(sql, sessionStart, endTime);

        System.out.println(prepared);
        sessionStart = null;
    }

    private void event(DateTime time, String type, String nick) {
        String sql = "INSERT INTO events (event_time, type, nick) VALUES (?, ?, ?);";
        String prepared = prepare(sql, time, type, nick);

        System.out.println(prepared);
    }

    private void event(DateTime time, String type, String nick, String extra) {
        String sql = "INSERT INTO events (event_time, type, nick, extra) VALUES (?, ?, ?, ?);";
        String prepared = prepare(sql, time, type, nick, extra.substring(0, Math.min(extra.length(), 256)));

        System.out.println(prepared);
    }

    private void message(DateTime time, String channel, String nick, String msg) {
        String sql = "INSERT INTO messages (msg_time, channel, nick, message) VALUES (?, ?, ?, ?);";
        String prepared = prepare(sql, time, channel, nick, msg);

        System.out.println(prepared);
    }

    private void action(DateTime time, String channel, String nick, String act) {
        message(time, channel, nick, "\001ACTION " + act);
    }

    private void processLine(String line) throws Exception {
        Matcher lineMatcher = eventPattern.matcher(line);

        if (lineMatcher.matches()) {
            int hours = Integer.valueOf(lineMatcher.group(1));
            int minutes = Integer.valueOf(lineMatcher.group(2));
            currentDate = currentDate.withHourOfDay(hours).withMinuteOfHour(minutes);

            String rest = lineMatcher.group(3);

            if (rest.startsWith("<")) {
                Matcher messageMatcher = messagePattern.matcher(rest);

                if (!messageMatcher.matches()) {
                    unexepectedLine(line);
                    return;
                }

                String nick = messageMatcher.group(2);
                String msg = messageMatcher.group(3);

                message(currentDate, channel, nick, msg);
            } else if (rest.startsWith(" *")) {
                Matcher actionMatcher = actionPattern.matcher(rest);

                if (!actionMatcher.matches()) {
                    unexepectedLine(line);
                    return;
                }

                String nick = actionMatcher.group(1);
                String action = actionMatcher.group(2);

                action(currentDate, channel, nick, action);
            } else if (rest.startsWith("-!-")) {
                rest = rest.substring(4);

                if (rest.startsWith("Irssi: ")) {
                    // Ignore
                } else if (rest.startsWith("mode/")) {
                    // Ignore
                } else if (rest.matches(".*? \\[.*?\\] .*")) {
                    Matcher userEventMatcher = Pattern.compile("(.*?) \\[(.*?)\\] (.*)").matcher(rest);
                    userEventMatcher.matches();

                    String nick = userEventMatcher.group(1);
                    String user = userEventMatcher.group(2);
                    String event = userEventMatcher.group(3);

                    if (event.startsWith("has quit ")) {
                        Matcher quitMatcher = Pattern.compile("has quit \\[(.*)\\]$").matcher(event);

                        if (!quitMatcher.matches()) {
                            unexepectedLine(line);
                            return;
                        }

                        String reason = quitMatcher.group(1);

                        event(currentDate, "quit", nick, channel);
                    } else if (event.startsWith("has joined ")) {
                        Matcher joinedMatcher = Pattern.compile("has joined (.*)").matcher(event);

                        if (!joinedMatcher.matches()) {
                            unexepectedLine(line);
                            return;
                        }

                        String channel = joinedMatcher.group(1);

                        event(currentDate, "join", nick, channel);
                    } else if (event.startsWith("has left ")) {
                        Matcher leftMatcher = Pattern.compile("has left (.*?) \\[(.*)\\]$").matcher(event);

                        if (!leftMatcher.matches()) {
                            unexepectedLine(line);
                            return;
                        }

                        String channel = leftMatcher.group(1);
                        String reason = leftMatcher.group(2);

                        event(currentDate, "part", nick, channel);
                    } else {
                        unexepectedLine(line);
                        return;
                    }
                } else if (rest.startsWith("You're now known as ")) {
                    Matcher nickChangeMatcher = Pattern.compile("^You're now known as (.*?)$").matcher(rest);
                    nickChangeMatcher.matches();

                    String toNick = nickChangeMatcher.group(1);

                    // Not supported
                } else if (rest.matches("^[^ ]* is now known as [^ ]*$")) {
                    Matcher nickChangeMatcher = Pattern.compile("^([^ ]*) is now known as ([^ ]*)$").matcher(rest);
                    nickChangeMatcher.matches();

                    String fromNick = nickChangeMatcher.group(1);
                    String toNick = nickChangeMatcher.group(2);

                    // Not supported
                } else if (rest.matches("^.*? was kicked from .*? by .*? \\[.*?\\]$")) {
                    Matcher kickMatcher = Pattern.compile("^(.*?) was kicked from (.*?) by (.*?) \\[(.*?)\\]$")
                        .matcher(rest);
                    kickMatcher.matches();

                    String nick = kickMatcher.group(1);
                    String from = kickMatcher.group(2);
                    String byNick = kickMatcher.group(3);
                    String reason = kickMatcher.group(4);

                    // Not supported
                } else if (rest.matches("^.*? changed the topic of .*? to: .*")) {
                    Matcher topicMatcher = Pattern.compile("^(.*?) changed the topic of (.*?) to: (.*)").matcher(rest);
                    topicMatcher.matches();

                    String nick = topicMatcher.group(1);
                    String topic = topicMatcher.group(3);

                    // Not supported
                } else {
                    unexepectedLine(line);
                    return;
                }
            } else {
                unexepectedLine(line);
                return;
            }
        } else {
            lineMatcher = metaPattern.matcher(line);

            if (!lineMatcher.matches()) {
                unexepectedLine(line);
                return;
            }

            String rest = lineMatcher.group(1);

            if (rest.startsWith("Day changed")) {
                currentDate = dateFormat.parseDateTime(rest.replace("Day changed ", ""));
            } else if (rest.startsWith("Log opened")) {
                if (sessionStart != null) {
                    // Previous session was ended abruptly. Close it using the
                    // last known timestamp.
                    endSession(currentDate);
                }

                startSession(dateTimeFormat.parseDateTime(rest.replace("Log opened ", "")));
            } else if (rest.startsWith("Log closed")) {
                if (sessionStart != null) {
                    endSession(dateTimeFormat.parseDateTime(rest.replace("Log closed ", "")));
                }
            } else {
                unexepectedLine(line);
                return;
            }
        }
    }

    private void run() throws Exception {
        if (channel == null) {
            Matcher channelMatcher = Pattern.compile("^(?:.*/)?(.*)\\.log$").matcher(logFile);

            if (channelMatcher.matches()) {
                channel = channelMatcher.group(1);
            } else {
                throw new IllegalArgumentException("Could not determine channel from log file name");
            }
        }

        input = new BufferedReader(new FileReader(logFile));
        currentDate = DateTime.now();

        System.out.println("BEGIN;");

        while (true) {
            String line = input.readLine();

            if (line == null) {
                break;
            }

            try {
                processLine(line);
            } catch (Exception e) {
                log.error("Error on line: {}", line, e);
                break;
            }
        }

        System.out.println("COMMIT;");

        input.close();
    }

    public static void main(String[] args) throws Exception {
        SQLFromIrssiLog cmd = new SQLFromIrssiLog();

        try {
            Args.parse(cmd, args);
        } catch (IllegalArgumentException e) {
            Args.usage(cmd);
            System.exit(-1);
            return;
        }

        cmd.run();
    }
}
