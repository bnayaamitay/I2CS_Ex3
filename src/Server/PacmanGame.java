package Server;

public interface PacmanGame {
    int INIT = 0;
    int PLAY = 1;
    int PAUSE = 2;
    int DONE = 3;
    int ERR = -1;
    int STAY = 0;
    int UP = 1;
    int LEFT = 2;
    int DOWN = 3;
    int RIGHT = 4;

    /**
     * Initializes the game state with the given parameters.
     * * @param level the game level index.
     * @param map the string representation of the game map.
     * @param cyclic whether the map wrap-around is enabled.
     * @param seed seed for random elements (like ghost movement).
     * @param time the total game time allocated.
     * @param score the starting score.
     * @param lives the starting number of lives.
     * @return a string representing the initial state of the game.
     */
    String init(int level, String map, boolean cyclic, long seed, double time, int score, int lives);

    /**
     * Sends a move command to the Pac-Man in the specified direction.
     * * @param dir a string representing the direction (e.g., "0" for UP, "1" for RIGHT).
     * @return a string describing the game state after the move.
     */
    String move(int dir);

    /**
     * Starts and runs the main game loop or logic.
     */
    void play();

    /**
     * Retrieves the current game board as a 2D integer array.
     * * @param id the game/session identifier.
     * @return a 2D array where each cell represents a game object (Wall, Food, Empty, etc.).
     */
    int[][] getGame(int id);

    /**
     * Gets an array of ghost objects currently in the game.
     * * @param id the game/session identifier.
     * @return an array of GhostCL objects containing ghost positions and status.
     */
    GhostCL[] getGhosts(int id);

    /**
     * Gets the string representation of an entity's position.
     * * @param id the entity or game identifier.
     * @return a string formatted as "x,y,type".
     */
    String getPos(int id);

    /**
     * Retrieves the raw data string for a specific game or entity.
     * * @param id the game/session identifier.
     * @return a string containing internal game data.
     */
    String getData(int id);

    /**
     * Gets the current status of the game (e.g., Running, Game Over, Win).
     * * @return an integer representing the status code.
     */
    int getStatus();

    /**
     * Checks if the current map allows cyclic (wrap-around) movement.
     * * @return true if cyclic, false otherwise.
     */
    boolean isCyclic();

    /**
     * Terminates the game session.
     * * @param id the game/session identifier.
     * @return a summary string of the finished game.
     */
    String end(int id);

    /**
     * Gets the last character pressed on the keyboard.
     * * @return the character code currently registered.
     */
    Character getKeyChar();

    /**
     * Gets the current total score of the player.
     * * @return the current score.
     */
    int getScore();

    /**
     * Gets the number of remaining lives for the Pac-Man.
     * * @return the number of lives left.
     */
    int getLives();

}
