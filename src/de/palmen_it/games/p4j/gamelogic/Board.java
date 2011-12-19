package de.palmen_it.games.p4j.gamelogic;

public class Board implements Cloneable {

	private Piece[][] _fields;
	private int[] _insertHistory;
	private int _numberOfInserts;

	public Board() {
		_fields = new Piece[6][7]; // the actual board
		_insertHistory = new int[42]; // sequence of columns where pieces were
										// inserted
		clear();
	}

	@Override
	public Object clone() {
		Board clone = new Board();
		for (int row = 0; row < 6; ++row)
			for (int col = 0; col < 7; ++col)
				clone._fields[row][col] = _fields[row][col];
		return clone;
	}

	public void clear() {
		for (int row = 0; row < 6; ++row)
			for (int col = 0; col < 7; ++col)
				_fields[row][col] = Piece.None;
		_numberOfInserts = 0; // no pieces inserted
	}

	public int getNumberOfInserts() {
		return _numberOfInserts;
	}
	
	public Piece getPieceAt(int row, int col) {
		// check coordinates
		if (row < 0 || col < 0 || row > 5 || col > 6)
			return Piece.None;

		return _fields[row][col];
	}

	public boolean insertPieceIn(Piece piece, int col) {
		// check for valid column and piece
		if (col < 0 || col > 6 || piece == Piece.None)
			return false;

		// check whether column is already full
		if (_fields[0][col] != Piece.None)
			return false;

		// find lowest empty position in column
		int row = 0;
		while (row < 5 && _fields[row + 1][col] == Piece.None)
			++row;

		// insert piece here
		_fields[row][col] = piece;

		// update history
		_insertHistory[_numberOfInserts++] = col;
		return true;
	}

	public boolean undoInsertion() {
		// check if any insertions were made
		if (_numberOfInserts == 0)
			return false;

		// get last insertion's column and decrease number of insertions
		int col = _insertHistory[--_numberOfInserts];

		// find top-most piece in column
		int row = 0;
		while (_fields[row][col] == Piece.None)
			++row;

		// remove piece
		_fields[row][col] = Piece.None;
		return true;
	}

	public PlayerRows getRows(int maxMissing) {
		PlayerRows rows = new PlayerRows(maxMissing);
		PieceComparer cmp = new PieceComparer(maxMissing);

		for (int row = 0; row < 6; ++row) {
			for (int col = 0; col < 7; ++col) {
				if (row < 3) {
					for (int checkrow = row; checkrow < row + 4; ++checkrow) {
						if (!cmp.check(_fields[checkrow][col])) break;
					}
					if (cmp.getIsMatch()) rows.addRow(cmp.getPiece(), cmp.getSkipped());
					cmp.reset();
				}
				if (row < 3 && col < 4) {
					for (int checkrow = row, checkcol = col; checkcol < col + 4; ++checkrow, ++checkcol) {
						if (!cmp.check(_fields[checkrow][checkcol])) break;
					}
					if (cmp.getIsMatch()) rows.addRow(cmp.getPiece(), cmp.getSkipped());
					cmp.reset();
				}
				if (row > 3 && col < 4) {
					for (int checkrow = row, checkcol = col; checkcol < col + 4; --checkrow, ++checkcol) {
						if (!cmp.check(_fields[checkrow][checkcol])) break;
					}
					if (cmp.getIsMatch()) rows.addRow(cmp.getPiece(), cmp.getSkipped());
					cmp.reset();
				}
				if (col < 4) {
					for (int checkcol = col; checkcol < col + 4; ++checkcol) {
						if (!cmp.check(_fields[row][checkcol])) break;
					}
					if (cmp.getIsMatch()) rows.addRow(cmp.getPiece(), cmp.getSkipped());
					cmp.reset();
				}
			}
		}
		return rows;
	}
}
