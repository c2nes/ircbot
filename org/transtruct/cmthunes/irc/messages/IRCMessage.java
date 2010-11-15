package org.transtruct.cmthunes.irc.messages;

import java.util.*;

public class IRCMessage {
    private String prefix;
    private IRCMessageType type;
    private String[] args;

    private static class ArgumentConstraint {
        private int min, max;

        private ArgumentConstraint(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @SuppressWarnings("unused")
        public static ArgumentConstraint atLeast(int n) {
            return new ArgumentConstraint(n, -1);
        }

        public static ArgumentConstraint atMost(int n) {
            return new ArgumentConstraint(-1, n);
        }

        public static ArgumentConstraint between(int min, int max) {
            return new ArgumentConstraint(min, max);
        }

        public static ArgumentConstraint exactly(int n) {
            return new ArgumentConstraint(n, n);
        }

        public boolean check(String[] args) {
            if(this.min != -1 && args.length < this.min) {
                return false;
            }

            if(this.max != -1 && args.length > this.max) {
                return false;
            }

            return true;
        }
    }

    /* Mapping of message constraints to allow message formats to be enforced */
    private static EnumMap<IRCMessageType, ArgumentConstraint> argumentConstraints = new EnumMap<IRCMessageType, ArgumentConstraint>(
            IRCMessageType.class);

    static {
        argumentConstraints.put(IRCMessageType.NICK, ArgumentConstraint.exactly(1));
        argumentConstraints.put(IRCMessageType.PASS, ArgumentConstraint.exactly(1));
        argumentConstraints.put(IRCMessageType.USER, ArgumentConstraint.exactly(4));
        argumentConstraints.put(IRCMessageType.QUIT, ArgumentConstraint.atMost(1));

        argumentConstraints.put(IRCMessageType.JOIN, ArgumentConstraint.between(1, 2));
        argumentConstraints.put(IRCMessageType.PART, ArgumentConstraint.between(1, 2));
        argumentConstraints.put(IRCMessageType.MODE, ArgumentConstraint.between(2, 5));
        argumentConstraints.put(IRCMessageType.TOPIC, ArgumentConstraint.between(1, 2));
        argumentConstraints.put(IRCMessageType.NAMES, ArgumentConstraint.atMost(1));

        argumentConstraints.put(IRCMessageType.PRIVMSG, ArgumentConstraint.exactly(2));
    }

    public IRCMessage(IRCMessageType type, String... args) throws IRCInvalidMessageException {
        this(null, type, args);
    }

    public IRCMessage(String prefix, IRCMessageType type, String... args) throws IRCInvalidMessageException {
        this.prefix = prefix;
        this.type = type;
        this.args = args;

        if(this.validArgs() == false) {
            throw new IRCInvalidMessageException("Invalid arguments");
        }
    }

    private boolean validArgs() {
        ArgumentConstraint constraint = IRCMessage.argumentConstraints.get(this.type);

        if(constraint != null) {
            return constraint.check(this.args);
        }

        return true;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        if(this.prefix != null) {
            buffer.append(":").append(this.prefix).append(" ");
        }

        buffer.append(this.type.toString());

        if(this.args.length > 0) {
            for(int i = 0; i < this.args.length - 1; i++) {
                buffer.append(" ").append(this.args[i]);
            }

            String lastArg = this.args[this.args.length - 1];
            switch(this.type) {
            case PRIVMSG:
            case USER:
            case PART:
                buffer.append(" :").append(lastArg);
                break;

            default:
                buffer.append(" ").append(lastArg);
            }
        }

        buffer.append("\r\n");
        return buffer.toString();
    }

    public static IRCMessage fromString(String messageFrame) throws IRCInvalidMessageException {
        String prefix = null;
        String trailing = null;

        messageFrame = messageFrame.trim();

        /* Remove prefix */
        if(messageFrame.startsWith(":")) {
            int splitPoint = messageFrame.indexOf(' ');
            prefix = messageFrame.substring(1, splitPoint);
            messageFrame = messageFrame.substring(splitPoint + 1).trim();
        }

        /* Remove trailing part */
        int trailingPoint = messageFrame.indexOf(':');
        if(trailingPoint != -1) {
            trailing = messageFrame.substring(trailingPoint + 1);
            messageFrame = messageFrame.substring(0, trailingPoint).trim();
        }

        /* Remove type */
        int endTypePoint = messageFrame.indexOf(' ');
        String typeString;
        if(endTypePoint == -1) {
            typeString = messageFrame;
            messageFrame = "";
        } else {
            typeString = messageFrame.substring(0, endTypePoint);
            messageFrame = messageFrame.substring(endTypePoint + 1).trim();
        }

        /* Attempt a type lookup for the message type */
        IRCMessageType type = IRCMessageType.fromString(typeString);
        if(type == null) {
            throw new IRCInvalidMessageException("invalid type: " + typeString);
        }

        /*
         * Split the remaining args into an arguments list and append the
         * trailing argument if it exists
         */
        LinkedList<String> argsList = new LinkedList<String>();
        while(messageFrame.length() > 0) {
            int nextSpace = messageFrame.indexOf(' ');
            if(nextSpace == -1) {
                argsList.add(messageFrame);
                break;
            } else {
                argsList.add(messageFrame.substring(0, nextSpace));
                messageFrame = messageFrame.substring(nextSpace + 1).trim();
            }
        }

        if(trailing != null) {
            argsList.add(trailing);
        }

        String[] args = new String[argsList.size()];
        return new IRCMessage(prefix, type, argsList.toArray(args));
    }

    public String getPrefix() {
        return this.prefix;
    }

    public IRCMessageType getType() {
        return this.type;
    }

    public String[] getArgs() {
        return this.args;
    }
}
