
/*
 * Exercise game
 */

import java.util.List;
import java.util.LinkedList;
import java.util.Random;
import java.io.Console;

class Exercise {
    public enum Level {
        EASY, MEDIUM, HARD, ESTIMATION;
    }

    public enum Operation {
        PLUS("+") {
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
        MINUS("-") {
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
                    rhs = uniform(111, lhs - 111);
                    break;
                case ESTIMATION:
                    lhs = uniform(10000, 999999);
                    rhs = uniform(50000, lhs - 50000);
                    break;
                default:
                    throw new IllegalArgumentException();
                }
                return new Exercise(level, this, lhs, rhs);
            }
        },
        TIMES("*") {
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
        DIVIDE("/") {
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
        PERCENT_OF("% of") {
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
            Operation[] ops = values();
            return ops[random.nextInt(ops.length)].newExercise(level);
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

    private boolean answer(String s) {
        givenAnswer = Integer.parseInt(s);
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
        return String.format("%d %s %d = ", lhs, operation, rhs);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.print("Please give one argument, one of:");
            for (Operation op : Operation.values()) {
                System.err.print(" " + op.name().toLowerCase());
            }
            System.err.println();
            return;
        }

        Operation operation;
        try {
            operation = Operation.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException exception) {
            System.err.println("Unknown operation: " + args[0]);
            return;
        }

        List<Exercise> exercises = new LinkedList<Exercise>();

        Console console = System.console();
        while (true) {
            Exercise e = operation.newExercise(Level.EASY);
            System.out.print(e.question());
            if (e.answer(console.readLine())) {
                //System.out.println("Correct!");
            } else {
                //System.out.println("Wrong.");
            }
            exercises.add(e);

            if (exercises.size() >= 3) {
                break;
            }
        }
        for ( Exercise e : exercises) {
            System.out.println(e.question() + e.solution() +
                               ", you gave " + e.getGivenAnswer() +
                               (e.givenAnswerCorrect() ? ", correct" : ", wrong") );
        }
    }
}
