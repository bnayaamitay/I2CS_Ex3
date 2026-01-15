package Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game implements PacmanGame {
    private Map _map;
    private Pacman _pacman;
    private ArrayList<Ghost> _ghosts;
    private int _score = 0;
    private int _lives = 3;
    private int _status = INIT;
    private int _foodCount = 0;
    private ArrayList<Pixel2D> _ghostStartingPositions;
    private long _levelStartTime;
    private int _moveCounter;
    private long _eatableTimer = 0;
    private Random _rand;

    public static final int WALL = 1;
    public static final int FOOD = 3;
    public static final int EMPTY = 0;
    public static final int PLAY = 0;
    public static final int DONE = 1;
    public static final int INIT = -1;
    public static final int CHERRY = 4;
    public static final int UP=1, RIGHT=2, DOWN=3, LEFT=4, STAY=0;

    /**
     * Initializes a new instance of the Game class.
     */
    public Game() {
        _ghosts = new ArrayList<>();
        _ghostStartingPositions = new ArrayList<>();
        _rand = new Random();
    }

    @Override
    public String init(int level, String mapStr, boolean cyclic, long seed, double time, int score, int lives) {
        this._score = score;
        this._lives = lives;
        this._foodCount = 0;
        this._eatableTimer = 0;
        this._rand = new Random(seed);
        String[] lines = mapStr.split("\n");
        int h = lines.length;
        int w = lines[0].length();
        int[][] board = new int[w][h];
        _ghosts.clear();
        _ghostStartingPositions.clear();
        for (int y = 0; y < h; y++) {
            String line = lines[y];
            for (int x = 0; x < w; x++) {
                char c = 'W';
                if (x < line.length()) c = line.charAt(x);
                int val = EMPTY;
                if (c == '#' || c == 'W') val = WALL;
                else if (c == '.') {
                    val = FOOD;
                    _foodCount++;
                }
                else if (c == 'C') {
                    val = CHERRY;
                    _foodCount++;
                }
                else if (c == 'P') {
                    val = EMPTY;
                    _pacman = new Pacman(new Index2D(x, y));
                }
                else if (c == 'G') {
                    val = EMPTY;
                    _ghostStartingPositions.add(new Index2D(x, y));
                }
                board[x][y] = val;
            }
        }
        _map = new Map(board);
        _map.setCyclic(cyclic);
        _status = PLAY;
        _levelStartTime = System.currentTimeMillis();
        _moveCounter = 0;
        return "Initialized";
    }

    /**
     * Spawns the ghosts at their starting positions based on the requested count.
     */
    public void setDesiredGhosts(int numGhosts) {
        _ghosts.clear();
        if (_ghostStartingPositions.isEmpty()) return;
        for (int i = 0; i < numGhosts; i++) {
            Pixel2D pos = _ghostStartingPositions.get(i % _ghostStartingPositions.size());
            _ghosts.add(new Ghost(new Index2D(pos.getX(), pos.getY()), 0, i, 1.0));
        }
    }

    @Override
    public String move(int dir) {
        if (_status != PLAY) return "Not Playing";
        if (_eatableTimer > 0) {
            _eatableTimer -= 100;
        }
        movePacman(dir);
        moveGhosts();
        if (_foodCount <= 0 || _lives <= 0) {
            _status = DONE;
        }
        return "Move Done";
    }

    /**
     * Updates Pacman's position and handles collisions with food or cherries.
     */
    private void movePacman(int dir) {
        if (dir == STAY) return;
        Pixel2D next = getNextPos(_pacman.getPos(), dir);
        int pixel = _map.getPixel(next);
        if (pixel != WALL) {
            _pacman.setPos(next);
            if (pixel == FOOD || pixel == CHERRY) {
                if (pixel == CHERRY) {
                    _score += 10;
                    _eatableTimer = 7000;
                } else {
                    _score++;
                }
                _foodCount--;
                _map.setPixel(next, EMPTY);
            }
        }
    }

    /**
     * Logic for ghost behavior including AI movement types (BFS, Smart, Random)
     * and collision detection with Pacman.
     */
    private void moveGhosts() {
        long timeSinceStart = System.currentTimeMillis() - _levelStartTime;
        boolean waitTimeOver = timeSinceStart > 5000;
        boolean forceExitMode = timeSinceStart > 10000;
        boolean isRestingStep = false;
        if (waitTimeOver) {
            _moveCounter++;
            if (_moveCounter % 6 == 0) {
                isRestingStep = true;
            }
        }
        boolean ghostsShouldMove = waitTimeOver && !isRestingStep;
        for (int i = 0; i < _ghosts.size(); i++) {
            Ghost g = _ghosts.get(i);
            if (ghostsShouldMove) {
                int dir = -1;
                if (forceExitMode && isInGhostHouse(g)) {
                    dir = getBFSDir(g, _pacman.getPos());
                    if (dir == -1) dir = getRandomDir(g);
                }
                else if (i == 0 && _rand.nextDouble() < 0.25) {
                    dir = getSmartDir(g);
                } else {
                    dir = getRandomDir(g);
                }
                if (dir != -1) {
                    g.setPos(getNextPos(g.getPixelPos(), dir));
                }
            }
            if (g.getPixelPos().equals(_pacman.getPos())) {
                if (_eatableTimer > 0) {
                    _score += 100;
                    Pixel2D startPos = _ghostStartingPositions.get(i % _ghostStartingPositions.size());
                    g.setPos(new Index2D(startPos.getX(), startPos.getY()));
                } else {
                    _lives = 0;
                    _status = DONE;
                    return;
                }
            }
        }
    }

    /**
     * Returns the remaining time Pacman is in "Power-up" mode.
     */
    public long getEatableTime() {
        return _eatableTimer;
    }

    /**
     * Checks if a ghost is currently inside the central ghost house area.
     */
    private boolean isInGhostHouse(Ghost g) {
        int centerX = _map.getWidth() / 2;
        int centerY = _map.getHeight() / 2;
        Pixel2D center = new Index2D(centerX, centerY);
        return g.getPixelPos().distance2D(center) < 6;
    }

    /**
     * Uses a Breadth-First Search algorithm to find the next move
     * on the shortest path to a target.
     */
    private int getBFSDir(Ghost g, Pixel2D target) {
        Pixel2D[] path = _map.shortestPath(g.getPixelPos(), target, WALL);
        if (path != null && path.length > 1) {
            Pixel2D nextStep = path[1];
            int[] dirs = {UP, DOWN, LEFT, RIGHT};
            for (int d : dirs) {
                if (getNextPos(g.getPixelPos(), d).equals(nextStep)) {
                    return d;
                }
            }
        }
        return -1;
    }

    /**
     * Calculates the best direction for a ghost based on a greedy
     * approach toward Pacman's current position.
     */
    private int getSmartDir(Ghost g) {
        Pixel2D gPos = g.getPixelPos();
        Pixel2D pPos = _pacman.getPos();
        int bestDir = -1;
        double minDst = Double.MAX_VALUE;
        int[] dirs = {UP, DOWN, LEFT, RIGHT};
        for (int d : dirs) {
            Pixel2D next = getNextPos(gPos, d);
            if (_map.getPixel(next) != WALL) {
                double dist = next.distance2D(pPos);
                if (dist < minDst) {
                    minDst = dist;
                    bestDir = d;
                }
            }
        }
        return bestDir;
    }

    /**
     * Selects a random valid direction for ghost movement.
     */
    private int getRandomDir(Ghost g) {
        Pixel2D gPos = g.getPixelPos();
        List<Integer> validMoves = new ArrayList<>();
        int[] dirs = {UP, DOWN, LEFT, RIGHT};
        for (int d : dirs) {
            if (_map.getPixel(getNextPos(gPos, d)) != WALL) validMoves.add(d);
        }
        if (validMoves.isEmpty()) return -1;
        return validMoves.get(_rand.nextInt(validMoves.size()));
    }

    /**
     * Calculates the target coordinates based on a starting position and direction,
     * with support for map wrapping (cyclic).
     */
    private Pixel2D getNextPos(Pixel2D curr, int dir) {
        int x = curr.getX();
        int y = curr.getY();
        if (dir == UP) y++;
        if (dir == DOWN) y--;
        if (dir == RIGHT) x++;
        if (dir == LEFT) x--;
        if (_map.isCyclic()) {
            x = (x + _map.getWidth()) % _map.getWidth();
            y = (y + _map.getHeight()) % _map.getHeight();
            if (x < 0) x += _map.getWidth();
            if (y < 0) y += _map.getHeight();
        }
        return new Index2D(x, y);
    }

    @Override public int getLives() { return _lives; }
    @Override public int getScore() { return _score; }
    @Override public int[][] getGame(int id) {
        int[][] b = new int[_map.getWidth()][_map.getHeight()];
        for(int x=0;x<_map.getWidth();x++) for(int y=0;y<_map.getHeight();y++) b[x][y]=_map.getPixel(x,y);
        return b;
    }
    @Override public GhostCL[] getGhosts(int id) {
        GhostCL[] arr = new GhostCL[_ghosts.size()];
        return _ghosts.toArray(arr);
    }
    @Override public String getPos(int id) { return _pacman.getPos().getX()+","+_pacman.getPos().getY()+",0"; }
    @Override public String getData(int id) { return ""; }
    @Override public int getStatus() { return _status; }
    @Override public boolean isCyclic() { return true; }
    @Override public void play() {}
    @Override public String end(int id) { return "Score: " + _score; }
    @Override public Character getKeyChar() { return null; }
}