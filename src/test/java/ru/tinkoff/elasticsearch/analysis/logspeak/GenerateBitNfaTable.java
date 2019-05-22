package ru.tinkoff.elasticsearch.analysis.logspeak;

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
    final static CharPattern useLast = new UseLast();
    final static CharPattern useFirst = new UseFirst();

    public static String generateSource(CharPattern[] stateMachine) {
        short[] bitNFA = new short[0x1000];
        int startMask = 1;
        int finishMask = 0;
        int breakMask = 0; // break after first or after last
        int leftMask = 0; // left or right break
        boolean left = true;
        for (int i=0; i<stateMachine.length; i++) {
            stateMachine[i].apply(i, bitNFA);
            if (stateMachine[i] == useFirst || stateMachine[i] == useLast) {
                startMask |= 1<<(i+1);
                finishMask |= 1<<i;
                if (left) leftMask |= 1<<i;
                left = !left;
            }
            if (stateMachine[i] == useFirst) breakMask |= 1<<i;
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
        result.append(String.format("static final short finishMask = 0x%x;\n", finishMask));
        result.append(String.format("static final short breakMask = 0x%x;\n", breakMask));
        result.append(String.format("static final short leftMask = 0x%x;\n", leftMask));
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
                useFirst,

                alnum,
                ws,
                useFirst,

                wsOrJoin,
                join,
                alnum,
                useLast,

                alnum,
                join,
                wsOrJoin,
                useFirst
        };

        System.out.println(generateSource(stateMachine));
    }
}
