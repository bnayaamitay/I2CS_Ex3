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
	 * Fills this map with the new color (new_v) starting from p.
	 * https://en.wikipedia.org/wiki/Flood_fill
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
	 * BFS like shortest the computation based on iterative raster implementation of BFS, see:
	 * https://en.wikipedia.org/wiki/Breadth-first_search
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
