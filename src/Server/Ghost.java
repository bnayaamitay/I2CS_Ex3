package Server;

public class Ghost implements GhostCL {
    private Pixel2D _pos;
    private Pixel2D _startPos;
    private int _type;
    private int _id;
    private double _speed;
    private double _eatableTime;

    public Ghost(Pixel2D pos, int type, int id, double speed) {
        this._pos = new Index2D(pos.getX(), pos.getY());
        this._startPos = new Index2D(pos.getX(), pos.getY());
        this._type = type;
        this._id = id;
        this._speed = speed;
        this._eatableTime = 0;
    }

    @Override
    public int getType() { return _type; }

    @Override
    public String getPos(int id) {
        return _pos.getX() + "," + _pos.getY() + ",0";
    }

    public Pixel2D getPixelPos() { return _pos; }

    public void setPos(Pixel2D p) { this._pos = p; }

    public void reset() { this._pos = new Index2D(_startPos.getX(), _startPos.getY()); }

    @Override
    public double remainTimeAsEatable(int id) { return _eatableTime; }

    public void setEatable(double time) { this._eatableTime = time; }

    public void decreaseTime() {
        if (_eatableTime > 0) _eatableTime--;
    }

    @Override
    public int getStatus() { return GhostCL.PLAY; }

    @Override
    public String getInfo() { return "Ghost " + _id; }
}