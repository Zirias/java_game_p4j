package de.palmen_it.games.p4j.gamelogic;

import java.util.ArrayList;

public class Player {

	private static int _difficulty = 6;

	/**
	 * Set difficulty of the game, the value determines how
	 * many moves are calculated for scoring
	 * @param value the new difficulty (minimum 2, maximum 12)
	 */
	public static void setDifficulty(int value) {
		if (value < 2)
			_difficulty = 2;
		else if (value > 12)
			_difficulty = 12;
		else
			_difficulty = value;
	}
	
	public static int getDifficulty() {
		return _difficulty;
	}

	private final Board _board;
	private final Piece _piece;
	private final Piece _opponentPiece;
	private final ArrayList<Integer> _bestColumns;
	private int[] _columnScores;
	private int _scoredState;
	private boolean _isHuman;
	private int _lastRow;
	private int _lastColumn;

	public Piece getPiece() {
		return _piece;
	}
	
	public boolean getIsHuman() {
		return _isHuman;
	}
	
	public void setIsHuman(boolean value) {
		_isHuman = value;
	}
	
	public int getLastRow() {
		return _lastRow;
	}
	
	public int getLastColumn() {
		return _lastColumn;
	}
	
	public ArrayList<Integer> getBestColumns() {
		computeBestColumns();
		return _bestColumns;
	}
	
	public int[] getColumnScores() {
		computeBestColumns();
		return _columnScores;
	}
	
	public Player(Board board, Piece piece, boolean isHuman) {
		_board = board;
		_piece = piece;
		_isHuman = isHuman;
		if (_piece == Piece.RED) {
			_opponentPiece = Piece.YELLOW;
		} else if (_piece == Piece.YELLOW) {
			_opponentPiece = Piece.RED;
		} else {
			throw new IllegalArgumentException("A player's piece can't be `None'");
		}
		_bestColumns = new ArrayList<Integer>();
		_columnScores = new int[7];
		_scoredState = -1;
		_lastRow = -1;
		_lastColumn = -1;
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
			if (_board.insertPieceIn(_piece, col) >= 0) {
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
				if (depth == 0) _columnScores[col] = min;
			}
			else if (depth == 0) {
				_columnScores[col] = -500;
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
			if (_board.insertPieceIn(_opponentPiece, col) >= 0) {
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

	private void computeBestColumns() {
		int state = _board.getNumberOfInserts();
		if (state != _scoredState) {
			maximize(0, -500, 500);
			_scoredState = state;
		}
	}
	
	private int chooseColumn() {
		computeBestColumns();
		if (_bestColumns.size() == 1)
			return _bestColumns.get(0);
		int idx = (int) (Math.random() * (_bestColumns.size()));
		return _bestColumns.get(idx);		
	}

	public boolean move() {
		if (_isHuman) {
			throw new IllegalStateException(
					"A human player cannot choose a column automatically.");
		}
		return move(chooseColumn());
	}
	
	public boolean move(int column) {
		int row = _board.insertPieceIn(_piece, column);
		if (row >= 0) {
			_lastRow = row;
			_lastColumn = column;
			return true;
		}
		return false;
	}
}
