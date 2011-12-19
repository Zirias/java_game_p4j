package de.palmen_it.games.p4j.gamelogic;

public class HumanPlayer extends Player {

	private int _nextColumn;
	
	public HumanPlayer(Board board, Piece piece) {
		super(board, piece, true);
	}

	public void setNextColumn(int value)
	{
		_nextColumn = value;
	}
	
	@Override
	protected int determineNextColumn() {
		return _nextColumn;
	}

}
