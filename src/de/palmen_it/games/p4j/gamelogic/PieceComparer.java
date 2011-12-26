package de.palmen_it.games.p4j.gamelogic;

/**
 * Helper class to compare consecutive pieces and determine whether they
 * belong to a row with a maximum amount of holes.
 */
class PieceComparer {
	private final int _maxMissing;
	private int _skipped;
	private boolean _isMatch;
	private Piece _reference;
	
	public void reset() {
		_reference = Piece.EMPTY;
		_skipped = 0;
		_isMatch = true;
	}
	
	public boolean getIsMatch() {
		return _isMatch && _reference != Piece.EMPTY;
	}
	
	public int getSkipped() {
		return _skipped;
	}
	
	public Piece getPiece() {
		return _reference;
	}
	
	public PieceComparer(int maxMissing) {
		_maxMissing = maxMissing;
		reset();
	}
	
	/**
	 * Add a piece to the checked row
	 * @param p the piece to add
	 * @return true if this could still result in a match, false otherwise
	 */
	public boolean check(Piece p) {
		if (_reference == Piece.EMPTY) {
			if (p == Piece.EMPTY) {
				if (_skipped < _maxMissing) ++_skipped;
				else {
					_isMatch = false;
					return false;
				}
			}
			else _reference = p;
		} else {
			if (p != _reference) {
				if (p == Piece.EMPTY && _skipped < _maxMissing) {
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