package ru.tinkoff.elasticsearch.plugin.analysis.logspeak;

public class GenerateBitNfaTable {
    static class CharPattern {

        boolean matches(int ch) { return true; }

        final void apply(int bit, short[] table) {
            short mask = (short)(1<<bit);
            for (int i=0; i<=0xFFF; i++)
                if (matches(i))
                    table[i] |= mask;
        }
    }

    final static class Alnum extends CharPattern {
        @Override
        boolean matches(int ch) {
            return Character.isAlphabetic(ch) || Character.isDigit(ch) ;
        }
    }

    final static class NotAlnum extends CharPattern {
        @Override
        boolean matches(int ch) {
            return !Character.isAlphabetic(ch) && !Character.isDigit(ch);
        }
    }


    final static class Joiner extends CharPattern {
        @Override
        boolean matches(int ch) {
            return ch == '-' || ch == '.' || ch == '_';
        }
    }

    final static class NotAlnumNotJoiner extends CharPattern {
        Joiner joiner = new Joiner();
        Alnum alnum = new Alnum();
        @Override
        boolean matches(int ch) {
            return !alnum.matches(ch) && !joiner.matches(ch);
        }
    }

    final static class UseFirst extends CharPattern {}

    final static class UseLast extends CharPattern {}

    // Character classes for NFA
    // intuitive character classes are generalized to remove unwanted chars
    // from the whole Unicode range, such as control chars
    // ws - whitespace ; generalized to not alnum, not one of joiner ('-', '_', '.')
    // alnum - alphanumeric ; generalized to alnum, includes digits of all kinds
    // wsOrJoin - whitespace or joiner ; generalized to not alnum
    //
    final static CharPattern ws = new NotAlnumNotJoiner();
    final static CharPattern alnum = new Alnum();
    final static CharPattern join = new Joiner();
    final static CharPattern wsOrJoin = new NotAlnum();
    final static CharPattern right = new UseLast();
    final static CharPattern left = new UseFirst();

    public static String generateSource(CharPattern[] stateMachine) {
        short[] bitNFA = new short[0x1000];
        int startMask = 1;
        int finishMask = 0;
        int leftMask = 0; // left mask
        int rightMask = 0;
        for (int i=0; i<stateMachine.length; i++) {
            stateMachine[i].apply(i, bitNFA);
            if (stateMachine[i] == left) {
                startMask |= 1<<(i+1);
                leftMask |= 1<<i;
            }
            if (stateMachine[i] == right) {
                startMask |= 1<<(i+1);
                rightMask |= 1<<i;
            }
        }
        StringBuilder result = new StringBuilder();
        result.append("class BitNfaConstants { \n");
        result.append("static final short[] bitnfa = {");
        for(int i=0; i< bitNFA.length; i++) {
            if (i % 16 == 0) {
                result.append("\n");
            }
            result.append(String.format(i == bitNFA.length - 1 ? "0x%04x" : "0x%04x, " , bitNFA[i]));
        }
        result.append("\n};\n");
        result.append(String.format("static final short maxInput = 0x%x;\n", bitNFA.length));
        result.append(String.format("static final short startMask = 0x%x;\n", startMask));
        result.append(String.format("static final short leftMask = 0x%x;\n", leftMask));
        result.append(String.format("static final short rightMask = 0x%x;\n", rightMask));
        result.append("}\n");
        return result.toString();
    }

    // Breaks required at (.):
    // ws . alnum
    // alnum . ws
    // wsOrJoin join . alnum
    // alnum . join wsOrJoin
    //
    // NOTE: all of that is such that we do not break on alnum join alnum
    public static void main(String[] args) {
        CharPattern[] stateMachine = {
                ws,
                alnum,
                left,

                alnum,
                ws,
                right,

                wsOrJoin,
                join,
                alnum,
                left,

                alnum,
                join,
                wsOrJoin,
                right
        };

        System.out.println(generateSource(stateMachine));
    }
}
