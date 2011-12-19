package de.palmen_it.games.p4j.gamelogic;

import java.util.ArrayList;

public class AIPlayer extends Player {

	private final Piece _opponentPiece;
	private int _difficulty;
	private ArrayList<Integer> _bestColumns;
	
	public void setDifficulty(int value) {
		if (value < 2) _difficulty = 2;
		else if (value > 12) _difficulty = 12;
		else _difficulty = value;
	}
	
	public AIPlayer(Board board, Piece piece) {
		super(board, piece, false);
		_difficulty = 6;
		_opponentPiece = (piece == Piece.Red) ? Piece.Black : Piece.Red;		
	}
	
	private int scoreCurrentState()
	{
		int score = 0;
		PlayerRows rows = _board.getRows(2);
		Piece winner = rows.getWinner();
		if (winner == _piece) score += 200;
		else if (winner == _opponentPiece) score -= 200;
		score += 10 * (rows.getCount(_piece, 1) - rows.getCount(_opponentPiece, 1));
		score += rows.getCount(_piece, 2) - rows.getCount(_opponentPiece, 2);
		return score;
	}
	
	private int maximize(int depth, int alpha, int beta)
	{
		// done when maximal number of movements calculated
		// or board full
		if (depth == _difficulty || _board.getNumberOfInserts() == 42) {
			return scoreCurrentState();
		}
		
		// for top level, remember best scored columns
		if (depth == 0) {
			_bestColumns = new ArrayList<Integer>();
		}
		
		// standard minimax / alpha-beta-cutoff
		// with score interval [-10000; 10000]
		int localAlpha = -10000;
		for (int col = 0; col < 7; ++col) {
			if (_board.insertPieceIn(_piece, col)) {
				int min = minimize(depth + 1, alpha, beta);
				_board.undoInsertion();
				if (min > localAlpha) {
					if (depth == 0) {
						_bestColumns.clear();
						_bestColumns.add(col);
					}
					if (min > beta) return min;
					localAlpha = min;
					if (min > alpha) alpha = min;
				}
				
				else if (depth == 0 && min == localAlpha) _bestColumns.add(col);
			}
		}
		return localAlpha;
	}
	
	private int minimize(int depth, int alpha, int beta) {
		// done when maximal number of movements calculated
		// or board full
		if (depth == _difficulty  || _board.getNumberOfInserts() == 42) {
			return scoreCurrentState();
		}
		
		// standard minimax / alpha-beta-cutoff
		// with score interval [-10000; 10000]
		int localBeta = 10000;
		for (int col = 0; col < 7; ++col) {
			if (_board.insertPieceIn(_opponentPiece, col)) {
				int max = maximize(depth + 1, alpha, beta);
				_board.undoInsertion();
				if (max < localBeta) {
					if (max < alpha) return max;
					localBeta = max;
					if (max < beta) beta = max;
				}
			}
		}
		return localBeta;
	}

	@Override
	protected int determineNextColumn() {
		maximize(0, -10000, 10000);
		if (_bestColumns.size() == 1) return _bestColumns.get(0);
		int idx = (int) (Math.random() * (_bestColumns.size() - 1));
		return _bestColumns.get(idx);
	}

}
