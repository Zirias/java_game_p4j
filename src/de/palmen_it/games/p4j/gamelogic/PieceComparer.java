package de.palmen_it.games.p4j.gamelogic;

class PieceComparer {
	private final int _maxMissing;
	private int _skipped;
	private boolean _isMatch;
	private Piece _reference;
	
	public void reset() {
		_reference = Piece.None;
		_skipped = 0;
		_isMatch = true;
	}
	
	public boolean getIsMatch() {
		return _isMatch && _reference != Piece.None;
	}
	
	public int getSkipped() {
		return _skipped;
	}
	
	public Piece getPiece() {
		return _reference;
	}
	
	public PieceComparer(int maxMissing) {
		_maxMissing = maxMissing;
	}
	
	public boolean check(Piece p) {
		if (_reference == Piece.None) {
			if (p == Piece.None) {
				if (_skipped < _maxMissing) ++_skipped;
				else {
					_isMatch = false;
					return false;
				}
			}
			else _reference = p;
		} else {
			if (p != _reference) {
				if (p == Piece.None && _skipped < _maxMissing) {
					++_skipped;
				} else {
					_isMatch = false;
					return false;
				}
			}
		}
		return true;
	}
}