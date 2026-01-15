package Server;

public class Algo implements PacManAlgo {
    private boolean _manualMode = false;
    private ManualAlgo _manualAlgo = new ManualAlgo();
    private Pixel2D _currentTarget = null;
    private int _lastDir = 0;

    /**
     * Returns the name/info of the algorithm.
     */
    @Override
    public String getInfo() {
        return "PACMAN SMART ALGO";
    }

    /**
     * Main logic loop for Pacman movement.
     * * PSEUDOCODE:
     * 1. Check if the 'Enter' key was pressed to toggle between Manual and Auto modes.
     * 2. If in Manual Mode: Fetch direction from ManualAlgo and apply the axis/direction fix.
     * 3. If in Auto Mode:
     * a. Initialize the game board and a navigation map with cyclic (wrap-around) support.
     * b. Parse Pacman's current coordinate from the game state.
     * c. Identify ghosts and determine if Pacman is "Powered Up" (edible timer > 1s).
     * d. Block the Ghost House entrance to prevent Pacman from getting trapped inside.
     * e. If NOT powered up: Treat ghost positions and their immediate neighbors as walls in the distance map.
     * f. Calculate distances from Pacman to all reachable points using BFS.
     * g. If blocking ghosts makes all food unreachable, fallback to a map where ghosts are not treated as walls.
     * h. Safety Check: If a ghost is within 3 steps, prioritize the 'bestEscapeDir' logic.
     * i. Hunting Logic: If powered up, prioritize moving toward the nearest edible ghost outside the house.
     * j. Targeting Logic: If the current target is gone or reached, find the nearest food or cherry.
     * k. Pathfinding: Use BFS to find the shortest path to the target and return the direction of the first step.
     */
    @Override
    public int move(PacmanGame game) {
        Character cmd = MyMain.getCMD();
        if (cmd != null && (cmd == '\n' || cmd == '\r')) {
            _manualMode = !_manualMode;
        }

        if (_manualMode) {
            int dir = _manualAlgo.move(game);
            return fixManualDir(dir);
        }

        int[][] board = game.getGame(0);
        int w = board.length, h = board[0].length;
        Map map = new Map(board);
        map.setCyclic(true);

        String posStr = game.getPos(0).toString();
        String[] t = posStr.split(",");
        Pixel2D pacmanPos = new Index2D(Integer.parseInt(t[0]), Integer.parseInt(t[1]));

        GhostCL[] ghosts = game.getGhosts(0);
        long remainingPowerTime = ((Game)game).getEatableTime();
        boolean isSafeToHunt = remainingPowerTime > 1000;

        map.setPixel(w / 2, h / 2 + 1, Game.WALL);

        if (!isSafeToHunt) {
            for (GhostCL g : ghosts) {
                Pixel2D gPos = getPos(g.getPos(0).toString());
                if (isInGhostHouse(gPos.getX(), gPos.getY(), w, h)) continue;
                map.setPixel(gPos.getX(), gPos.getY(), Game.WALL);
                blockNeighbors(map, gPos, w, h, Game.WALL);
            }
        }

        Map2D dists = map.allDistance(pacmanPos, Game.WALL);
        if (dists == null || !isAnyFoodReachable(board, dists)) {
            map = new Map(board);
            map.setCyclic(true);
            map.setPixel(w / 2, h / 2 + 1, Game.WALL);
            dists = map.allDistance(pacmanPos, Game.WALL);
        }

        if (dists == null) return Game.STAY;

        if (!isSafeToHunt) {
            Pixel2D threat = getNearestThreat(ghosts, dists, 3);
            if (threat != null) {
                _currentTarget = null;
                int escapeDir = bestEscapeDir(pacmanPos, threat, map);
                _lastDir = escapeDir;
                return escapeDir;
            }
        }

        if (isSafeToHunt) {
            Pixel2D ghostTarget = findNearestEdibleGhost(ghosts, dists, w, h);
            if (ghostTarget != null && dists.getPixel(ghostTarget.getX(), ghostTarget.getY()) <= 3) {
                _currentTarget = ghostTarget;
            }
        }

        if (_currentTarget != null) {
            int p = board[_currentTarget.getX()][_currentTarget.getY()];
            if (p != Game.FOOD && p != Game.CHERRY && !isGhostAtPos(ghosts, _currentTarget)) {
                _currentTarget = null;
            }
        }

        if (_currentTarget == null) {
            _currentTarget = findNearestTarget(board, dists);
        }

        if (_currentTarget != null) {
            Pixel2D[] path = map.shortestPath(pacmanPos, _currentTarget, Game.WALL);
            if (path != null && path.length > 1) {
                int dir = getDirection(pacmanPos, path[1], w, h);
                _lastDir = dir;
                return dir;
            }
        }

        return Game.STAY;
    }

    /**
     * Checks if any ghost is currently located at the specified position.
     */
    private boolean isGhostAtPos(GhostCL[] ghosts, Pixel2D pos) {
        for (GhostCL g : ghosts) {
            if (getPos(g.getPos(0).toString()).equals(pos)) return true;
        }
        return false;
    }

    /**
     * Finds the closest ghost within a specific distance threshold.
     */
    private Pixel2D getNearestThreat(GhostCL[] ghosts, Map2D dists, int threshold) {
        Pixel2D nearest = null;
        int minD = Integer.MAX_VALUE;
        for (GhostCL g : ghosts) {
            Pixel2D gPos = getPos(g.getPos(0).toString());
            int d = dists.getPixel(gPos.getX(), gPos.getY());
            if (d != -1 && d <= threshold && d < minD) {
                minD = d;
                nearest = gPos;
            }
        }
        return nearest;
    }

    /**
     * Marks the four adjacent neighbors of a pixel as walls in the navigation map.
     */
    public void blockNeighbors(Map map, Pixel2D p, int w, int h, int wall) {
        int[] dx = {1, -1, 0, 0}, dy = {0, 0, 1, -1};
        for (int i = 0; i < 4; i++) {
            map.setPixel((p.getX() + dx[i] + w) % w, (p.getY() + dy[i] + h) % h, wall);
        }
    }

    /**
     * Determines if the given coordinates fall within the ghost spawn area (Ghost House).
     */
    public boolean isInGhostHouse(int x, int y, int w, int h) {
        return x >= w/2-3 && x <= w/2+3 && y >= h/2-2 && y <= h/2+2;
    }

    /**
     * Scans the board to find the closest reachable Food or Cherry item.
     */
    private Pixel2D findNearestTarget(int[][] board, Map2D dists) {
        int min = Integer.MAX_VALUE;
        Pixel2D best = null;
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[0].length; y++) {
                if (board[x][y] == Game.FOOD || board[x][y] == Game.CHERRY) {
                    int d = dists.getPixel(x, y);
                    if (d != -1 && d < min) {
                        min = d;
                        best = new Index2D(x, y);
                    }
                }
            }
        }
        return best;
    }

    /**
     * Determines the safest direction to move by maximizing the distance to a specific ghost.
     */
    private int bestEscapeDir(Pixel2D pacman, Pixel2D ghost, Map map) {
        int bestDir = Game.STAY;
        double maxDist = -1;
        int[] dirs = {Game.UP, Game.DOWN, Game.LEFT, Game.RIGHT};
        for (int d : dirs) {
            Pixel2D next = getNextPos(pacman, d, map);
            if (map.getPixel(next.getX(), next.getY()) != Game.WALL) {
                double dist = next.distance2D(ghost);
                if (dist > maxDist) {
                    maxDist = dist;
                    bestDir = d;
                }
            }
        }
        return bestDir;
    }

    /**
     * Calculates the resulting position when moving in a specific direction, including cyclic wrap-around.
     */
    public Pixel2D getNextPos(Pixel2D p, int d, Map map) {
        int x = p.getX(), y = p.getY();
        if (d == Game.UP) y++;
        if (d == Game.DOWN) y--;
        if (d == Game.RIGHT) x++;
        if (d == Game.LEFT) x--;
        return new Index2D((x + map.getWidth()) % map.getWidth(), (y + map.getHeight()) % map.getHeight());
    }

    /**
     * Maps the movement between two adjacent pixels into a Game direction constant (UP, DOWN, LEFT, RIGHT).
     */
    public int getDirection(Pixel2D curr, Pixel2D next, int w, int h) {
        int dx = next.getX() - curr.getX();
        int dy = next.getY() - curr.getY();
        if (Math.abs(dx) > 1) dx = (dx > 0) ? -1 : 1;
        if (Math.abs(dy) > 1) dy = (dy > 0) ? -1 : 1;
        if (dx == 1) return Game.RIGHT;
        if (dx == -1) return Game.LEFT;
        if (dy == 1) return Game.UP;
        if (dy == -1) return Game.DOWN;
        return Game.STAY;
    }

    /**
     * Parses a coordinate string formatted as "x,y,z" into a Pixel2D object.
     */
    public Pixel2D getPos(String s) {
        String[] t = s.split(",");
        return new Index2D(Integer.parseInt(t[0]), Integer.parseInt(t[1]));
    }

    /**
     * Identifies the closest edible ghost that is currently outside the ghost house.
     */
    private Pixel2D findNearestEdibleGhost(GhostCL[] ghosts, Map2D dists, int w, int h) {
        Pixel2D best = null;
        int min = Integer.MAX_VALUE;
        for (GhostCL g : ghosts) {
            Pixel2D gPos = getPos(g.getPos(0).toString());
            if (isInGhostHouse(gPos.getX(), gPos.getY(), w, h)) continue;
            int d = dists.getPixel(gPos.getX(), gPos.getY());
            if (d != -1 && d < min) {
                min = d;
                best = gPos;
            }
        }
        return best;
    }

    /**
     * Checks if there is any food item on the board currently reachable by Pacman.
     */
    public boolean isAnyFoodReachable(int[][] board, Map2D dists) {
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[0].length; y++) {
                if ((board[x][y] == Game.FOOD || board[x][y] == Game.CHERRY) && dists.getPixel(x, y) != -1) return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the algorithm is currently in Automatic mode.
     */
    public boolean isAuto() {
        return !_manualMode;
    }

    /**
     * Compensates for axis/control differences by remapping manual input directions.
     */
    public int fixManualDir(int dir) {
        if (dir == 1) return 3;
        if (dir == 3) return 1;
        if (dir == 4) return 2;
        if (dir == 2) return 4;
        return dir;
    }
}