
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
        },
        MINUS("-") {
            int apply(int x, int y) { return x - y; }
        },
        TIMES("*") {
            int apply(int x, int y) { return x * y; }
        },
        DIVIDE("/") {
            int apply(int x, int y) { return x / y; }
        },
        PERCENT_OF("% of") {
            int apply(int x, int y) { return x * y / 100; }
        };

        private final String symbol;
        Operation(String symbol) {
            this.symbol = symbol;
        }
        @Override public String toString() {
            return symbol;
        }

        abstract int apply(int x, int y);
    }

    private static final Random random = new Random();

    private static final int[][] percentage_array_easy =
      { {80, 5}, {75, 4}, {60, 5}, {50, 2}, {40, 5}, {25, 4}, {20, 5}};

    private static final int[][] percentage_array_hard =
      { {90, 10}, {80, 5}, {75, 4}, {70, 10}, {60, 5}, {50, 2},
        {40, 5}, {30, 10}, {25, 4}, {20, 5}, {10, 10} };

    private final int lhs, rhs;
    private final Level level;
    private final Operation operation;

    private int givenAnswer;

    private Exercise(Level level, Operation operation) {
        this.level = level;
        this.operation = operation;

        // create lhs and rhs
        switch (operation) {
        case PLUS:
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

            break;
        case MINUS:
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
            break;
        case TIMES:
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
            break;
        case DIVIDE:
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
            break;
        case PERCENT_OF:
            int rand_percentage;
            switch (level) {
            case EASY:
                rand_percentage = uniform(0, 6);
                lhs = percentage_array_easy[rand_percentage][0];
                rhs = percentage_array_easy[rand_percentage][1] * uniform(2, 10);
                break;
            case MEDIUM:
                rand_percentage = uniform(0, 6);
                lhs = percentage_array_easy[rand_percentage][0];
                rhs = percentage_array_easy[rand_percentage][1] * uniform(2, 20);
                break;
            case HARD:
                rand_percentage = uniform(0, 10);
                lhs = percentage_array_hard[rand_percentage][0];
                rhs = percentage_array_hard[rand_percentage][1] * uniform(2, 20);
                break;
            case ESTIMATION:
                lhs = uniform(2, 99);
                rhs = uniform(111, 9999);
                break;
            default:
                throw new IllegalArgumentException();
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    protected final int uniform(int a, int b) {
        return a + random.nextInt(b - a);
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

    public static Exercise create(Level level, Operation operation) {
        return new Exercise(level, operation);
    }

    public static void main(String[] args) {
        System.out.print("Exercise: ");
        List<Exercise> exercises = new LinkedList<Exercise>();

        Operation operation;
        switch (args[0]) {
        case "add":
            operation = Operation.PLUS;
            break;
        case "sub":
            operation = Operation.MINUS;
            break;
        case "mult":
            operation = Operation.TIMES;
            break;
        case "div":
            operation = Operation.DIVIDE;
            break;
        case "perc":
            operation = Operation.PERCENT_OF;
            break;
        default:
            throw new IllegalArgumentException();
        }

        Console console = System.console();
        while (true) {
            Exercise e = Exercise.create(Level.EASY, operation);
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
