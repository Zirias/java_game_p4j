package de.palmen_it.games.p4j.gamelogic;

import java.util.ArrayList;

public class AIPlayer extends Player {

	private final Piece _opponentPiece;
	private final ArrayList<Integer> _bestColumns;
	private int _difficulty;

	/**
	 * Set difficulty of the game, the value determines how
	 * many moves are calculated for scoring
	 * @param value the new difficulty (minimum 2, maximum 12)
	 */
	public void setDifficulty(int value) {
		if (value < 2)
			_difficulty = 2;
		else if (value > 12)
			_difficulty = 12;
		else
			_difficulty = value;
	}

	public ArrayList<Integer> getBestColumns() {
		return _bestColumns;
	}
	
	public AIPlayer(Board board, Piece piece) {
		super(board, piece, false);
		_difficulty = 6;
		_opponentPiece = (piece == Piece.Red) ? Piece.Black : Piece.Red;
		_bestColumns = new ArrayList<Integer>();
	}

	private int getWinningScore(int depth) {
		PlayerRows rows = _board.getRows(0);
		Piece winner = rows.getWinner();
		int score = 0;
		
		if (winner == _piece) {
			score = 100;
		}
		else if (winner == _opponentPiece) {
			score = -100;
		}
		// rate earlier win higher
		if (score != 0 && depth < 5) score *= (5 - depth);			
		return score;
	}

	private int scoreCurrentState() {
		int score = 0;
		PlayerRows rows = _board.getRows(2);
		Piece winner = rows.getWinner();
		if (winner == _piece) {
			return 100;
		}
		else if (winner == _opponentPiece) {
			return -100;
		}
		score += 10 * (rows.getCount(_piece, 1) - rows.getCount(_opponentPiece,
				1));
		score += rows.getCount(_piece, 2) - rows.getCount(_opponentPiece, 2);
		return score;
	}

	private int maximize(int depth, int alpha, int beta) {
		// done when maximal number of movements calculated
		// or board full
		if (depth == _difficulty || _board.getNumberOfInserts() == 42) {
			return scoreCurrentState();
		}

		// shortcut for winning position
		int winningScore = getWinningScore(depth);
		if (winningScore != 0) return winningScore;
		
		// standard minimax / alpha-beta-cutoff
		// with score interval [-500; 500]
		int localAlpha = -500;
		for (int col = 0; col < 7; ++col) {
			if (_board.insertPieceIn(_piece, col)) {
				int min = minimize(depth + 1, alpha, beta);
				_board.undoInsertion();
				if (min > localAlpha) {
					// for top level, remember best scored columns
					if (depth == 0) {
						_bestColumns.clear();
						_bestColumns.add(col);
					}
					if (min > beta)
						return min;
					localAlpha = min;
					if (min > alpha)
						alpha = min;
				}

				// for top level, remember best scored columns
				else if (depth == 0 && min == localAlpha)
					_bestColumns.add(col);
			}
		}
		return localAlpha;
	}

	private int minimize(int depth, int alpha, int beta) {
		// done when maximal number of movements calculated
		// or board full
		if (depth == _difficulty || _board.getNumberOfInserts() == 42) {
			return scoreCurrentState();
		}
		
		// shortcut for winning position
		int winningScore = getWinningScore(depth);
		if (winningScore != 0) return winningScore;
		
		// standard minimax / alpha-beta-cutoff
		// with score interval [-500; 500]
		int localBeta = 500;
		for (int col = 0; col < 7; ++col) {
			if (_board.insertPieceIn(_opponentPiece, col)) {
				int max = maximize(depth + 1, alpha, beta);
				_board.undoInsertion();
				if (max < localBeta) {
					if (max < alpha)
						return max;
					localBeta = max;
					if (max < beta)
						beta = max;
				}
			}
		}
		return localBeta;
	}

	public void ComputeBestColumns() {
		maximize(0, -500, 500);
	}
	
	@Override
	protected int determineNextColumn() {
		maximize(0, -500, 500);
		if (_bestColumns.size() == 1)
			return _bestColumns.get(0);
		int idx = (int) (Math.random() * (_bestColumns.size() - 1));
		return _bestColumns.get(idx);
	}

}
