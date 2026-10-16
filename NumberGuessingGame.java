import java.util.Random;
import java.util.Scanner;

/**
 * OASIS INFOBYTE - Java Development
 * Task: Number Guessing Game
 */
public class NumberGuessingGame {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        System.out.println("====================================");
        System.out.println("       NUMBER GUESSING GAME");
        System.out.println("====================================");
        int gamesPlayed = 0;
        int totalPoints = 0;
        boolean playAgain = true;

        while (playAgain) {
            int maxNumber = chooseDifficulty();
            int secretNumber = RANDOM.nextInt(maxNumber) + 1;
            int maxAttempts = attemptsFor(maxNumber);
            int attempts = 0;
            boolean guessed = false;

            System.out.printf("\nI selected a number from 1 to %d.%n", maxNumber);
            System.out.printf("You have %d attempts. Good luck!%n", maxAttempts);

            while (attempts < maxAttempts) {
                int guess = readInteger("Enter your guess: ");
                if (guess < 1 || guess > maxNumber) {
                    System.out.printf("Please enter a number between 1 and %d.%n", maxNumber);
                    continue;
                }

                attempts++;
                if (guess == secretNumber) {
                    guessed = true;
                    int points = Math.max(10, (maxAttempts - attempts + 1) * 10);
                    totalPoints += points;
                    gamesPlayed++;
                    System.out.printf("Correct! You guessed it in %d attempt(s). You earned %d points.%n",
                            attempts, points);
                    break;
                } else if (guess < secretNumber) {
                    System.out.println("Too low. Try a higher number.");
                } else {
                    System.out.println("Too high. Try a lower number.");
                }
                System.out.printf("Attempts remaining: %d%n", maxAttempts - attempts);
            }

            if (!guessed) {
                gamesPlayed++;
                System.out.printf("Out of attempts! The number was %d.%n", secretNumber);
            }

            System.out.printf("Score so far: %d points across %d game(s).%n", totalPoints, gamesPlayed);
            playAgain = readYesNo("Would you like to play again? (y/n): ");
        }

        System.out.println("\nThanks for playing!");
        System.out.printf("Final score: %d points across %d game(s).%n", totalPoints, gamesPlayed);
        INPUT.close();
    }

    private static int chooseDifficulty() {
        System.out.println("\nChoose difficulty:");
        System.out.println("1. Easy   (1-50, 10 attempts)");
        System.out.println("2. Medium (1-100, 7 attempts)");
        System.out.println("3. Hard   (1-500, 9 attempts)");
        while (true) {
            int choice = readInteger("Your choice (1-3): ");
            switch (choice) {
                case 1: return 50;
                case 2: return 100;
                case 3: return 500;
                default: System.out.println("Please choose 1, 2, or 3.");
            }
        }
    }

    private static int attemptsFor(int maxNumber) {
        if (maxNumber == 50) return 10;
        if (maxNumber == 100) return 7;
        return 9;
    }

    private static int readInteger(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = INPUT.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    private static boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String answer = INPUT.nextLine().trim().toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) return true;
            if (answer.equals("n") || answer.equals("no")) return false;
            System.out.println("Please enter y or n.");
        }
    }
}
