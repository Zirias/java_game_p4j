package de.palmen_it.games.p4j.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;

import de.palmen_it.games.p4j.gamelogic.*;

public class P4J implements ActionListener {

	private final JFrame _frame;
	private final ColumnButton[] _buttons;
	private final ImageContainer[][] _fields;
	private final JPopupMenu _menu;
	private final Image _red;
	private final Image _yellow;
	private final Image _empty;

	private final Board _board;
	private final Player _player1;
	private final Player _player2;
	private Player _activePlayer;
	private boolean _isAssisted;
	private boolean _assistentReady;

	private AIWorker _aiWorker;

	private final int MODE_1 = 1;
	private final int MODE_2 = 2;
	private final int MODE_1a = 3;

	private int _mode;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager
					.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					P4J window = new P4J();
					window._frame.pack();
					window._frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void ColumnClicked(int col) {
		if (_activePlayer.move(col)) {
			finishPlayerMove();
			updateButtons();
			if (CheckWinner())
				return;
			if (!_activePlayer.getIsHuman()) {
				_aiWorker = new AIWorker(_activePlayer, this);
				_aiWorker.execute();
			}
		}
	}

	private void finishPlayerMove()
	{
		setField(_activePlayer.getLastRow(), _activePlayer.getLastColumn(), _activePlayer.getPiece());
		_activePlayer = (_activePlayer == _player1) ? _player2 : _player1;		
	}
	
	public void aiDone() {
		_aiWorker = null;

		if (_isAssisted && !_assistentReady) {
			finishPlayerMove();
			_aiWorker = new AIWorker(_activePlayer, this);
			_aiWorker.execute();
			_assistentReady = true;
		} else {
			_assistentReady = false;
			if (!_isAssisted) {
				finishPlayerMove();
			}
			updateButtons();
			CheckWinner();
		}
	}

	private boolean CheckWinner() {
		if (_board.getNumberOfInserts() == 42) {
			JOptionPane.showMessageDialog(_frame, "Draw game!");
			restart();
			return true;
		}
		Piece winner = _board.getRows(0).getWinner();
		if (winner != Piece.EMPTY) {
			JOptionPane.showMessageDialog(_frame, winner.toString() + " wins!");
			restart();
			return true;
		}
		return false;
	}

	private void restart() {
		if (_aiWorker != null) {
			_aiWorker.cancel(true);
			try {
				_aiWorker.wait();
			} catch (InterruptedException e) {
			}
		}
		
		_board.clear();

		_isAssisted = _mode == MODE_1a;
		_assistentReady = false;
		_player2.setIsHuman(_mode == MODE_2);
		_aiWorker = null;

		updateField();

		if (_isAssisted) {
			_activePlayer = _player2;
			updateButtons();
			_activePlayer = _player1;
			_assistentReady = true;
			_aiWorker = new AIWorker(_activePlayer, this);
			_aiWorker.execute();
		} else {
			_activePlayer = _player1;
			updateButtons();			
		}
	}

	private void setField(int row, int column, Piece piece) {
		switch (piece) {
		case EMPTY:
			_fields[row][column].setImage(_empty);
			break;
		case YELLOW:
			_fields[row][column].setImage(_yellow);
			break;
		case RED:
			_fields[row][column].setImage(_red);
			break;
		}
	}

	private void updateField() {
		for (int row = 0; row < 6; ++row)
			for (int col = 0; col < 7; ++col)
				setField(row, col, _board.getPieceAt(row, col));
	}

	private void updateButtons() {
		boolean isHuman = _activePlayer.getIsHuman();
		boolean isUnassisted = isHuman && !_isAssisted;
		boolean isAssisted = isHuman && _isAssisted;

		int[] scores = null;
		if (isAssisted)
			scores = _activePlayer.getColumnScores();

		int i = 0;
		for (ColumnButton b : _buttons) {
			b.getButton().setEnabled(isHuman);
			b.getButton().setText(isUnassisted ? "O" : "");
			if (isAssisted) {
				int score = scores[i++];
				if (score < -100)
					score = -100;
				if (score > 100)
					score = 100;
				float red = 100;
				float green = 100;
				if (score < 0)
					green += score;
				if (score > 0)
					red -= score;
				Color c = new Color(red / 100, green / 100, 0);
				b.getButton().setBackground(c);
			} else {
				b.getButton().setBackground(null);
			}
		}
		if (isAssisted) {
			for (int col : _activePlayer.getBestColumns()) {
				_buttons[col].getButton().setText("O");
			}
		}
	}

	class PopupListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				_menu.show(e.getComponent(), e.getX(), e.getY());
			}

		}
	}

	/**
	 * Create the application.
	 */
	public P4J() {
		_frame = new JFrame();
		_frame.setLocationByPlatform(true);
		_frame.setResizable(false);
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_frame.setTitle("P4J - Puissance 4 Java");
		_frame.getContentPane().setLayout(new GridLayout(7, 7, 0, 0));
		_frame.addMouseListener(new PopupListener());

		_buttons = new ColumnButton[7];
		for (int col = 0; col < 7; ++col) {
			ColumnButton bt = new ColumnButton(col, this);
			_buttons[col] = bt;
			_frame.add(bt.getButton());
		}

		Dimension size = new Dimension(48, 48);
		_fields = new ImageContainer[6][7];
		for (int row = 0; row < 6; ++row)
			for (int col = 0; col < 7; ++col) {
				ImageContainer p = new ImageContainer();
				p.setPreferredSize(size);
				_fields[row][col] = p;
				_frame.add(p);
			}

		_menu = new JPopupMenu();
		JMenuItem item;
		item = new JMenuItem("Restart");
		item.setActionCommand("restart");
		item.addActionListener(this);
		_menu.add(item);
		_menu.addSeparator();
		JRadioButtonMenuItem rbItem;
		ButtonGroup group;
		group = new ButtonGroup();
		rbItem = new JRadioButtonMenuItem("2 Player");
		rbItem.setActionCommand("mode_2");
		rbItem.addActionListener(this);
		group.add(rbItem);
		_menu.add(rbItem);
		rbItem = new JRadioButtonMenuItem("1 Player");
		rbItem.setActionCommand("mode_1");
		rbItem.addActionListener(this);
		rbItem.setSelected(true);
		group.add(rbItem);
		_menu.add(rbItem);
		rbItem = new JRadioButtonMenuItem("1 Player assisted");
		rbItem.setActionCommand("mode_1a");
		rbItem.addActionListener(this);
		group.add(rbItem);
		_menu.add(rbItem);

		Toolkit tk = Toolkit.getDefaultToolkit();
		_red = tk.createImage(getClass().getResource("red.png"));
		_yellow = tk.createImage(getClass().getResource("yellow.png"));
		_empty = tk.createImage(getClass().getResource("empty.png"));
		
		_board = new Board();
		_aiWorker = null;

		_player1 = new Player(_board, Piece.RED, true);
		_player2 = new Player(_board, Piece.YELLOW, false);
		
		restart();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		boolean restart = false;
		if (command == "restart") {
			restart = true;
		} else if (command == "mode_1") {
			_mode = MODE_1;
			restart = true;
		} else if (command == "mode_2") {
			_mode = MODE_2;
			restart = true;
		} else if (command == "mode_1a") {
			_mode = MODE_1a;
			restart = true;
		}

		if (restart) {
			if (_board.getNumberOfInserts() == 0
					|| JOptionPane.showConfirmDialog(_frame, "Restart game?",
							"Restart", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				restart();
			}
		}
	}

}
