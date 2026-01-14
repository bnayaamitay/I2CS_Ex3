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

    String init(int level, String map, boolean cyclic, long seed, double time, int score, int lives);
    String move(int dir);
    void play();
    int[][] getGame(int id);
    GhostCL[] getGhosts(int id);
    String getPos(int id);
    String getData(int id);
    int getStatus();
    boolean isCyclic();
    String end(int id);
    Character getKeyChar();
    int getScore();
    int getLives();

}
