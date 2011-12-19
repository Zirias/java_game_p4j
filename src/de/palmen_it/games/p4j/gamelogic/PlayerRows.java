package de.palmen_it.games.p4j.gamelogic;

public class PlayerRows {
	private final int _maxMissingPieces;
	private int[] _redRows;
	private int[] _blackRows;
	
	public PlayerRows(int maxMissingPieces) {
		_maxMissingPieces = maxMissingPieces;
		_blackRows = new int[maxMissingPieces + 1];
		_redRows = new int [maxMissingPieces + 1];
		for (int i = 0; i <= maxMissingPieces; ++i) {
			_blackRows[i] = 0;
			_redRows[i] = 0;
		}
	}
	
	public void addRow(Piece p, int missing)
	{
		if (missing < 0 || missing > _maxMissingPieces) return;
		switch (p) {
		case Black:
			++_blackRows[missing];
			break;
		case Red:
			++_redRows[missing];
			break;
		}
	}
	
	public int getCount(Piece p, int missing)
	{
		if (missing < 0 || missing > _maxMissingPieces) return 0;
		switch (p) {
		case Black: return _blackRows[missing];
		case Red: return _redRows[missing];
		default: return _redRows[missing] + _blackRows[missing];
		}
	}
	
	public Piece getWinner()
	{
		if (_redRows[0] > 0 && _blackRows[0] == 0) return Piece.Red;
		else if (_blackRows[0] > 0 && _redRows[0] == 0) return Piece.Black;
		else return Piece.None;
	}
}
