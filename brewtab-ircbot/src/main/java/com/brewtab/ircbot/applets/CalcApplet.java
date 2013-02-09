package com.brewtab.ircbot.applets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

import com.brewtab.irc.User;
import com.brewtab.irc.client.Channel;

public class CalcApplet implements BotApplet {
    private enum OperatorType {
        BINARY,
        UNARY;
    };

    private static class Operator {
        @SuppressWarnings("unused")
        public String operator;
        public int precedence;
        public OperatorType type;

        public Operator(String operator, int precedence, OperatorType type) {
            this.operator = operator;
            this.precedence = precedence;
            this.type = type;
        }
    }

    private static final HashSet<String> allSpecialTokens;
    private static final Pattern number;

    private static final Operator plus = new Operator("+", 0, OperatorType.BINARY);
    private static final Operator minus = new Operator("-", 0, OperatorType.BINARY);
    private static final Operator times = new Operator("*", 1, OperatorType.BINARY);
    private static final Operator div = new Operator("/", 1, OperatorType.BINARY);
    private static final Operator mod = new Operator("%", 1, OperatorType.BINARY);
    private static final Operator pow = new Operator("**", 3, OperatorType.BINARY);

    private static final Operator and = new Operator("&", 3, OperatorType.BINARY);
    private static final Operator or = new Operator("|", 3, OperatorType.BINARY);
    private static final Operator xor = new Operator("^", 3, OperatorType.BINARY);
    private static final Operator bitnot = new Operator("~", 2, OperatorType.UNARY);

    private static final Operator cos = new Operator("cos", 2, OperatorType.UNARY);
    private static final Operator sin = new Operator("sin", 2, OperatorType.UNARY);
    private static final Operator tan = new Operator("tan", 2, OperatorType.UNARY);
    private static final Operator sqrt = new Operator("sqrt", 2, OperatorType.UNARY);
    private static final Operator log = new Operator("log", 2, OperatorType.UNARY);
    private static final Operator log2 = new Operator("log2", 2, OperatorType.UNARY);
    private static final Operator log10 = new Operator("log10", 2, OperatorType.UNARY);

    private static final Operator neg = new Operator("-", 4, OperatorType.UNARY);

    private static final String openGroup = "(";
    private static final String closeGroup = ")";

    /* Initialize full token list */
    static {
        allSpecialTokens = new HashSet<String>();
        allSpecialTokens.add("+");
        allSpecialTokens.add("-");
        allSpecialTokens.add("*");
        allSpecialTokens.add("/");
        allSpecialTokens.add("%");
        allSpecialTokens.add("**");

        allSpecialTokens.add("&");
        allSpecialTokens.add("|");
        allSpecialTokens.add("^");
        allSpecialTokens.add("~");

        allSpecialTokens.add("cos");
        allSpecialTokens.add("sin");
        allSpecialTokens.add("tan");
        allSpecialTokens.add("sqrt");
        allSpecialTokens.add("log");
        allSpecialTokens.add("log2");
        allSpecialTokens.add("log10");

        allSpecialTokens.add("(");
        allSpecialTokens.add(")");

        number = Pattern.compile("[0-9]+|[0-9]*\\.[0-9]+|0b[01]+|0x[0-9abcdefABCDEF]+|PI|E");
    }

    private static ArrayList<String> tokenizeExpression(String expression) throws Exception {
        ArrayList<String> tokens = new ArrayList<String>();
        int ti = 0; /* Token index */
        int longestMatch = 0; /* Token length */
        int tempLength = 1;

        while (ti < expression.length()) {
            String token = expression.substring(ti, ti + tempLength);
            boolean isNum = number.matcher(token).matches();

            if (token.matches("\\s")) {
                /* Remove whitespace */
                ti++;
            } else if (isNum || allSpecialTokens.contains(token)) {
                longestMatch = tempLength;
                tempLength++;
                if (ti + tempLength > expression.length()) {
                    tokens.add(token);
                    break;
                }
            } else {
                /* Increase length to attempt match */
                tempLength++;
                if (tempLength - longestMatch > 5 || ti + tempLength > expression.length()) {
                    if (longestMatch > 0) {
                        tokens.add(expression.substring(ti, ti + longestMatch));

                        ti += longestMatch;
                        longestMatch = 0;
                        tempLength = 1;
                    } else {
                        throw new Exception("Invalid token '" + token + "'");
                    }
                }
            }
        }

        return tokens;
    }

    private static Operator getOperator(ArrayList<String> tokens, int i) {
        String token = tokens.get(i);

        if (token.equals("+")) {
            return plus;
        } else if (token.equals("*")) {
            return times;
        } else if (token.equals("/")) {
            return div;
        } else if (token.equals("%")) {
            return mod;
        } else if (token.equals("**")) {
            return pow;
        } else if (token.equals("&")) {
            return and;
        } else if (token.equals("|")) {
            return or;
        } else if (token.equals("^")) {
            return xor;
        } else if (token.equals("~")) {
            return bitnot;
        } else if (token.equals("cos")) {
            return cos;
        } else if (token.equals("sin")) {
            return sin;
        } else if (token.equals("tan")) {
            return tan;
        } else if (token.equals("sqrt")) {
            return sqrt;
        } else if (token.equals("log")) {
            return log;
        } else if (token.equals("log2")) {
            return log2;
        } else if (token.equals("log10")) {
            return log10;
        } else if (token.equals("-")) {
            if (i == 0) {
                return neg;
            }

            String previous = tokens.get(i - 1);
            if (previous.equals(closeGroup)) {
                return minus;
            } else if (allSpecialTokens.contains(previous)) {
                return neg;
            } else {
                return minus;
            }
        }

        return null;
    }

    private static double doOperation(Operator op, double... operands) throws Exception {
        if (op == plus) {
            return operands[0] + operands[1];
        } else if (op == minus) {
            return operands[0] - operands[1];
        } else if (op == times) {
            return operands[0] * operands[1];
        } else if (op == div) {
            return operands[0] / operands[1];
        } else if (op == mod) {
            return ((int) operands[0]) % ((int) operands[1]);
        } else if (op == pow) {
            return Math.pow(operands[0], operands[1]);
        } else if (op == and) {
            return ((int) operands[0]) & ((int) operands[1]);
        } else if (op == or) {
            return ((int) operands[0]) | ((int) operands[1]);
        } else if (op == xor) {
            return ((int) operands[0]) ^ ((int) operands[1]);
        } else if (op == bitnot) {
            return ~((int) operands[0]);
        } else if (op == neg) {
            return -operands[0];
        } else if (op == sin) {
            return Math.sin(operands[0]);
        } else if (op == cos) {
            return Math.cos(operands[0]);
        } else if (op == tan) {
            return Math.tan(operands[0]);
        } else if (op == sqrt) {
            return Math.sqrt(operands[0]);
        } else if (op == log) {
            return Math.log(operands[0]);
        } else if (op == log2) {
            return Math.log(operands[0]) / Math.log(2);
        } else if (op == log10) {
            return Math.log10(operands[0]);
        }

        throw new Exception("Invalid operator");
    }

    static int matchGroup(ArrayList<String> tokens, int i) throws Exception {
        int depth = 1;

        i++;
        while (depth != 0 && i < tokens.size()) {
            if (tokens.get(i).equals(openGroup)) {
                depth++;
            } else if (tokens.get(i).equals(closeGroup)) {
                depth--;
            }

            i++;
        }

        if (depth == 0) {
            return i - 1;
        }

        throw new Exception("Mismatched parentheses");
    }

    static double evaluateSubExpression(ArrayList<String> tokens, int start, int end) throws Exception {
        Operator rootOp = null;
        int iRootOp = 0;
        Operator tempOp = null;
        String token;
        int depth = 0;

        /* Trim parentheses from expression */
        while (tokens.get(start).equals(openGroup) && matchGroup(tokens, start) == end) {
            start++;
            end--;
        }

        /* Singleton (should just be a value) */
        if (end - start == 0) {
            token = tokens.get(start);
            if (number.matcher(token).matches()) {
                if (token.equals("PI")) {
                    return Math.PI;
                } else if (token.equals("E")) {
                    return Math.E;
                } else if (token.startsWith("0b")) {
                    return Integer.valueOf(token.replaceFirst("0b", ""), 2);
                } else if (token.startsWith("0x")) {
                    return Integer.valueOf(token.replaceFirst("0x", ""), 16);
                } else {
                    return Double.valueOf(token);
                }
            } else {
                throw new Exception(String.format("Expected literal at token %d (%s)", start, token));
            }
        }

        /*
         * Find the right-most, lowest precedence operator, at the top level.
         * The expression is split at this operator and recurses to find the
         * value of this operators operands.
         */
        for (int i = start; i <= end; i++) {
            tempOp = getOperator(tokens, i);

            if (tempOp != null) {
                if (depth == 0 && (rootOp == null || tempOp.precedence <= rootOp.precedence)) {
                    iRootOp = i;
                    rootOp = tempOp;
                }
            } else {
                token = tokens.get(i);
                if (token.equals(openGroup)) {
                    depth++;
                } else if (token.equals(closeGroup)) {
                    depth--;
                }

                if (depth < 0) {
                    throw new Exception("Mismatched parentheses");
                }
            }
        }

        /* No operator found */
        if (rootOp == null) {
            throw new Exception(String.format("Missing operator"));
        }

        /* Operator is at the end of the expression */
        if (iRootOp == end) {
            throw new Exception("Operator at end of expression");
        }

        /* A binary operator is at the beginning of an expression */
        if (iRootOp == start && rootOp.type == OperatorType.BINARY) {
            throw new Exception("Binary operator at beginning of expression");
        }

        /*
         * Recurse to evaluate the operators operands, then apply the operation
         * to these operands
         */
        switch (rootOp.type) {
        case UNARY:
            double operand = evaluateSubExpression(tokens, iRootOp + 1, end);
            return doOperation(rootOp, operand);

        case BINARY:
            double operandOne = evaluateSubExpression(tokens, start, iRootOp - 1);
            double operandTwo = evaluateSubExpression(tokens, iRootOp + 1, end);
            return doOperation(rootOp, operandOne, operandTwo);
        }

        /* Not reached */
        return 0;
    }

    public static double evaluateExpression(String expression) throws Exception {
        ArrayList<String> tokens = tokenizeExpression(expression);
        return evaluateSubExpression(tokens, 0, tokens.size() - 1);
    }

    @Override
    public void run(Channel channel, User from, String command, String[] args, String unparsed) {
        String expression = unparsed;

        try {
            String outFormat = "float";
            if (expression.contains("as")) {
                String[] parts = expression.split("as");
                expression = parts[0].trim();
                outFormat = parts[1].trim().toLowerCase();
            }

            double result = CalcApplet.evaluateExpression(expression);
            String sResult = null;

            if (outFormat.equals("float")) {
                sResult = String.format("%.10f", result);
                sResult = sResult.replaceFirst("0*$", "");
                sResult = sResult.replaceFirst("\\.$", "");
            } else if (outFormat.equals("hex")) {
                sResult = "0x" + Integer.toString((int) result, 16);
            } else if (outFormat.equals("bin")) {
                sResult = "0b" + Integer.toString((int) result, 2);
            } else {
                sResult = "Invalid conversion specifier";
            }

            channel.write(String.format("%s: %s", from.getNick(), sResult));
        } catch (Exception e) {
            channel.write(String.format("%s: %s", from.getNick(), e.getMessage()));
        }
    }
}
