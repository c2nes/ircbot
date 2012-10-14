package com.brewtab.ircbot;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;

import com.brewtab.irclog.IRCLogger;

public class InitializeDatabase {
    private static void usage() {
        System.out.println("Usage: InitializeDatabase <log file> <database>");
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        Connection connection;
        IRCLogger logger;
        String logFileName;
        String databaseName;

        for (String arg : args) {
            if (arg.equals("-h")) {
                usage();
                return;
            }
        }

        if (args.length != 2) {
            usage();
            return;
        }

        logFileName = args[0];
        databaseName = args[1];

        if ((new File(String.format("%s.h2.db", databaseName))).exists()) {
            System.err.println("Error: Database already exists");
            return;
        }

        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection(String.format("jdbc:h2:%s", databaseName), "sa", "");
        logger = new IRCLogger(connection);

        System.out.println("Reading log file...");
        logger.addFromIrssiLog("#main", new FileReader(logFileName));
        System.out.println("done.");
    }
}
