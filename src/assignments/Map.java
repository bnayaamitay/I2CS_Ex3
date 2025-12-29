package assignments;

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
		/////// add your code below ///////

		///////////////////////////////////
		return ans;
	}

	@Override
	/**
	 * BFS like shortest the computation based on iterative raster implementation of BFS, see:
	 * https://en.wikipedia.org/wiki/Breadth-first_search
	 */
	public Pixel2D[] shortestPath(Pixel2D p1, Pixel2D p2, int obsColor) {
		Pixel2D[] ans = null;  // the result.
		/////// add your code below ///////

		///////////////////////////////////
		return ans;
	}
	@Override
	/////// add your code below ///////
	public boolean isInside(Pixel2D p) {
		return false;
	}

	@Override
	/////// add your code below ///////
	public boolean isCyclic() {
		return false;
	}
	@Override
	/////// add your code below ///////
	public void setCyclic(boolean cy) {;}
	@Override
	/////// add your code below ///////
	public Map2D allDistance(Pixel2D start, int obsColor) {
		Map2D ans = null;  // the result.
		/////// add your code below ///////

		///////////////////////////////////
		return ans;
	}
}
