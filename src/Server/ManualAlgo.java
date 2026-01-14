package Server;

public class ManualAlgo implements PacManAlgo{
    public ManualAlgo() {;}
    @Override
    public String getInfo() {
        return "This is a manual algorithm for manual controlling the PacMan using w,a,s,d (up,left,down,right).";
    }

    @Override
    public int move(PacmanGame game) {
        int ans = PacmanGame.ERR;
        Character cmd = MyMain.getCMD();
            if (cmd != null) {
                if (cmd == 's' || cmd == 'S') {ans = PacmanGame.UP;}
                if (cmd == 'w' || cmd == 'W') {ans = PacmanGame.DOWN;}
                if (cmd == 'a' || cmd == 'A') {ans = PacmanGame.LEFT;}
                if (cmd == 'd' || cmd == 'D') {ans = PacmanGame.RIGHT;}
            }
            return  ans;
    }
}
