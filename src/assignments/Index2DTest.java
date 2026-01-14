package assignments;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Index2DTest {

    @Test
    public void testConstructorsAndGetters() {
        int x = 5, y = -3;
        Pixel2D p1 = new Index2D(x, y);
        assertEquals(x, p1.getX(), "Constructor should set X correctly");
        assertEquals(y, p1.getY(), "Constructor should set Y correctly");
    }

    @Test
    public void testConstructorsAndGetters2() {
        int x = 0, y = 0;
        Pixel2D p1 = new Index2D(x, y);
        assertEquals(x, p1.getX(), "Constructor should set X correctly");
        assertEquals(y, p1.getY(), "Constructor should set Y correctly");
    }

    @Test
    public void testCopyConstructor() {
        Pixel2D p1 = new Index2D(30, 40);
        Pixel2D p2 = new Index2D(p1);
        assertEquals(p1.getX(), p2.getX());
        assertEquals(p1.getY(), p2.getY());
        assertNotSame(p1, p2, "Copy constructor should create a new object");
    }

    @Test
    public void testCopyConstructor2() {
        Pixel2D p1 = new Index2D(-10, 0);
        Pixel2D p2 = new Index2D(p1);
        assertEquals(p1.getX(), p2.getX());
        assertEquals(p1.getY(), p2.getY());
    }

    @Test
    public void testCopyConstructorException() {
        assertThrows(RuntimeException.class, () -> {new Index2D(null);}
        , "Copy constructor must throw exception for null input");
    }

    @Test
    public void testDistance2D() {
        Pixel2D p1 = new Index2D(1, 1);
        Pixel2D p2 = new Index2D(0, 0);
        assertEquals(p1.distance2D(p2), Math.sqrt(2));
    }

    @Test
    public void testDistanceException() {
        Pixel2D p1 = new Index2D(2, 7);
        assertThrows(RuntimeException.class, () -> {
            p1.distance2D(null);
        }, "Should throw exception when p2 is null");
    }

    @Test
    public void testToString() {
        Pixel2D p1 = new Index2D(-5, 6);
        assertEquals("-5,6", p1.toString(), "toString() should equal string");
    }

    @Test
    public void testEquals1() {
        Pixel2D p1 = new Index2D(3, 4);
        Pixel2D p2 = new Index2D(3, 4);
        assertTrue(p1.equals(p2), "Two pixels with same (x,y) should be equal");
        assertTrue(p1.equals(p1), "Object should be equal to itself");
    }

    @Test
    public void testEquals2() {
        Pixel2D p1 = new Index2D(3, 4);
        Pixel2D p3 = new Index2D(5, 5);
        assertFalse(p1.equals(p3), "Pixels with different coordinates should not be equal");
    }

    @Test
    public void testEquals3() {
        Pixel2D p1 = new Index2D(10, 8);
        assertFalse(p1.equals(null), "Should be false for null");
        assertFalse(p1.equals("Some String"), "Should be false for different object type");
    }
}

