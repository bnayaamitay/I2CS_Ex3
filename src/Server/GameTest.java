package Server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    private Game game;
    private final String smallMap =
            "WWWWWWW\n" +
                    "W.P.C.W\n" +
                    "WWWGWWW\n" +
                    "WWWWWWW\n";

    @BeforeEach
    void setUp() {
        game = new Game();
        game.init(1, smallMap, false, 42, 0, 0, 3);
        game.setDesiredGhosts(1);
    }

    @Test
    void testInit() {
        assertEquals(0, game.getScore());
        assertEquals(3, game.getLives());
        assertEquals(0, game.getStatus());

        int[][] board = game.getGame(0);
        assertEquals(7, board.length);
        assertEquals(4, board[0].length);
    }

    @Test
    void testPacmanMovementAndWalls() {
        game.move(Game.LEFT);
        assertEquals("1,1,0", game.getPos(0));

        game.move(Game.DOWN);
        assertEquals("1,1,0", game.getPos(0));
    }

    @Test
    void testEatingFood() {
        game.move(Game.LEFT);
        assertEquals(1, game.getScore());

        int[][] board = game.getGame(0);
        assertEquals(Game.EMPTY, board[1][1]);
    }

    @Test
    void testEatingCherryAndPowerUp() {
        game.move(Game.RIGHT);
        game.move(Game.RIGHT);

        assertEquals(11, game.getScore());
        assertTrue(game.getEatableTime() > 0);
    }

    @Test
    void testGhostCollisionNormalMode() {
        game.move(Game.RIGHT);
        game.move(Game.UP);

        assertEquals(0, game.getLives());
        assertEquals(1, game.getStatus());
    }

    @Test
    void testPowerUpEatingGhost() {
        game.init(1, smallMap, false, 42, 0, 0, 3);
        game.setDesiredGhosts(1);

        game.move(Game.RIGHT);
        game.move(Game.RIGHT);

        assertTrue(game.getEatableTime() > 0);
        int scoreAfterCherry = game.getScore();

        game.move(Game.LEFT);
        game.move(Game.UP);

        assertEquals(scoreAfterCherry + 100, game.getScore());
        assertEquals(3, game.getLives());
    }

    @Test
    void testLossCondition() {
        game.init(1, smallMap, false, 42, 0, 0, 1);
        game.setDesiredGhosts(1);

        game.move(Game.RIGHT);
        game.move(Game.UP);

        assertEquals(0, game.getLives());
        assertEquals(1, game.getStatus());
    }

    @Test
    void testWinCondition() {
        game.move(Game.LEFT);
        game.move(Game.RIGHT);
        game.move(Game.RIGHT);
        game.move(Game.RIGHT);
        game.move(Game.RIGHT);

        assertEquals(1, game.getStatus());
    }
}