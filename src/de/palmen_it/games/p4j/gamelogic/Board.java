package de.palmen_it.games.p4j.gamelogic;

import java.io.Serializable;

public class Board implements Serializable {
	private static final long serialVersionUID = 8538141074979359100L;
	
	private Piece[][] _fields;
	private int[][] _insertHistory;
	private int _numberOfInserts;

	public Board() {
		_fields = new Piece[6][7]; // the actual board
		_insertHistory = new int[42][2]; // sequence of coordinates where pieces
		// were inserted
		clear();
	}

	public void clear() {
		for (int row = 0; row < 6; ++row)
			for (int col = 0; col < 7; ++col)
				_fields[row][col] = Piece.EMPTY;
		_numberOfInserts = 0; // no pieces inserted
	}

	public int getNumberOfInserts() {
		return _numberOfInserts;
	}

	public Piece getPieceAt(int row, int col) {
		// check coordinates
		if (row < 0 || col < 0 || row > 5 || col > 6)
			return Piece.EMPTY;

		return _fields[row][col];
	}

	public int insertPieceIn(Piece piece, int col) {
		// check for valid column and piece
		if (col < 0 || col > 6 || piece == Piece.EMPTY)
			return -1;

		// search bottom up for first empty position
		for (int row = 5; row >= 0; --row) {
			if (_fields[row][col] == Piece.EMPTY) {
				// insert piece here
				_fields[row][col] = piece;

				// update history
				_insertHistory[_numberOfInserts][0] = row;
				_insertHistory[_numberOfInserts][1] = col;
				++_numberOfInserts;
				
				return row;
			}
		}

		// column is full -> return -1
		return -1;
	}

	public boolean undoInsertion() {
		// check if any insertions were made
		if (_numberOfInserts == 0)
			return false;

		// remove last inserted Piece
		--_numberOfInserts;
		_fields [_insertHistory[_numberOfInserts][0]]
				[_insertHistory[_numberOfInserts][1]]
						= Piece.EMPTY;
		return true;
	}

	public PlayerRows getRows(int maxMissing) {
		PlayerRows rows = new PlayerRows(maxMissing);
		PieceComparer cmp = new PieceComparer(maxMissing);

		for (int row = 0; row < 6; ++row) {
			for (int col = 0; col < 7; ++col) {
				if (row < 3) {
					for (int checkrow = row; checkrow < row + 4; ++checkrow) {
						if (!cmp.check(_fields[checkrow][col]))
							break;
					}
					if (cmp.getIsMatch())
						rows.addRow(cmp.getPiece(), cmp.getSkipped());
					cmp.reset();
				}
				if (row < 3 && col < 4) {
					for (int checkrow = row, checkcol = col; checkcol < col + 4; ++checkrow, ++checkcol) {
						if (!cmp.check(_fields[checkrow][checkcol]))
							break;
					}
					if (cmp.getIsMatch())
						rows.addRow(cmp.getPiece(), cmp.getSkipped());
					cmp.reset();
				}
				if (row > 2 && col < 4) {
					for (int checkrow = row, checkcol = col; checkcol < col + 4; --checkrow, ++checkcol) {
						if (!cmp.check(_fields[checkrow][checkcol]))
							break;
					}
					if (cmp.getIsMatch())
						rows.addRow(cmp.getPiece(), cmp.getSkipped());
					cmp.reset();
				}
				if (col < 4) {
					for (int checkcol = col; checkcol < col + 4; ++checkcol) {
						if (!cmp.check(_fields[row][checkcol]))
							break;
					}
					if (cmp.getIsMatch())
						rows.addRow(cmp.getPiece(), cmp.getSkipped());
					cmp.reset();
				}
			}
		}
		return rows;
	}
}
