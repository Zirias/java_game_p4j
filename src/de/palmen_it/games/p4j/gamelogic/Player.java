package de.palmen_it.games.p4j.gamelogic;

public abstract class Player {

	protected final Board _board;
	protected final Piece _piece;
	private final boolean _isHuman;

	public boolean getIsHuman() {
		return _isHuman;
	}
	
	public Player(Board board, Piece piece, boolean isHuman) {
		_board = board;
		_piece = piece;
		_isHuman = isHuman;
	}

	protected abstract int determineNextColumn();

	public boolean move() {
		return _board.insertPieceIn(_piece, determineNextColumn());
	}
}
