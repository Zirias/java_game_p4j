package de.palmen_it.games.p4j.gui;

import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import de.palmen_it.games.p4j.gamelogic.*;

public class P4J {

	private class ColumnButton implements ActionListener {
		private int _col;
		private JButton _button;
		private P4J _owner;

		public JButton getButton() {
			return _button;
		}

		public ColumnButton(int col, P4J owner) {
			_col = col;
			_owner = owner;
			_button = new JButton("O");
			_button.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			_owner.ColumnClicked(_col);
		}
	}

	private class AIWorker extends SwingWorker<Void, Void> {

		private final Player _player;
		private final P4J _owner;

		public AIWorker(Player player, P4J owner) {
			_player = player;
			_owner = owner;
		}

		@Override
		protected Void doInBackground() throws Exception {
			_player.Move();
			return null;
		}

		@Override
		public void done() {
			_owner.aiDone();
		}
	}

	private JFrame _frame;
	private ColumnButton[] _buttons;
	private JLabel[][] _fields;
	private ImageIcon _red;
	private ImageIcon _black;

	private Board _board;
	private Player[] _players;
	private int _activePlayer;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					P4J window = new P4J();
					window._frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public P4J() {
		initialize();
	}

	public void ColumnClicked(int col) {
		HumanPlayer player = (HumanPlayer) _players[_activePlayer];
		player.setNextColumn(col);
		if (player.Move()) {
			_activePlayer = (_activePlayer == 0) ? 1 : 0;
			UpdateButtons();
			UpdateField();
			CheckWinner();
			if (!_players[_activePlayer].getIsHuman()) {
				new AIWorker(_players[_activePlayer], this).execute();
			}
		}
	}

	public void aiDone() {
		_activePlayer = (_activePlayer == 0) ? 1 : 0;
		UpdateButtons();
		UpdateField();
		CheckWinner();
	}

	private void CheckWinner() {
		if (_board.getNumberOfInserts() == 42)
		{
			JOptionPane.showMessageDialog(_frame, "Draw game!");
			_board.clear();
			UpdateField();
			return;
		}
		Piece winner = _board.getRows(0).getWinner();
		if (winner != Piece.None) {
			JOptionPane.showMessageDialog(_frame, winner.toString() + " wins!");
			_board.clear();
			UpdateField();
		}
	}

	private void UpdateField() {
		for (int row = 0; row < 6; ++row)
			for (int col = 0; col < 7; ++col)
				switch (_board.getPieceAt(row, col)) {
				case None:
					_fields[row][col].setIcon(null);
					break;
				case Black:
					_fields[row][col].setIcon(_black);
					break;
				case Red:
					_fields[row][col].setIcon(_red);
					break;
				}
	}
	
	private void UpdateButtons() {
		boolean isHuman = _players[_activePlayer].getIsHuman();
		for (ColumnButton b: _buttons) {
			b.getButton().setEnabled(isHuman);
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		_frame = new JFrame();
		_frame.setLocationByPlatform(true);
		Insets insets = _frame.getInsets();
		_frame.setSize(364 + insets.left + insets.right, 364 + insets.top
				+ insets.bottom);
		_frame.setResizable(false);
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_frame.getContentPane().setLayout(new GridLayout(7, 0, 0, 0));

		_buttons = new ColumnButton[7];
		for (int col = 0; col < 7; ++col) {
			ColumnButton bt = new ColumnButton(col, this);
			_buttons[col] = bt;
			_frame.add(bt.getButton());
		}

		_fields = new JLabel[6][7];
		for (int row = 0; row < 6; ++row)
			for (int col = 0; col < 7; ++col) {
				JLabel lbl = new JLabel();
				_fields[row][col] = lbl;
				_frame.add(lbl);
			}

		_black = new ImageIcon(getClass().getResource("black.png"));
		_red = new ImageIcon(getClass().getResource("red.png"));
		_board = new Board();
		_players = new Player[2];
		_players[0] = new HumanPlayer(_board, Piece.Black);
		AIPlayer ai = new AIPlayer(_board, Piece.Red);
		ai.setDifficulty(6);
		_players[1] = ai;
		_activePlayer = 0;
		
		UpdateButtons();
	}

}
