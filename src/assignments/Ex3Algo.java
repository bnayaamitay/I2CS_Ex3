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
        return "This algorithm implements an autonomous Pacman agent " +
                "using Breadth-First Search (BFS) to navigate the map optimally. " +
                "It prioritizes collecting the nearest pink food pellets " +
                "while actively calculating safe paths to avoid ghost collisions " +
                "by treating them as dynamic walls. " +
                "When a ghost is detected within a dangerous proximity, " +
                "the agent switches to an evasion strategy to maximize distance from threats. " +
                "Additionally, the algorithm supports a hybrid mode, " +
                "allowing the user to toggle manual control by pressing Enter during the game.";
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
                System.out.println("Mode Switched! Manual: " + _manualMode);
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
        GhostCL[] ghosts = game.getGhosts(0);

        for (int i = 0; i < ghosts.length; i++) {
            if (ghosts[i].remainTimeAsEatable(0) > 0) {
                continue;
            }
            Pixel2D gPos = getPos(ghosts[i].getPos(0).toString());
            map.setPixel(gPos, blueColor);
        }

        String posString = game.getPos(0).toString();
        Pixel2D pacmanPos = getPos(posString);
        Map2D distances = map.allDistance(pacmanPos, blueColor);

        int minGhostDist = Integer.MAX_VALUE;
        for (int i = 0; i < ghosts.length; i++) {
            if (ghosts[i].remainTimeAsEatable(0) > 0) continue;
            Pixel2D ghostPos = getPos(ghosts[i].getPos(0).toString());
            int d = distances.getPixel(ghostPos);
            if (d != -1 && d < minGhostDist) {
                minGhostDist = d;
            }
        }

        if (minGhostDist < 6) {
            Pixel2D closestGhost = null;
            int minD = Integer.MAX_VALUE;
            for (int i = 0; i < ghosts.length; i++) {
                if (ghosts[i].remainTimeAsEatable(0) > 0) continue;
                Pixel2D gPos = getPos(ghosts[i].getPos(0).toString());
                int d = distances.getPixel(gPos);
                if (d != -1 && d < minD) {
                    minD = d;
                    closestGhost = gPos;
                }
            }
            if (closestGhost != null) {
                return bestEscapeDir(pacmanPos, closestGhost, map);
            }
        }

        double minDist = Integer.MAX_VALUE;
        Pixel2D target = null;
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Pixel2D p = new Index2D(x, y);
                if (map.getPixel(p) == pinkColor && distances.getPixel(p) != -1) {
                    int d = distances.getPixel(p);
                    if (d < minDist) {
                        minDist = d;
                        target = p;
                    }
                }
            }
        }

        if (target != null) {
            Pixel2D[] path = map.shortestPath(pacmanPos, target, blueColor);
            if (path != null && path.length > 1) {
                return getDirection(pacmanPos, path[1], map.getWidth(), map.getHeight());
            }
        }

        return randomDir();
    }

	private static void printBoard(int[][] b) {
		for(int y =0;y<b[0].length;y++){
			for(int x =0;x<b.length;x++){
				int v = b[x][y];
				System.out.print(v+"\t");
			}
			System.out.println();
		}
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

    private int getDirection(Pixel2D curr, Pixel2D next, int w, int h) {
        int dx = next.getX() - curr.getX();
        int dy = next.getY() - curr.getY();
        if (Math.abs(dx) > 1) {
            dx = -dx / Math.abs(dx);
        }
        if (Math.abs(dy) > 1) {
            dy = -dy / Math.abs(dy);
        }
        if (dx == 1) return Game.RIGHT;
        if (dx == -1) return Game.LEFT;
        if (dy == 1) return Game.UP;
        if (dy == -1) return Game.DOWN;
        return Game.RIGHT;
    }

    private int bestEscapeDir(Pixel2D pacman, Pixel2D closestGhost, Map map) {
        int bestDir = -1;
        double maxDist = -1;
        int[] dirs = {Game.UP, Game.LEFT, Game.DOWN, Game.RIGHT};
        int[] dx = {0, -1, 0, 1};
        int[] dy = {1, 0, -1, 0};
        int blueColor = Game.getIntColor(Color.BLUE, 0);

        for (int i = 0; i < 4; i++) {
            int nx = pacman.getX() + dx[i];
            int ny = pacman.getY() + dy[i];

            if (map.isCyclic()) {
                nx = (nx + map.getWidth()) % map.getWidth();
                ny = (ny + map.getHeight()) % map.getHeight();
            }

            Pixel2D neighbor = new Index2D(nx, ny);

            if (map.getPixel(neighbor) != blueColor) {
                double d = neighbor.distance2D(closestGhost);
                if (d > maxDist) {
                    maxDist = d;
                    bestDir = dirs[i];
                }
            }
        }

        if (bestDir == -1) return randomDir();
        return bestDir;
    }
}