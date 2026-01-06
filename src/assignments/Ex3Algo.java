package assignments;

import exe.ex3.game.Game;
import exe.ex3.game.GhostCL;
import exe.ex3.game.PacManAlgo;
import exe.ex3.game.PacmanGame;

import java.awt.*;

/**
 * This is the major algorithmic class for Ex3 - the PacMan game:
 *
 * This code is a very simple example (random-walk algorithm).
 * Your task is to implement (here) your PacMan algorithm.
 */
public class Ex3Algo implements PacManAlgo{
	private int _count;
	public Ex3Algo() {_count=0;}
    private boolean _manualMode = false;
    private ManualAlgo _manualAlgo = new ManualAlgo();
	@Override
	/**
	 *  Add a short description for the algorithm as a String.
	 */
	public String getInfo() {
        return "PACMAN ALGO - SMART SURVIVOR \n" +
        "*** PRESS [ENTER] TO TOGGLE MANUAL / AUTO MODE *** \n\n" +
                "1. GREEDY SEARCH: Uses BFS to find the shortest path \n" +
                "   to the closest food pellet. \n" +
                "2. STARTUP LOGIC: Ignores ghosts in the central 'Ghost House' \n" +
                "   (radius 4) to prevent hesitation at the start. \n" +
                "3. ENDGAME AGGRESSION: Normal Safety Radius is 8 steps. \n" +
                "   BUT, if Food Left <= 5, Safety Radius drops to 3! \n" +
                "   Pacman becomes brave to rush the final points. \n" +
                "4. RESOURCE MANAGEMENT: Conserves Power Pills (Green)! \n" +
                "   If ghosts are far (>13), green pills get LOW priority. \n" +
                "5. TRAP ESCAPE: Detects 'Pincer Attacks' (blocked sides). \n" +
                "   If only one exit remains, it abandons food to survive. \n" +
                "6. COUNTER-ATTACK: If a ghost is approaching (<13) and \n" +
                "   a weapon is near (<7), it sprints to grab the Power Pill. \n" +
                "7. SAFE HUNTING: Chases edible ghosts (up to 15 steps), \n" +
                "   but strictly forbids entering the center trap.";
    }

	@Override
	/**
	 * This ia the main method - that you should design, implement and test.
	 */
    public int move(PacmanGame game) {
        Character cmd = Ex3Main.getCMD();
            if (cmd != null) {
                if (cmd == '\n' || cmd == '\r' || cmd == 10 || cmd == 13) {
                    _manualMode = !_manualMode;
                }
            }
            if (_manualMode) {
                return _manualAlgo.move(game);
            }

            int[][] board = game.getGame(0);
            Map map = new Map(board);
            map.setCyclic(GameInfo.CYCLIC_MODE);

            int blueColor = Game.getIntColor(Color.BLUE, 0);
            int pinkColor = Game.getIntColor(Color.PINK, 0);
            int greenColor = Game.getIntColor(Color.GREEN, 0);

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

                    if (isNearCenter(gPos, map)) {
                        map.setPixel(gPos, blueColor);
                        continue;
                    }

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

            Map2D distances;
            if (detectedDanger) {
                distances = map.allDistance(pacmanPos, blueColor);
                if (distances == null) return validMove(pacmanPos, map, blueColor);
            } else {
                distances = cleanDistances;
            }

            if (!isPoweredUp) {
                int trapDir = checkForTrap(pacmanPos, map, ghosts, blueColor);
                if (trapDir != -1) return trapDir;

                for (GhostCL g : ghosts) {
                    if (g.remainTimeAsEatable(0) <= 0) {
                        Pixel2D gPos = getPos(g.getPos(0).toString());

                        if (isNearCenter(gPos, map)) continue;

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

                    if (d != -1 && d <= 4 && !isNearCenter(gPos, map)) {
                        int dir = goToTarget(pacmanPos, gPos, map, blueColor);
                        if (dir != -1) return dir;
                    }
                }
            }

            if (!isPoweredUp) {
                for (GhostCL g : ghosts) {
                    if (g.remainTimeAsEatable(0) <= 0) {
                        Pixel2D gPos = getPos(g.getPos(0).toString());

                        if (isNearCenter(gPos, map)) continue;

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
                Pixel2D edibleGhostTarget = findNearestEdibleGhost(ghosts, distances, map);
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
                    else {
                        if (target == null) {
                            target = greenTarget;
                        }
                    }
                }
            } else {
                if (target == null) {
                    target = findNearestTarget(map, distances, greenColor);
                }
            }

            if (target != null) {
                int dir = goToTarget(pacmanPos, target, map, blueColor);
                if (dir != -1) return dir;
            }

            return validMove(pacmanPos, map, blueColor);
    }

	private static void printGhosts(GhostCL[] gs) {
		for(int i=0;i<gs.length;i++){
			GhostCL g = gs[i];
			System.out.println(i+") status: "+g.getStatus()+",  type: "+g.getType()+",  pos: "+g.getPos(0)+",  time: "+g.remainTimeAsEatable(0));
		}
	}

	private static int randomDir() {
		int[] dirs = {Game.UP, Game.LEFT, Game.DOWN, Game.RIGHT};
		int ind = (int)(Math.random()*dirs.length);
		return dirs[ind];
	}

    private Pixel2D getPos(String s) {
        if (s == null) return null;
        String[] t = s.split(",");
        int x = Integer.parseInt(t[0]);
        int y = Integer.parseInt(t[1]);
        return new Index2D(x, y);
    }

    private Pixel2D findNearestTarget(Map map, Map2D distances, int targetColor) {
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

    private int goToTarget(Pixel2D curr, Pixel2D target, Map map, int obstacleColor) {
        Pixel2D[] path = map.shortestPath(curr, target, obstacleColor);
        if (path != null && path.length > 1) {
            return getDirection(curr, path[1], map.getWidth(), map.getHeight());
        }
        return randomDir();
    }

    private int bestEscapeDir(Pixel2D pacman, Pixel2D closestGhost, Map map, int wallColor) {
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
        if (bestDir == -1) return validMove(pacman, map, wallColor);
        return bestDir;
    }

    private int countOpenNeighbors(Pixel2D p, Map map, int wallColor) {
        int count = 0;
        int[] dirs = {Game.UP, Game.LEFT, Game.DOWN, Game.RIGHT};
        for (int dir : dirs) {
            Pixel2D n = getNextPos(p, dir, map);
            if (map.getPixel(n) != wallColor) count++;
        }
        return count;
    }

    private Pixel2D getNextPos(Pixel2D curr, int dir, Map map) {
        int x = curr.getX();
        int y = curr.getY();
        if(dir == Game.UP) y++;
        if(dir == Game.DOWN) y--;
        if(dir == Game.RIGHT) x++;
        if(dir == Game.LEFT) x--;
        x = (x + map.getWidth()) % map.getWidth();
        y = (y + map.getHeight()) % map.getHeight();
        return new Index2D(x, y);
    }

    private int getDirection(Pixel2D curr, Pixel2D next, int w, int h) {
        int dx = next.getX() - curr.getX();
        int dy = next.getY() - curr.getY();
        if (Math.abs(dx) > 1) dx = -dx / Math.abs(dx);
        if (Math.abs(dy) > 1) dy = -dy / Math.abs(dy);
        if (dx == 1) return Game.RIGHT;
        if (dx == -1) return Game.LEFT;
        if (dy == 1) return Game.UP;
        if (dy == -1) return Game.DOWN;
        return Game.RIGHT;
    }

    private int countFoodNeighbors(Pixel2D p, Map map, int targetColor) {
        int count = 0;
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};
        for (int i = 0; i < 4; i++) {
            int nx = p.getX() + dx[i];
            int ny = p.getY() + dy[i];
            nx = (nx + map.getWidth()) % map.getWidth();
            ny = (ny + map.getHeight()) % map.getHeight();
            Pixel2D neighbor = new Index2D(nx, ny);
            if (map.getPixel(neighbor) == targetColor) {
                count++;
            }
        }
        return count;
    }

    private void setSafetyZone(Map map, Pixel2D center, int wallColor, Pixel2D safeSpot) {
        int r = 1;
        for(int x = -r; x <= r; x++) {
            for(int y = -r; y <= r; y++) {
                int nx = center.getX() + x;
                int ny = center.getY() + y;
                nx = (nx + map.getWidth()) % map.getWidth();
                ny = (ny + map.getHeight()) % map.getHeight();
                if (nx == safeSpot.getX() && ny == safeSpot.getY()) {
                    continue;
                }
                map.setPixel(new Index2D(nx, ny), wallColor);
            }
        }
    }

    private int validMove(Pixel2D curr, Map map, int wallColor) {
        int[] dirs = {Game.UP, Game.LEFT, Game.DOWN, Game.RIGHT};
        for (int dir : dirs) {
            Pixel2D next = getNextPos(curr, dir, map);
            if (map.getPixel(next) != wallColor) {
                return dir;
            }
        }
        return Game.UP;
    }

    private int checkForTrap(Pixel2D pacman, Map map, GhostCL[] ghosts, int wallColor) {
        int[] dirs = {Game.UP, Game.LEFT, Game.DOWN, Game.RIGHT};
        int safeDir = -1;
        int safeCount = 0;
        boolean ghostsNearby = false;
        for (GhostCL g : ghosts) {
            if (g.remainTimeAsEatable(0) <= 0) {
                Pixel2D gPos = getPos(g.getPos(0).toString());
                if (pacman.distance2D(gPos) < 10) {
                    ghostsNearby = true;
                    break;
                }
            }
        }
        if (!ghostsNearby) return -1;
        for (int dir : dirs) {
            Pixel2D next = getNextPos(pacman, dir, map);
            if (map.getPixel(next) == wallColor) continue;
            boolean isDirectionSafe = true;
            for (GhostCL g : ghosts) {
                if (g.remainTimeAsEatable(0) <= 0) {
                    Pixel2D gPos = getPos(g.getPos(0).toString());
                    if (next.distance2D(gPos) < 4) {
                        isDirectionSafe = false;
                        break;
                    }
                }
            }
            if (isDirectionSafe) {
                safeCount++;
                safeDir = dir;
            }
        }
        if (safeCount == 1) {
            return safeDir;
        }
        return -1;
    }

    private Pixel2D findNearestEdibleGhost(GhostCL[] ghosts, Map2D distances, Map map) {
        Pixel2D bestGhost = null;
        int minD = Integer.MAX_VALUE;
        for (GhostCL g : ghosts) {
            if (g.remainTimeAsEatable(0) > 0) {
                Pixel2D gPos = getPos(g.getPos(0).toString());
                if (isNearCenter(gPos, map)) {
                    continue;
                }
                int d = distances.getPixel(gPos);
                if (d != -1 && d < minD) {
                    minD = d;
                    bestGhost = gPos;
                }
            }
        }
        return bestGhost;
    }

    private boolean isNearCenter(Pixel2D p, Map map) {
        int centerX = map.getWidth() / 2;
        int centerY = map.getHeight() / 2;
        Pixel2D center = new Index2D(centerX, centerY);
        return p.distance2D(center) <= 4;
    }

    private int countRemainingFood(Map map, int color) {
        int count = 0;
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                if (map.getPixel(x, y) == color) {
                    count++;
                }
            }
        }
        return count;
    }

}