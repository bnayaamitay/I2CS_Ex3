package Server;

import java.awt.*;

public class GameGUI {

    private boolean _init = false;

    public void draw(PacmanGame game, int dir, boolean isAuto) {
        try {
            int[][] board = game.getGame(0);
            int w = board.length;
            int h = board[0].length;

            if (!_init) {
                initCanvas(w, h);
                _init = true;
            }

            StdDraw.clear(Color.BLACK);
            drawBoard(board, w, h);
            drawPacman(game, dir);
            drawGhosts(game);
            drawUI(game, w, h, isAuto);

            if (game.getStatus() == 1) {
                if (game.getLives() > 0) {
                    drawWinScreen(w, h, game.getScore());
                } else {
                    drawGameOverScreen(w, h);
                }
            }

            StdDraw.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void drawMenu(PacmanGame game, boolean isAuto, int dt, boolean isStartMenu) {
        int w = game.getGame(0).length;
        int h = game.getGame(0)[0].length;

        if (isStartMenu) {
            StdDraw.clear(Color.BLACK);
            try {
                StdDraw.picture(w / 2.0, h / 2.0, "src/Server/Image/menu.png", w * 0.8, h * 1.0);
            } catch (Exception e) {}
        } else {
            draw(game, 0, isAuto);
        }

        StdDraw.setPenColor(new Color(0, 0, 0, 220));
        StdDraw.filledRectangle(w / 2.0, h / 2.0, w * 0.35, h * 0.38);

        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setPenRadius(0.005);
        StdDraw.rectangle(w / 2.0, h / 2.0, w * 0.35, h * 0.38);

        StdDraw.setFont(new Font("Monospaced", Font.BOLD, 40));
        StdDraw.text(w / 2.0, h * 0.82, "PAUSED");

        StdDraw.setPenColor(Color.YELLOW);
        StdDraw.setFont(new Font("Monospaced", Font.BOLD, 18));
        StdDraw.text(w / 2.0, h * 0.74, "SPEED: " + dt);

        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monospaced", Font.BOLD, 14));
        StdDraw.text(w / 2.0, h * 0.70, "Press 'M' for high speed");
        StdDraw.text(w / 2.0, h * 0.66, "Press 'N' for low speed");

        StdDraw.setPenColor(Color.YELLOW);
        StdDraw.setFont(new Font("Monospaced", Font.BOLD, 22));
        StdDraw.text(w / 2.0, h * 0.58, "CONTROLS");

        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monospaced", Font.BOLD, 16));
        double startY = 0.52;
        double step = 0.045;
        StdDraw.text(w / 2.0, h * startY, "W : UP");
        StdDraw.text(w / 2.0, h * (startY - step), "A : LEFT");
        StdDraw.text(w / 2.0, h * (startY - 2*step), "S : DOWN");
        StdDraw.text(w / 2.0, h * (startY - 3*step), "D : RIGHT");

        StdDraw.text(w / 2.0, h * (startY - 4.5*step), "ENTER : AUTO/MANUAL");
        StdDraw.text(w / 2.0, h * (startY - 5.5*step), "SPACE : CONTINUE");
        StdDraw.text(w / 2.0, h * (startY - 6.5*step), "Q : MAIN MENU");

        StdDraw.show();
    }

    private void drawWinScreen(int w, int h, int score) {
        StdDraw.setPenColor(new Color(0, 0, 0, 180));
        StdDraw.filledRectangle(w / 2.0, h / 2.0, w / 2.0, h / 2.0);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monospaced", Font.BOLD, 60));
        StdDraw.text(w / 2.0, h * 0.65, "YOU WON!");
        StdDraw.setFont(new Font("Monospaced", Font.BOLD, 30));
        StdDraw.text(w / 2.0 - 2, h * 0.45, "YOUR SCORE");
        StdDraw.setPenColor(Color.YELLOW);
        StdDraw.text(w / 2.0 + 4, h * 0.45, "" + score);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monospaced", Font.PLAIN, 20));
        StdDraw.text(w / 2.0, h * 0.25, "PRESS SPACE TO CONTINUE");
    }

    private void drawGameOverScreen(int w, int h) {
        StdDraw.setPenColor(new Color(0, 0, 0, 180));
        StdDraw.filledRectangle(w / 2.0, h / 2.0, w / 2.0, h / 2.0);
        try {
            StdDraw.picture(w / 2.0, h / 2.0, "src/Server/Image/gameover.png", w * 0.8, h * 0.4);
        } catch (Exception e) {
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.setFont(new Font("Monospaced", Font.BOLD, 50));
            StdDraw.text(w / 2.0, h / 2.0, "GAME OVER");
        }
    }

    private void initCanvas(int w, int h) {
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int targetHeight = (int)(screenSize.height * 0.8);
            int scalar = Math.max(20, targetHeight / h);
            StdDraw.setCanvasSize(w * scalar, h * scalar);
        } catch (Exception e) {}
        StdDraw.setXscale(0, w);
        StdDraw.setYscale(0, h);
        StdDraw.enableDoubleBuffering();
    }

    private void drawBoard(int[][] board, int w, int h) {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = board[x][y];
                if (pixel == 1) {
                    StdDraw.setPenColor(Color.BLUE);
                    StdDraw.filledRectangle(x + 0.5, y + 0.5, 0.49, 0.49);
                } else if (pixel == 3) {
                    StdDraw.setPenColor(Color.PINK);
                    StdDraw.filledCircle(x + 0.5, y + 0.5, 0.12);
                } else if (pixel == 4) {
                    try {
                        StdDraw.picture(x + 0.5, y + 0.5, "src/Server/Image/cherry.png", 0.8, 0.8);
                    } catch (Exception e) {}
                }
            }
        }
    }

    private void drawPacman(PacmanGame game, int dir) {
        String[] pCoords = game.getPos(0).toString().split(",");
        double px = Double.parseDouble(pCoords[0]);
        double py = Double.parseDouble(pCoords[1]);
        double angle = (dir == 1) ? 90 : (dir == 3) ? 270 : (dir == 4) ? 180 : 0;
        try {
            StdDraw.picture(px + 0.5, py + 0.5, "src/Server/Image/p1.png", 0.8, 0.8, angle);
        } catch (Exception e) {
            StdDraw.setPenColor(Color.YELLOW);
            StdDraw.filledCircle(px + 0.5, py + 0.5, 0.4);
        }
    }

    private void drawGhosts(PacmanGame game) {
        GhostCL[] ghosts = game.getGhosts(0);
        long timeLeft = ((Game)game).getEatableTime();
        if (ghosts == null) return;
        for (int i = 0; i < ghosts.length; i++) {
            String[] gCoords = ghosts[i].getPos(0).toString().split(",");
            double gx = Double.parseDouble(gCoords[0]);
            double gy = Double.parseDouble(gCoords[1]);
            String img = (timeLeft > 1000 || (timeLeft > 0 && (System.currentTimeMillis() / 200) % 2 == 0)) ? "sg.png" : "g" + (i % 4) + ".png";
            try {
                StdDraw.picture(gx + 0.5, gy + 0.5, "src/Server/Image/" + img, 0.8, 0.8);
            } catch (Exception e) {
                StdDraw.setPenColor(timeLeft > 0 ? Color.BLUE : Color.RED);
                StdDraw.filledCircle(gx + 0.5, gy + 0.5, 0.4);
            }
        }
    }

    private void drawUI(PacmanGame game, int w, int h, boolean isAuto) {
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monospaced", Font.BOLD, 14));
        StdDraw.textLeft(0.5, h - 0.5, "Score: " + game.getScore());
        String modeText = isAuto ? "AUTOMATIC" : "MANUAL";
        StdDraw.text(w / 2.0, h - 0.5, "MODE: " + modeText + " (Press 'x' for Menu)");
        StdDraw.textRight(w - 0.5, h - 0.5, "Lives: " + game.getLives());
    }
}