package Server;

public class GameInfo {
	public static final long RANDOM_SEED = 31; // Random seed
	public static final boolean CYCLIC_MODE = true;
	public static final int DT = 130; // [20,200]
	private static PacManAlgo _manualAlgo = new ManualAlgo();
	private static PacManAlgo _myAlgo = new Algo();;
	public static final PacManAlgo ALGO = _myAlgo;
}
