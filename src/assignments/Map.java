package assignments;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This class represents a 2D map as a "screen" or a raster matrix or maze over integers.
 * @author boaz.benmoshe
 *
 */
public class Map implements Map2D {
	private int[][] _map;
	private boolean _cyclicFlag = true;
	
	/**
	 * Constructs a w*h 2D raster map with an init value v.
	 * @param w
	 * @param h
	 * @param v
	 */
	public Map(int w, int h, int v) {init(w,h, v);}
	/**
	 * Constructs a square map (size*size).
	 * @param size
	 */
	public Map(int size) {this(size,size, 0);}
	
	/**
	 * Constructs a map from a given 2D array.
	 * @param data
	 */
	public Map(int[][] data) {
		init(data);
	}
	@Override
	public void init(int w, int h, int v) {
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("Invalid width or height");
        }
		this._map = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                this._map[x][y] = v;
            }
        }
	}
	@Override
	public void init(int[][] arr) {
        if (arr == null || arr.length == 0 || arr[0].length == 0 ) {
            throw new IllegalArgumentException("Array is null or empty");
        }
        int w = arr.length;
        int h = arr[0].length;
        _map = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                this._map[x][y] = arr[x][y];
            }
        }
	}

	@Override
	public int[][] getMap() {
		int[][] ans = null;
        if (_map == null) return null;
        int w = _map.length;
        int h = _map[0].length;
		ans = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                ans[x][y] = _map[x][y];
            }
        }
		return ans;
	}

	@Override
	public int getWidth() {return _map.length;}

	@Override
	public int getHeight() {
        if (_map == null || _map.length == 0) return 0;
        return _map[0].length;}

    @Override
	public int getPixel(int x, int y) {
        if (x < 0 || y < 0 || x >= _map.length || y >= _map[0].length) {
            throw new IndexOutOfBoundsException("Coordinate ("+ x + "," + y + ") out of bounds");
        }
        return _map[x][y];
    }

    @Override
	public int getPixel(Pixel2D p) {
        if (p == null) throw new IllegalArgumentException("Pixel cannot be null");
		return this.getPixel(p.getX(),p.getY());
	}

	@Override
	public void setPixel(int x, int y, int v) {
        if (x < 0 || y < 0 || x >= _map.length || y >= _map[0].length) {
            throw new IndexOutOfBoundsException("Coordinate ("+ x + "," + y + ") out of bounds");
        }
        _map[x][y] = v;
    }

	@Override
	public void setPixel(Pixel2D p, int v) {
        if (p == null) throw new IllegalArgumentException("Pixel cannot be null");
        int x = p.getX();
        int y = p.getY();
        if (x < 0 || y < 0 || x >= _map.length || y >= _map[0].length) {
            throw new IndexOutOfBoundsException("Coordinate ("+ x + "," + y + ") out of bounds");
        }
        _map[x][y] = v;
    }

	@Override
	/**
     *  Fills a connected area starting from point 'xy' with color 'new_v' (Flood Fill).
     * a. Check if 'xy' is valid (inside map) and if its current color is different from 'new_v'. If not, return 0.
     * b. Identify the original color at 'xy' as 'old_v'.
     * c. Initialize a Queue for BFS and a counter 'ans' = 0.
     * d. Add 'xy' to the queue, set its pixel to 'new_v', and increment 'ans'.
     * e. While the queue is not empty:
     * i.   Remove the front pixel 'current'.
     * ii.  For each of the 4 adjacent neighbors (Up, Down, Left, Right):
     * - If the map is in 'Cyclic' mode, calculate wrapped coordinates using modulo.
     * - If the neighbor is inside the map and its color is 'old_v':
     * - Change neighbor's color to 'new_v'.
     * - Add the neighbor to the queue.
     * - Increment 'ans'.
     * f. Return the total count of changed pixels 'ans'.
	 */
	public int fill(Pixel2D xy, int new_v) {
		int ans=0;
        if (!isInside(xy) || getPixel(xy.getX(), xy.getY()) == new_v) {
            return ans;
        }
        int old_v = getPixel(xy.getX(), xy.getY());
        Queue<Pixel2D> pendingP = new LinkedList<>();
        pendingP.add(xy);
        setPixel(xy.getX(), xy.getY(), new_v);
        ans ++;
        int [] dx = {1,-1,0,0};
        int [] dy = {0,0,1,-1};
        while (!pendingP.isEmpty()) {
            Pixel2D current = pendingP.poll();
            int cx = current.getX();
            int cy = current.getY();
            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];
                if (isCyclic()) {
                    nx = (nx + _map.length) % _map.length;
                    ny = (ny + _map[0].length) % _map[0].length;
                }
                Pixel2D neighbor = new Index2D(nx, ny);
                if (isInside(neighbor) && getPixel(nx, ny) == old_v) {
                    setPixel(nx, ny, new_v);
                    pendingP.add(neighbor);
                    ans++;
                }
            }
        }
        return ans;
	}

	@Override
	/**
     *  Finds the shortest path between p1 and p2 avoiding 'obsColor' using BFS.
     * a. Preliminary checks: If points are outside, return null. If p1 equals p2, return array with p1.
     * b. If p1 or p2 are obstacles, return null.
     * c. Create a 2D 'distances' matrix initialized with -1.
     * d. Set distances[p1] = 0 and add p1 to a Queue.
     * e. BFS Phase (Map distances):
     * i.   While queue is not empty and p2 is not found:
     * - Get 'current' pixel from queue.
     * - For each neighbor (handling cyclic wrap if enabled):
     * - If neighbor is not an obstacle and distance is -1:
     * - Set distance[neighbor] = distance[current] + 1.
     * - Add neighbor to queue.
     * f. Backtracking Phase (Reconstruct Path):
     * i.   If p2 was never reached, return null.
     * ii.  Create an array 'ans' of size (distance[p2] + 1).
     * iii. Starting from currentBack = p2, work backwards from index 'dist' to 0:
     * - Find a neighbor whose distance is exactly (current distance - 1).
     * - Add that neighbor to the array and move to it.
     * g. Return the 'ans' array.
	 */
	public Pixel2D[] shortestPath(Pixel2D p1, Pixel2D p2, int obsColor) {
        Pixel2D[] ans = null;
        if (!isInside(p1) || !isInside(p2)) {
            return ans;
        }
        if (p1.equals(p2)) {
            ans = new Pixel2D[1];
            ans[0] = p1;
            return ans;
        }
        if (getPixel(p1.getX(), p1.getY()) == obsColor || getPixel(p2.getX(), p2.getY()) == obsColor) {
            return ans;
        }
        int[][] distances = new int[_map.length][_map[0].length];
        for (int i = 0; i < _map.length; i++) {
            for (int j = 0; j < _map[0].length; j++) {
                distances[i][j] = -1;
            }
        }
        distances[p1.getX()][p1.getY()] = 0;
        Queue<Pixel2D> pendingP = new LinkedList<>();
        pendingP.add(p1);
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        boolean found = false;
        while (!pendingP.isEmpty()) {
            Pixel2D current = pendingP.poll();
            if (current.equals(p2)) {
                found = true;
                break;
            }
            int cx = current.getX();
            int cy = current.getY();
            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];
                if (isCyclic()) {
                    nx = (nx + _map.length) % _map.length;
                    ny = (ny + _map[0].length) % _map[0].length;
                }
                if (nx >= 0 && nx < _map.length && ny >= 0 && ny < _map[0].length) {
                    if (getPixel(nx, ny) != obsColor && distances[nx][ny] == -1) {
                        distances[nx][ny] = distances[cx][cy] + 1;
                        pendingP.add(new Index2D(nx, ny));
                    }
                }
            }
        }
        if (!found || distances[p2.getX()][p2.getY()] == -1) {
            return null;
        }
        int dist = distances[p2.getX()][p2.getY()];
        ans = new Pixel2D[dist + 1];
        Pixel2D currentBack = p2;
        ans[dist] = p2;
        for (int i = dist - 1; i >= 0; i--) {
            int cx = currentBack.getX();
            int cy = currentBack.getY();
            for (int k = 0; k < 4; k++) {
                int nx = cx + dx[k];
                int ny = cy + dy[k];
                if (isCyclic()) {
                    nx = (nx + _map.length) % _map.length;
                    ny = (ny + _map[0].length) % _map[0].length;
                }
                if (nx >= 0 && nx < _map.length && ny >= 0 && ny < _map[0].length) {
                    if (distances[nx][ny] == i) {
                        ans[i] = new Index2D(nx, ny);
                        currentBack = ans[i];
                        break;
                    }
                }
            }
        }
        return ans;
    }

	@Override
	public boolean isInside(Pixel2D p) {
        if (p == null) return false;
        if (p.getX() < 0 || p.getY() < 0 || p.getX() >= _map.length || p.getY() >= _map[0].length) {
            return false;
        }
		return true;
	}

	@Override
	public boolean isCyclic() {
		return _cyclicFlag;
	}

	@Override
	public void setCyclic(boolean cy) {
        this._cyclicFlag = cy;
    }

	@Override
    /**
     * Generates a distance map where each pixel contains its shortest distance from 'start'.
     * a. If 'start' is invalid or an obstacle, return null.
     * b. Initialize a new Map 'ans' of the same dimensions filled with -1.
     * c. Set the distance of 'start' in 'ans' to 0 and add 'start' to a Queue.
     * d. While the queue is not empty:
     * i.   Get 'current' pixel and its distance 'currentDist'.
     * ii.  For each neighbor (handling cyclic wrap if enabled):
     * - If neighbor is valid, not an obstacle, and its distance in 'ans' is -1:
     * - Set ans[neighbor] = currentDist + 1.
     * - Add neighbor to the queue.
     * e. Return the resulting Map 'ans'.
     */
	public Map2D allDistance(Pixel2D start, int obsColor) {
        Map2D ans = null;
        if (!isInside(start) || getPixel(start) == obsColor) {
            return ans;
        }
        ans = new Map(_map.length, _map[0].length, -1);
        ans.setPixel(start, 0);
        Queue<Pixel2D> pendingP = new LinkedList<>();
        pendingP.add(start);
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        while (!pendingP.isEmpty()) {
            Pixel2D current = pendingP.poll();
            int cx = current.getX();
            int cy = current.getY();
            int currentDist = ans.getPixel(cx, cy);
            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];
                if (isCyclic()) {
                    nx = (nx + _map.length) % _map.length;
                    ny = (ny + _map[0].length) % _map[0].length;
                }
                if (nx >= 0 && nx < _map.length && ny >= 0 && ny < _map[0].length) {
                    if (getPixel(nx, ny) != obsColor && ans.getPixel(nx, ny) == -1) {
                        ans.setPixel(nx, ny, currentDist + 1);
                        pendingP.add(new Index2D(nx, ny));
                    }
                }
            }
        }
        return ans;
    }
}
