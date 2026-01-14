package Server;

import assignments.Index2D;
import assignments.Map;
import assignments.Map2D;
import assignments.Pixel2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

class MapTest {
    private int[][] _map_3_3 = {{0, 1, 0}, {1, 0, 1}, {0, 1, 0}};
    private assignments.Map2D _m1, _m3_3;

    @BeforeEach
    void setUp() {
        _m1 = new assignments.Map(3);
        _m3_3 = new assignments.Map(_map_3_3);
    }

    @Test
    @Timeout(value = 1, unit = SECONDS)
    void testInitAndDimensions() {
        int[][] bigarr = new int[500][500];
        _m1.init(bigarr);
        assertEquals(500, _m1.getWidth());
        assertEquals(500, _m1.getHeight());
        assertEquals(0, _m1.getPixel(0, 0));
    }

    @Test
    void testEncapsulation() {
        int[][] data = {{5}};
        assignments.Map map = new assignments.Map(data);
        data[0][0] = 100;
        assertEquals(5, map.getPixel(0, 0));

        int[][] exported = map.getMap();
        exported[0][0] = 999;
        assertEquals(5, map.getPixel(0, 0));
    }

    @Test
    void testOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> _m3_3.getPixel(3, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> _m3_3.getPixel(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> _m3_3.setPixel(3, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> _m3_3.getPixel(null));
    }

    @Test
    void testIsInside() {
        assertTrue(_m3_3.isInside(new assignments.Index2D(0, 0)));
        assertTrue(_m3_3.isInside(new assignments.Index2D(2, 2)));
        assertFalse(_m3_3.isInside(new assignments.Index2D(3, 3)));
        assertFalse(_m3_3.isInside(new assignments.Index2D(-1, 0)));
        assertFalse(_m3_3.isInside(null));
    }

    @Test
    void testFill_NonCyclic() {
        _m3_3.setCyclic(false);
        int changed = _m3_3.fill(new assignments.Index2D(0, 0), 5);
        assertEquals(1, changed);
        assertEquals(5, _m3_3.getPixel(0, 0));
    }

    @Test
    void testFill_Cyclic() {
        assignments.Map map = new assignments.Map(3, 3, 0);
        map.setCyclic(true);
        map.setPixel(0, 1, 1);
        map.setPixel(1, 1, 1);
        map.setPixel(2, 1, 1);
        int changed = map.fill(new assignments.Index2D(0, 0), 2);
        assertEquals(6, changed);
        assertEquals(2, map.getPixel(0, 2));
    }

    @Test
    void testShortestPath_WithObstacle() {
        assignments.Map map = new assignments.Map(5, 5, 0);
        map.setCyclic(false);
        map.setPixel(2, 1, 1);
        map.setPixel(2, 2, 1);
        map.setPixel(2, 3, 1);
        assignments.Pixel2D p1 = new assignments.Index2D(1, 2);
        assignments.Pixel2D p2 = new assignments.Index2D(3, 2);
        assignments.Pixel2D[] path = map.shortestPath(p1, p2, 1);
        assertNotNull(path);
        assertEquals(p1, path[0]);
        assertEquals(p2, path[path.length - 1]);
        assertTrue(path.length > 3);
    }

    @Test
    void testShortestPath_Cyclic() {
        assignments.Map map = new assignments.Map(10, 10, 0);
        map.setCyclic(true);
        assignments.Pixel2D p1 = new assignments.Index2D(0, 5);
        assignments.Pixel2D p2 = new assignments.Index2D(9, 5);
        Pixel2D[] path = map.shortestPath(p1, p2, 1);
        assertEquals(2, path.length);
    }

    @Test
    void testAllDistance_Simple() {
        _m3_3.setCyclic(false);
        assignments.Map2D dists = _m3_3.allDistance(new assignments.Index2D(1, 1), 1);
        assertNotNull(dists);
        assertEquals(0, dists.getPixel(1, 1));
        assertEquals(-1, dists.getPixel(0, 0));
    }

    @Test
    void testAllDistance_Cyclic() {
        assignments.Map map = new assignments.Map(5, 5, 0);
        map.setCyclic(true);
        Map2D dists = map.allDistance(new assignments.Index2D(0, 0), 1);
        assertEquals(1, dists.getPixel(4, 0));
        assertEquals(2, dists.getPixel(4, 4));
    }

    @Test
    void testShortestPath_Blocked() {
        assignments.Map map = new Map(3, 3, 0);
        map.setCyclic(false);
        map.setPixel(0, 1, 1);
        map.setPixel(1, 0, 1);
        map.setPixel(1, 1, 1);
        assertNull(map.shortestPath(new assignments.Index2D(0, 0), new Index2D(2, 2), 1));
    }
}