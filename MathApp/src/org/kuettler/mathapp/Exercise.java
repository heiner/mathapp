package org.kuettler.mathapp;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.io.Console;

class Exercise {
    public enum Level {
        EASY, MEDIUM, HARD, ESTIMATION;
        public final static String TAG =
            Level.class.getCanonicalName();

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public enum Operation {
        PLUS(" + ") {
            int apply(int x, int y) { return x + y; }

            Exercise newExercise(Level level) {
                int lhs, rhs;
                switch (level) {
                case EASY:
                    lhs = uniform(11, 99);
                    rhs = uniform(11, 99);
                    break;
                case MEDIUM:
                    lhs = uniform(11, 999);
                    rhs = uniform(111, 999);
                    break;
                case HARD:
                    lhs = uniform(111, 999);
                    rhs = uniform(1011, 9999);
                    break;
                case ESTIMATION:
                    lhs = uniform(10000, 999999);
                    rhs = uniform(50000, 999999);
                    break;
                default:
                    throw new IllegalArgumentException();
                }
                return new Exercise(level, this, lhs, rhs);
            }
        },
        MINUS(" − ") {
            int apply(int x, int y) { return x - y; }

            Exercise newExercise(Level level) {
                int lhs, rhs;
                switch (level) {
                case EASY:
                    lhs = uniform(30, 99);
                    rhs = uniform(5, lhs - 3);
                    break;
                case MEDIUM:
                    lhs = uniform(30, 499);
                    rhs = uniform(11, lhs - 11);
                    break;
                case HARD:
                    lhs = uniform(111, 999);
                    rhs = uniform(37, lhs - 37);
                    break;
                case ESTIMATION:
                    lhs = uniform(10000, 999999);
                    rhs = uniform(3333, lhs - 3333);
                    break;
                default:
                    throw new IllegalArgumentException();
                }
                return new Exercise(level, this, lhs, rhs);
            }
        },
        TIMES(" × ") {
            int apply(int x, int y) { return x * y; }

            Exercise newExercise(Level level) {
                int lhs, rhs;
                switch (level) {
                case EASY:
                    lhs = uniform(5, 20);
                    rhs = uniform(3, 10);
                    break;
                case MEDIUM:
                    lhs = uniform(5, 20);
                    rhs = uniform(9, 25);
                    break;
                case HARD:
                    lhs = uniform(11, 99);
                    rhs = uniform(11, 99);
                    break;
                case ESTIMATION:
                    lhs = uniform(500, 999);
                    rhs = uniform(500, 999);
                    break;
                default:
                    throw new IllegalArgumentException();
                }
                return new Exercise(level, this, lhs, rhs);
            }
        },
        DIVIDE(" ⁄ ") {
            int apply(int x, int y) { return x / y; }

            Exercise newExercise(Level level) {
                int lhs, rhs;
                switch (level) {
                case EASY:
                    rhs = uniform(3, 10);
                    lhs = rhs * uniform(5, 20);
                    break;
                case MEDIUM:
                    rhs = uniform(3, 15);
                    lhs = rhs * uniform(5, 40);
                    break;
                case HARD:
                    rhs = uniform(11, 99);
                    lhs = rhs * uniform(11, 99);
                    break;
                case ESTIMATION:
                    rhs = uniform(500, 999);
                    lhs = rhs * uniform(500, 999);
                    break;
                default:
                    throw new IllegalArgumentException();
                }
                return new Exercise(level, this, lhs, rhs);
            }
        },
        PERCENT_OF("% of ") {
            int apply(int x, int y) { return x * y / 100; }

            Exercise newExercise(Level level) {
                int lhs, rhs, random_index;
                switch (level) {
                case EASY:
                    random_index = uniform(0, 6);
                    lhs = percentage_array_easy[random_index][0];
                    rhs = percentage_array_easy[random_index][1] * uniform(2, 10);
                    break;
                case MEDIUM:
                    random_index = uniform(0, 6);
                    lhs = percentage_array_easy[random_index][0];
                    rhs = percentage_array_easy[random_index][1] * uniform(2, 20);
                    break;
                case HARD:
                    random_index = uniform(0, 10);
                    lhs = percentage_array_hard[random_index][0];
                    rhs = percentage_array_hard[random_index][1] * uniform(2, 20);
                    break;
                case ESTIMATION:
                    lhs = uniform(2, 99);
                    rhs = uniform(111, 9999);
                    break;
                default:
                    throw new IllegalArgumentException();
                }
                return new Exercise(level, this, lhs, rhs);
            }
        };

        private static final int[][] percentage_array_easy =
            { {80, 5}, {75, 4}, {60, 5}, {50, 2}, {40, 5}, {25, 4}, {20, 5}};

        private static final int[][] percentage_array_hard =
            { {90, 10}, {80, 5}, {75, 4}, {70, 10}, {60, 5}, {50, 2},
              {40, 5}, {30, 10}, {25, 4}, {20, 5}, {10, 10} };

        private static final Random random = new Random();

        private static final int uniform(int a, int b) {
            return a + random.nextInt(b - a);
        }

        public static Exercise newRandomExercise(Level level) {
            return randomOperation().newExercise(level);
        }

        public static Operation randomOperation() {
            Operation[] ops = values();
            return ops[random.nextInt(ops.length)];
        }

        private final String symbol;
        Operation(String symbol) {
            this.symbol = symbol;
        }
        @Override public String toString() {
            return symbol;
        }

        abstract int apply(int x, int y);
        abstract Exercise newExercise(Level level);
    }

    private final Level level;
    private final Operation operation;
    private final int lhs, rhs;

    private int givenAnswer;

    private Exercise(Level level, Operation operation, int lhs, int rhs) {
        this.level = level;
        this.operation = operation;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    protected int solution() {
        return operation.apply(lhs, rhs);
    }

    private static final Map<Character, Integer> suffixes =
        new HashMap<Character, Integer>();
    static {
        suffixes.put('k', 1000);
        suffixes.put('m', 1000*1000);
        suffixes.put('b', 1000*1000*1000);
    }

    boolean answer(String s) {
        Character lastChar = Character.toLowerCase(s.charAt(s.length() - 1));
        int multiplier = 1;
        if (suffixes.containsKey(lastChar)) {
            multiplier = suffixes.get(lastChar);
            s = s.substring(0, s.length() - 1);
        }
        float a = multiplier * Float.valueOf(s);

        givenAnswer = Math.round(a);
        return givenAnswerCorrect();
    }

    public int getGivenAnswer() {
        return givenAnswer;
    }

    public boolean givenAnswerCorrect() {
        if (givenAnswer == solution()) {
            return true;
        }
        if (level == Level.ESTIMATION) {
            if (0.88*solution() <= givenAnswer && givenAnswer <= 1.12*solution()) {
                return true;
            }
        }
        return false;
    }

    public String question() {
        /*
          // for estimation, have "k" or smth.:
          Add: 211K + 221K
          Minus: 531K - 21K
          Div: 212K / 311
          Perc: 92% of 2.42K
         */
        if (level != Level.ESTIMATION || operation == Operation.TIMES) {
            return String.format("%d%s%d = ", lhs, operation, rhs);
        }
        switch (operation) {
        case PLUS: case MINUS:
            return String.format("%dK%s%dK = ", lhs/1000, operation, rhs/1000);
        case DIVIDE:
            return String.format("%dK%s%d = ", lhs/1000, operation, rhs);
        case PERCENT_OF:
            return String.format("%d%s%.2fK = ", lhs, operation, rhs/1000.0);
        default:
            return null;
        }
    }

    public static void main(String[] args) {
        Level level;
        Operation operation;
        try {
            if (args.length != 2) {
                throw new IllegalArgumentException();
            }
            level = Level.valueOf(args[0].toUpperCase());
            operation = Operation.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException exception) {
            System.err.print(Exercise.class.getSimpleName() + " {");
            for (Level lev : Level.values()) {
                System.err.print(" " + lev.name().toLowerCase());
            }
            System.err.print(" } {");
            for (Operation op : Operation.values()) {
                System.err.print(" " + op.name().toLowerCase());
            }
            System.err.println(" }");
            return;
        }

        List<Exercise> exercises = new LinkedList<Exercise>();
        int correctCount = 0;
        int wrongCount = 0;

        Console console = System.console();
        do {
            Exercise e = operation.newExercise(level);
            System.out.print(e.question());
            if (e.answer(console.readLine())) {
                ++correctCount;
            } else {
                ++wrongCount;
            }
            exercises.add(e);
        } while (exercises.size() < 3);
        for (Exercise e : exercises) {
            System.out.println(e.question() + e.solution() +
                               ", you gave " + e.getGivenAnswer() +
                               (e.givenAnswerCorrect() ? ", correct" : ", wrong") );
        }
        System.out.format("%d - %d = %d points%n", correctCount, wrongCount,
                          Math.max(0, correctCount - wrongCount));
    }
}
