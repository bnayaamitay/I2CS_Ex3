package assignments;

import exe.ex3.game.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Ex3AlgoTest {
    private Ex3Algo _algo;
    private Map _map;
    private int _wall = 1;
    private int _food = 3;

    @BeforeEach
    void setUp() {
        _algo = new Ex3Algo();
        int[][] board = {
                {0, 0, 0, 0, 0},
                {0, 1, 1, 1, 0},
                {0, 0, 0, 0, 0},
                {0, 1, 1, 1, 0},
                {0, 0, 0, 0, 0}
        };
        _map = new Map(board);
        _map.setCyclic(true);
    }

    @Test
    void testGetPos() {
        Pixel2D p = _algo.getPos("1,2,0");
        assertNotNull(p);
        assertEquals(1, p.getX());
        assertEquals(2, p.getY());
        assertNull(_algo.getPos(null));
    }

    @Test
    void testCountRemainingFood() {
        _map.setPixel(0, 0, _food);
        _map.setPixel(4, 4, _food);
        assertEquals(2, _algo.countRemainingFood(_map, _food));
    }

    @Test
    void testGetNextPos_Cyclic() {
        Pixel2D curr = new Index2D(0, 0);
        Pixel2D next = _algo.getNextPos(curr, Game.LEFT, _map);
        assertEquals(_map.getWidth() - 1, next.getX());
        assertEquals(0, next.getY());
    }

    @Test
    void testGetDirection() {
        Pixel2D p1 = new Index2D(1, 1);
        Pixel2D p2 = new Index2D(1, 2);
        assertEquals(Game.UP, _algo.getDirection(p1, p2, 5, 5));

        Pixel2D pStart = new Index2D(0, 0);
        Pixel2D pWrap = new Index2D(4, 0);
        assertEquals(Game.LEFT, _algo.getDirection(pStart, pWrap, 5, 5));
    }

    @Test
    void testCountOpenNeighbors() {
        Pixel2D p = new Index2D(0, 0);
        assertEquals(4, _algo.countOpenNeighbors(p, _map, _wall));

        _map.setPixel(1, 0, _wall);
        assertEquals(3, _algo.countOpenNeighbors(p, _map, _wall));
    }

    @Test
    void testSetSafetyZone() {
        Pixel2D ghostPos = new Index2D(2, 2);
        Pixel2D pacmanPos = new Index2D(0, 0);
        _algo.setSafetyZone(_map, ghostPos, _wall, pacmanPos);

        assertEquals(_wall, _map.getPixel(2, 2));
        assertEquals(_wall, _map.getPixel(2, 3));
        assertEquals(_wall, _map.getPixel(3, 2));
    }

    @Test
    void testValidMove() {
        Pixel2D p = new Index2D(1, 0);
        int move = _algo.validMove(p, _map, _wall);
        assertNotEquals(Game.UP, move);
        Pixel2D next = _algo.getNextPos(p, move, _map);
        assertNotEquals(_wall, _map.getPixel(next));
    }

    @Test
    void testFindNearestTarget() {
        _map.setPixel(4, 4, _food);
        Pixel2D pacman = new Index2D(0, 0);
        Map2D dists = _map.allDistance(pacman, _wall);

        Pixel2D target = _algo.findNearestTarget(_map, dists, _food);
        assertNotNull(target);
        assertEquals(4, target.getX());
        assertEquals(4, target.getY());
    }

    @Test
    void testBestEscapeDir() {
        Pixel2D pacman = new Index2D(2, 0);
        Pixel2D ghost = new Index2D(1, 0);

        int escapeDir = _algo.bestEscapeDir(pacman, ghost, _map, _wall);
        assertNotEquals(Game.LEFT, escapeDir);
    }

    @Test
    void testCountFoodNeighbors() {
        _map.setPixel(0, 1, _food);
        _map.setPixel(1, 0, _food);
        Pixel2D p = new Index2D(0, 0);
        assertEquals(2, _algo.countFoodNeighbors(p, _map, _food));
    }
}