package Server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AlgoTest {
    private Algo _algo;
    private Map _map;

    @BeforeEach
    public void setUp() {
        _algo = new Algo();
        int[][] board = new int[20][20];
        _map = new Map(board);
    }

    @Test
    public void testGetPos() {
        Pixel2D p = _algo.getPos("10,15,0");
        assertEquals(10, p.getX());
        assertEquals(15, p.getY());
    }

    @Test
    public void testIsInGhostHouse() {
        assertTrue(_algo.isInGhostHouse(10, 10, 20, 20));
        assertFalse(_algo.isInGhostHouse(0, 0, 20, 20));
    }

    @Test
    public void testGetNextPos() {
        Pixel2D p = new Index2D(0, 0);
        Pixel2D up = _algo.getNextPos(p, Game.UP, _map);
        assertEquals(0, up.getX());
        assertEquals(1, up.getY());

        Pixel2D leftWrap = _algo.getNextPos(p, Game.LEFT, _map);
        assertEquals(19, leftWrap.getX());
    }

    @Test
    public void testGetDirection() {
        Pixel2D p1 = new Index2D(5, 5);
        Pixel2D p2 = new Index2D(5, 6);
        assertEquals(Game.UP, _algo.getDirection(p1, p2, 20, 20));

        Pixel2D p3 = new Index2D(0, 5);
        Pixel2D p4 = new Index2D(19, 5);
        assertEquals(Game.LEFT, _algo.getDirection(p3, p4, 20, 20));
    }

    @Test
    public void testFixManualDir() {
        assertEquals(3, _algo.fixManualDir(1));
        assertEquals(1, _algo.fixManualDir(3));
        assertEquals(2, _algo.fixManualDir(4));
        assertEquals(4, _algo.fixManualDir(2));
        assertEquals(0, _algo.fixManualDir(0));
    }

    @Test
    public void testIsAnyFoodReachable() {
        int[][] board = new int[5][5];
        board[2][2] = Game.FOOD;
        Map2D dists = new Map(5, 5, 0).allDistance(new Index2D(0,0), Game.WALL);
        assertTrue(_algo.isAnyFoodReachable(board, dists));
    }

    @Test
    public void testBlockNeighbors() {
        _map.init(5, 5, 0);
        Pixel2D center = new Index2D(2, 2);
        _algo.blockNeighbors(_map, center, 5, 5, Game.WALL);
        assertEquals(Game.WALL, _map.getPixel(2, 3));
        assertEquals(Game.WALL, _map.getPixel(2, 1));
        assertEquals(Game.WALL, _map.getPixel(3, 2));
        assertEquals(Game.WALL, _map.getPixel(1, 2));
    }
}