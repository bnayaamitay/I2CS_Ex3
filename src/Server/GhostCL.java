package Server;

/**
 * This interface defines the behavior and properties of a Ghost in the game.
 * It includes state constants and methods for tracking position and status.
 */
public interface GhostCL {

    /** Initial state of the ghost. */
    int INIT = 0;

    /** Ghost is active and moving in the game. */
    int PLAY = 1;

    /** Ghost movement is paused. */
    int PAUSE = 2;

    /** Movement strategy: Simple random walk. */
    int RANDOM_WALK0 = 10;

    /** Movement strategy: Advanced random walk. */
    int RANDOM_WALK1 = 11;

    /** Movement strategy: Greedy Shortest Path towards a target. */
    int GREEDY_SP = 12;

    /**
     * Gets the specific type/personality of the ghost.
     * @return an integer representing the ghost type.
     */
    int getType();

    /**
     * Retrieves the current position of the ghost.
     * @param id the ghost identifier.
     * @return a string representation of the position (e.g., "x,y").
     */
    String getPos(int id);

    /**
     * Calculates how much time is left while the ghost is in its "eatable" (scared) mode.
     * @param id the ghost identifier.
     * @return the remaining time in seconds; 0 if the ghost is in normal mode.
     */
    double remainTimeAsEatable(int id);

    /**
     * Returns the current operational status of the ghost.
     * @return an integer status code (INIT, PLAY, or PAUSE).
     */
    int getStatus();

    /**
     * Provides a summary of the ghost's current data and attributes.
     * @return a string containing the ghost's information.
     */
    String getInfo();
}
