package Server;

public class Pacman {
    private Pixel2D _pos;
    private Pixel2D _startPos;

    public Pacman(Pixel2D startPos) {
        this._pos = startPos;
        this._startPos = startPos;
    }

    public Pixel2D getPos() { return _pos; }
    public void setPos(Pixel2D p) { this._pos = p; }
    public void reset() { this._pos = _startPos; }
}