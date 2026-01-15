package Server;

/**
 * This interface defines the core AI logic for a Pac-Man controller.
 * Any class implementing this interface must provide a strategy for movement.
 */
public interface PacManAlgo {

    /**
     * Calculates and returns the next direction for the Pac-Man to move.
     * * @param game the current state of the game, including positions and map.
     * @return an integer representing the chosen direction (e.g., 0:UP, 1:RIGHT, 2:DOWN, 3:LEFT).
     */
    int move(PacmanGame game);

    /**
     * Retrieves basic information or metadata about this algorithm implementation.
     * * @return a string containing details about the algorithm version or logic.
     */
    String getInfo();
}
