package assignments;

import exe.ex3.game.Game;
import exe.ex3.game.GhostCL;
import exe.ex3.game.PacManAlgo;
import exe.ex3.game.PacmanGame;

import java.awt.*;

public class Ex3Algo implements PacManAlgo {
    private int _count;
    private boolean _manualMode = false;
    private ManualAlgo _manualAlgo = new ManualAlgo();

    public Ex3Algo() {
        _count = 0;
    }

    @Override
    public String getInfo() {
        return "PACMAN ALGO - SMART SURVIVOR\n" +
                "*** PRESS [ENTER] TO TOGGLE MANUAL / AUTO MODE ***\n\n" +
                "1. GREEDY NAVIGATION & CONNECTIVITY:\n" +
                "   Employs BFS to find the closest pellets. If multiple targets have the same \n" +
                "   distance, it prioritizes cells with more adjacent food to maintain flow.\n\n" +
                "2. GHOST HOUSE BYPASS:\n" +
                "   Implements a virtual wall at (w/2, h/2 + 1) to prevent Pacman from \n" +
                "   entering the respawn zone, ensuring he stays on the active board.\n\n" +
                "3. ADAPTIVE SAFETY RADIUS:\n" +
                "   Default safety buffer is 8 steps. When 5 or fewer pellets remain, \n" +
                "   it drops to 3 steps, allowing an aggressive sprint to finish the level.\n\n" +
                "4. TACTICAL RESOURCE MANAGEMENT:\n" +
                "   Conserves Power Pills. Pacman only deviates to grab a weapon if a ghost \n" +
                "   is within 13 steps and the pill is within 7, or if a threat is within 10.\n\n" +
                "5. TRAP & PINCER DETECTION:\n" +
                "   Scans for threats within 10 steps. If a pincer movement is detected where \n" +
                "   only one safe exit (no ghost within 4 steps) remains, survival is prioritized.\n\n" +
                "6. SAFE COUNTER-ATTACK:\n" +
                "   Actively hunts edible ghosts within a 15-step range, provided they are \n" +
                "   outside the restricted ghost house area.\n\n" +
                "7. EMERGENCY EVASION:\n" +
                "   If a ghost closes within 3 steps, Pacman triggers a distance-maximization \n" +
                "   protocol to find the safest cell that leads to open paths.";
    }

    @Override
    public int move(PacmanGame game) {
        Character cmd = Ex3Main.getCMD();
        if (cmd != null && (cmd == '\n' || cmd == '\r' || cmd == 10 || cmd == 13)) {
            _manualMode = !_manualMode;
        }

        if (_manualMode) {
            return _manualAlgo.move(game);
        }

        int[][] board = game.getGame(0);
        int w = board.length;
        int h = board[0].length;
        Map map = new Map(board);
        map.setCyclic(GameInfo.CYCLIC_MODE);

        int blueColor = Game.getIntColor(Color.BLUE, 0);
        int pinkColor = Game.getIntColor(Color.PINK, 0);
        int greenColor = Game.getIntColor(Color.GREEN, 0);

        map.setPixel(w / 2, h / 2 + 1, blueColor);

        GhostCL[] ghosts = game.getGhosts(0);
        String posString = game.getPos(0).toString();
        Pixel2D pacmanPos = getPos(posString);

        Map2D cleanDistances = map.allDistance(pacmanPos, blueColor);
        if (cleanDistances == null) return validMove(pacmanPos, map, blueColor);

        int foodLeft = countRemainingFood(map, pinkColor);
        int safetyRadius = (foodLeft <= 5) ? 3 : 8;

        boolean isPoweredUp = false;
        boolean detectedDanger = false;
        int minGhostDist = Integer.MAX_VALUE;

        for (int i = 0; i < ghosts.length; i++) {
            if (ghosts[i].remainTimeAsEatable(0) > 20) {
                isPoweredUp = true;
            }

            if (ghosts[i].remainTimeAsEatable(0) <= 0) {
                Pixel2D gPos = getPos(ghosts[i].getPos(0).toString());
                int walkingDist = cleanDistances.getPixel(gPos);

                if (walkingDist != -1 && walkingDist < safetyRadius) {
                    setSafetyZone(map, gPos, blueColor, pacmanPos);
                    detectedDanger = true;
                    if (walkingDist < minGhostDist) {
                        minGhostDist = walkingDist;
                    }
                } else {
                    map.setPixel(gPos, blueColor);
                }
            }
        }

        Map2D distances = detectedDanger ? map.allDistance(pacmanPos, blueColor) : cleanDistances;
        if (distances == null) return validMove(pacmanPos, map, blueColor);

        if (!isPoweredUp) {
            int trapDir = checkForTrap(pacmanPos, map, ghosts, blueColor);
            if (trapDir != -1) return trapDir;

            for (GhostCL g : ghosts) {
                if (g.remainTimeAsEatable(0) <= 0) {
                    Pixel2D gPos = getPos(g.getPos(0).toString());
                    int d = distances.getPixel(gPos);
                    if (d != -1 && d < 3) {
                        return bestEscapeDir(pacmanPos, gPos, map, blueColor);
                    }
                }
            }
        }

        for (GhostCL g : ghosts) {
            if (g.remainTimeAsEatable(0) > 0) {
                Pixel2D gPos = getPos(g.getPos(0).toString());
                int d = distances.getPixel(gPos);
                if (d != -1 && d <= 4) {
                    int dir = goToTarget(pacmanPos, gPos, map, blueColor);
                    if (dir != -1) return dir;
                }
            }
        }

        if (!isPoweredUp) {
            for (GhostCL g : ghosts) {
                if (g.remainTimeAsEatable(0) <= 0) {
                    Pixel2D gPos = getPos(g.getPos(0).toString());
                    int d = distances.getPixel(gPos);
                    if (d != -1 && d < 4) {
                        Pixel2D nearestPowerPill = findNearestTarget(map, distances, greenColor);
                        if (nearestPowerPill != null) {
                            int distToPill = distances.getPixel(nearestPowerPill);
                            if (distToPill != -1 && distToPill < 6) {
                                int attackDir = goToTarget(pacmanPos, nearestPowerPill, map, blueColor);
                                if (attackDir != -1) return attackDir;
                            }
                        }
                        return bestEscapeDir(pacmanPos, gPos, map, blueColor);
                    }
                }
            }
        }

        if (isPoweredUp) {
            Pixel2D edibleGhostTarget = findNearestEdibleGhost(ghosts, distances);
            if (edibleGhostTarget != null) {
                int distToGhost = distances.getPixel(edibleGhostTarget);
                if (distToGhost != -1 && distToGhost < 15) {
                    int dir = goToTarget(pacmanPos, edibleGhostTarget, map, blueColor);
                    if (dir != -1) return dir;
                }
            }
        }

        Pixel2D target = findNearestTarget(map, distances, pinkColor);

        if (!isPoweredUp) {
            Pixel2D greenTarget = findNearestTarget(map, distances, greenColor);
            if (greenTarget != null && distances.getPixel(greenTarget) != -1) {
                int distToGreen = distances.getPixel(greenTarget);
                if (minGhostDist < 13 && distToGreen < 7) {
                    target = greenTarget;
                }
                else if (minGhostDist < 10) {
                    if (target == null || distToGreen < distances.getPixel(target)) {
                        target = greenTarget;
                    }
                }
                else if (target == null) {
                    target = greenTarget;
                }
            }
        }
        else if (target == null) {
            target = findNearestTarget(map, distances, greenColor);
        }

        if (target != null) {
            int dir = goToTarget(pacmanPos, target, map, blueColor);
            if (dir != -1) return dir;
        }

        return validMove(pacmanPos, map, blueColor);
    }

    /**
     * Converts a coordinate string (formatted as "x,y,z") into a Pixel2D object for algorithmic use.
     */
    public Pixel2D getPos(String s) {
        if (s == null) return null;
        String[] t = s.split(",");
        return new Index2D(Integer.parseInt(t[0]), Integer.parseInt(t[1]));
    }

    /**
     * Locates the closest pixel of a specific color (e.g., food) using a distance map.
     * Includes a tie-breaker to prioritize targets with higher "connectivity" (nearby food).
     */
    public Pixel2D findNearestTarget(Map map, Map2D distances, int targetColor) {
        double minDist = Integer.MAX_VALUE;
        Pixel2D bestTarget = null;
        int minConnectivity = Integer.MAX_VALUE;
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Pixel2D p = new Index2D(x, y);
                if (map.getPixel(p) == targetColor && distances.getPixel(p) != -1) {
                    int d = distances.getPixel(p);
                    if (d == 1) {
                        int connectivity = countFoodNeighbors(p, map, targetColor);
                        if (minDist > 1 || connectivity < minConnectivity) {
                            minDist = 1;
                            bestTarget = p;
                            minConnectivity = connectivity;
                        }
                    }
                    else if (minDist > 1 && d < minDist) {
                        minDist = d;
                        bestTarget = p;
                    }
                }
            }
        }
        return bestTarget;
    }

    /**
     * Calculates the initial direction required to reach a target by following the shortest BFS-calculated path.
     */
    public int goToTarget(Pixel2D curr, Pixel2D target, Map map, int obstacleColor) {
        Pixel2D[] path = map.shortestPath(curr, target, obstacleColor);
        if (path != null && path.length > 1) {
            return getDirection(curr, path[1], map.getWidth(), map.getHeight());
        }
        return randomDir();
    }

    /**
     * Evaluates all possible moves and selects the direction that maximizes the distance between Pacman and the nearest ghost.
     */
    public int bestEscapeDir(Pixel2D pacman, Pixel2D closestGhost, Map map, int wallColor) {
        int bestDir = -1;
        double maxDist = -1;
        int[] dirs = {Game.UP, Game.LEFT, Game.DOWN, Game.RIGHT};
        for (int dir : dirs) {
            Pixel2D nextPos = getNextPos(pacman, dir, map);
            if (map.getPixel(nextPos) != wallColor) {
                if (countOpenNeighbors(nextPos, map, wallColor) > 0) {
                    double d = nextPos.distance2D(closestGhost);
                    if (d > maxDist) {
                        maxDist = d;
                        bestDir = dir;
                    }
                }
            }
        }
        return bestDir == -1 ? validMove(pacman, map, wallColor) : bestDir;
    }

    /**
     * Returns the number of adjacent cells (Up, Down, Left, Right) that are not blocked by walls.
     */
    public int countOpenNeighbors(Pixel2D p, Map map, int wallColor) {
        int count = 0;
        int[] dirs = {Game.UP, Game.LEFT, Game.DOWN, Game.RIGHT};
        for (int dir : dirs) {
            if (map.getPixel(getNextPos(p, dir, map)) != wallColor) count++;
        }
        return count;
    }

    /**
     * Predicts the next Pixel2D position based on a given direction, incorporating cyclic wrap-around logic.
     */
    public Pixel2D getNextPos(Pixel2D curr, int dir, Map map) {
        int x = curr.getX(), y = curr.getY();
        if (dir == Game.UP) y++;
        if (dir == Game.DOWN) y--;
        if (dir == Game.RIGHT) x++;
        if (dir == Game.LEFT) x--;
        return new Index2D((x + map.getWidth()) % map.getWidth(), (y + map.getHeight()) % map.getHeight());
    }

    /**
     * Predicts the next Pixel2D position based on a given direction, incorporating cyclic wrap-around logic.
     */
    public int getDirection(Pixel2D curr, Pixel2D next, int w, int h) {
        int dx = next.getX() - curr.getX(), dy = next.getY() - curr.getY();
        if (Math.abs(dx) > 1) dx = -dx / Math.abs(dx);
        if (Math.abs(dy) > 1) dy = -dy / Math.abs(dy);
        if (dx == 1) return Game.RIGHT;
        if (dx == -1) return Game.LEFT;
        if (dy == 1) return Game.UP;
        if (dy == -1) return Game.DOWN;
        return Game.RIGHT;
    }

    /**
     * Counts how many neighboring cells of a specific position contain a target item (like food).
     */
    public int countFoodNeighbors(Pixel2D p, Map map, int targetColor) {
        int count = 0;
        int[] dx = {0, 0, 1, -1}, dy = {1, -1, 0, 0};
        for (int i = 0; i < 4; i++) {
            int nx = (p.getX() + dx[i] + map.getWidth()) % map.getWidth();
            int ny = (p.getY() + dy[i] + map.getHeight()) % map.getHeight();
            if (map.getPixel(nx, ny) == targetColor) count++;
        }
        return count;
    }

    /**
     * Temporarily marks areas around a danger point (ghost) as walls to prevent pathing too close to threats.
     */
    public void setSafetyZone(Map map, Pixel2D center, int wallColor, Pixel2D safeSpot) {
        int r = 1;
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                int nx = (center.getX() + x + map.getWidth()) % map.getWidth();
                int ny = (center.getY() + y + map.getHeight()) % map.getHeight();
                if (nx != safeSpot.getX() || ny != safeSpot.getY()) {
                    map.setPixel(nx, ny, wallColor);
                }
            }
        }
    }

    /**
     * Identifies the first available legal direction that is not obstructed by a wall, used as a fallback.
     */
    public int validMove(Pixel2D curr, Map map, int wallColor) {
        int[] dirs = {Game.UP, Game.LEFT, Game.DOWN, Game.RIGHT};
        for (int dir : dirs) {
            if (map.getPixel(getNextPos(curr, dir, map)) != wallColor) return dir;
        }
        return Game.UP;
    }

    /**
     * Analyzes nearby ghosts to detect "pincer" attacks. If only one safe exit exists, returns that direction.
     */
    public int checkForTrap(Pixel2D pacman, Map map, GhostCL[] ghosts, int wallColor) {
        boolean ghostsNearby = false;
        for (GhostCL g : ghosts) {
            if (g.remainTimeAsEatable(0) <= 0 && pacman.distance2D(getPos(g.getPos(0).toString())) < 10) {
                ghostsNearby = true;
                break;
            }
        }
        if (!ghostsNearby) return -1;

        int safeDir = -1, safeCount = 0;
        int[] dirs = {Game.UP, Game.LEFT, Game.DOWN, Game.RIGHT};
        for (int dir : dirs) {
            Pixel2D next = getNextPos(pacman, dir, map);
            if (map.getPixel(next) == wallColor) continue;
            boolean isSafe = true;
            for (GhostCL g : ghosts) {
                if (g.remainTimeAsEatable(0) <= 0 && next.distance2D(getPos(g.getPos(0).toString())) < 4) {
                    isSafe = false;
                    break;
                }
            }
            if (isSafe) {
                safeCount++;
                safeDir = dir;
            }
        }
        return safeCount == 1 ? safeDir : -1;
    }

    /**
     * Scans all ghosts to find the closest one that is currently in a vulnerable (edible) state.
     */
    public Pixel2D findNearestEdibleGhost(GhostCL[] ghosts, Map2D distances) {
        Pixel2D bestGhost = null;
        int minD = Integer.MAX_VALUE;
        for (GhostCL g : ghosts) {
            if (g.remainTimeAsEatable(0) > 0) {
                Pixel2D gPos = getPos(g.getPos(0).toString());
                int d = distances.getPixel(gPos);
                if (d != -1 && d < minD) {
                    minD = d;
                    bestGhost = gPos;
                }
            }
        }
        return bestGhost;
    }

    /**
     * Scans the entire map to count the total number of items of a specific color currently remaining.
     */
    public int countRemainingFood(Map map, int color) {
        int count = 0;
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                if (map.getPixel(x, y) == color) count++;
            }
        }
        return count;
    }

    /**
     * Selects and returns a random direction from the four cardinal directions.
     */
    public int randomDir() {
        int[] dirs = {Game.UP, Game.LEFT, Game.DOWN, Game.RIGHT};
        return dirs[(int) (Math.random() * dirs.length)];
    }
}