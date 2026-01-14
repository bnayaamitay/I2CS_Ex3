package Server;

public class MyMain {
    private static Character _cmd;
    private static int _dt = GameInfo.DT;

    public static void main(String[] args) {
        playLevels();
    }

    public static void playLevels() {
        String mapStr =
                "WWWWWWWWWWWWWWWWWWWWWWW\n" +
                        "W.....................W\n" +
                        "W.WWWWWWWW.W.WWWWWWWW.W\n" +
                        "W.....W....W....W.....W\n" +
                        "WWW.W.W.WWWWWWW.W.W.WWW\n" +
                        "W..CW.............WC..W\n" +
                        "W.WWW.WWWW.W.WWWW.WWW.W\n" +
                        "W..........W..........W\n" +
                        "W.WWW.W.WWWWWWW.W.WWW.W\n" +
                        "......W.........W......\n" +
                        "WW.WW.W.WWWWWWW.W.WW.WW\n" +
                        "........W  G  W........\n" +
                        "WW.WW.W.W     W.W.WW.WW\n" +
                        "W.....W.WWW WWW.W.....W\n" +
                        "WWWWW.W....P....W.WWWWW\n" +
                        "W.....WWWW.W.WWWW.....W\n" +
                        "W.W.W.W....W....W.W.W.W\n" +
                        "W.W.W.W.WWWWWWW.W.W.W.W\n" +
                        "W.WCW.............WCW.W\n" +
                        "W.W.WWWWWW.W.WWWWWW.W.W\n" +
                        "W..........W..........W\n" +
                        "WWWWWWWWWWWWWWWWWWWWWWW\n";

        String[] lines = mapStr.split("\n");
        int h = lines.length;
        int w = lines[0].length();

        GameGUI gui = new GameGUI();
        PacManAlgo algo = new Algo();
        Game game = new Game();

        while (true) {
            int totalLives = 1;
            int score = 0;
            long lastMoveTime = System.currentTimeMillis();

            StdDraw.setCanvasSize(800, 600);
            StdDraw.setXscale(0, w);
            StdDraw.setYscale(0, h);
            StdDraw.enableDoubleBuffering();

            while (true) {
                StdDraw.clear(java.awt.Color.BLACK);
                try {
                    StdDraw.picture(w / 2.0, h / 2.0, "src/Server/Image/menu.png", w * 0.8, h * 1.0);
                } catch (Exception e) {
                    StdDraw.setPenColor(java.awt.Color.WHITE);
                    StdDraw.text(w / 2.0, h / 2.0, "PRESS SPACE TO START");
                }
                StdDraw.show();

                if (StdDraw.hasNextKeyTyped()) {
                    char key = StdDraw.nextKeyTyped();
                    if (key == ' ') break;
                    if (key == 'x' || key == 'X') {
                        game.init(1, mapStr, GameInfo.CYCLIC_MODE, GameInfo.RANDOM_SEED, 0, 0, 3);
                        showPauseMenu(gui, game, algo, true);
                    }
                }
                try { Thread.sleep(50); } catch (Exception e) {}
            }

            for (int level = 1; level <= 4; level++) {
                game.init(level, mapStr, GameInfo.CYCLIC_MODE, GameInfo.RANDOM_SEED, 0, score, totalLives);
                game.setDesiredGhosts(level);

                int lastDir = Game.STAY;
                gui.draw(game, lastDir, ((Algo)algo).isAuto());

                try {
                    StdDraw.picture(w / 2.0, h / 2.0, "src/Server/Image/start" + level + ".png", w * 0.5, h * 0.5);
                } catch (Exception e) {
                    StdDraw.setPenColor(java.awt.Color.YELLOW);
                    StdDraw.text(w / 2.0, h / 2.0, "LEVEL " + level + " - PRESS SPACE");
                }
                StdDraw.show();

                while (true) {
                    if (StdDraw.hasNextKeyTyped() && StdDraw.nextKeyTyped() == ' ') break;
                    try { Thread.sleep(50); } catch (Exception e) {}
                }

                boolean quitToMain = false;
                while (game.getStatus() == 0) {
                    if (StdDraw.hasNextKeyTyped()) {
                        _cmd = StdDraw.nextKeyTyped();
                        if (_cmd == 'x' || _cmd == 'X') {
                            if (showPauseMenu(gui, game, algo, false)) {
                                quitToMain = true;
                                break;
                            }
                            _cmd = null;
                            lastMoveTime = System.currentTimeMillis();
                        }
                    }
                    long now = System.currentTimeMillis();
                    if (now - lastMoveTime > _dt) {
                        lastDir = algo.move(game);
                        game.move(lastDir);
                        gui.draw(game, lastDir, ((Algo)algo).isAuto());
                        lastMoveTime = now;
                        _cmd = null;
                    }
                    try { Thread.sleep(10); } catch (Exception e) {}
                }

                if (quitToMain) break;

                score = game.getScore();
                totalLives = game.getLives();

                while (true) {
                    gui.draw(game, lastDir, ((Algo)algo).isAuto());
                    if (StdDraw.hasNextKeyTyped() && StdDraw.nextKeyTyped() == ' ') break;
                    try { Thread.sleep(50); } catch (Exception e) {}
                }

                if (totalLives <= 0) break;
            }
        }
    }

    private static boolean showPauseMenu(GameGUI gui, Game game, PacManAlgo algo, boolean isStartMenu) {
        boolean paused = true;
        while (paused) {
            gui.drawMenu(game, ((Algo)algo).isAuto(), _dt, isStartMenu);
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                if (key == ' ') return false;
                if (key == 'q' || key == 'Q') return true;
                if (key == 'm' || key == 'M') _dt = Math.max(20, _dt - 10);
                if (key == 'n' || key == 'N') _dt = Math.min(200, _dt + 10);
            }
            try { Thread.sleep(50); } catch (Exception e) {}
        }
        return false;
    }

    public static Character getCMD() {
        return _cmd;
    }
}