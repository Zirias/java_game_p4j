package de.palmen_it.games.p4j.gui;

import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import de.palmen_it.games.p4j.gamelogic.*;

public class P4J implements ActionListener {

	private JFrame _frame;
	private ColumnButton[] _buttons;
	private JLabel[][] _fields;
	private JPopupMenu _menu;
	private ImageIcon _red;
	private ImageIcon _black;

	private Board _board;
	private Player[] _players;
	private int _activePlayer;
	private AIPlayer _assistent;
	private boolean _assistentReady;

	private AIWorker _aiWorker;
	private AssistentWorker _assistentWorker;
	
	private final int MODE_1 = 1;
	private final int MODE_2 = 2;
	private final int MODE_1a = 3;
	
	private int _mode;
	
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
			if (CheckWinner()) return;
			if (!_players[_activePlayer].getIsHuman()) {
				_aiWorker = new AIWorker(_players[_activePlayer], this);
				_aiWorker.execute();
			}
		}
	}

	public void aiDone() {
		_aiWorker = null;
		_assistentWorker = null;
		
		if (_assistent != null && !_assistentReady) {
			_assistentWorker = new AssistentWorker(_assistent, this);
			_assistentWorker.execute();
			_assistentReady = true;
		} else {
			_assistentReady = false;
			_activePlayer = (_activePlayer == 0) ? 1 : 0;
			UpdateButtons();
			UpdateField();
			CheckWinner();
		}
	}

	private boolean CheckWinner() {
		if (_board.getNumberOfInserts() == 42)
		{
			JOptionPane.showMessageDialog(_frame, "Draw game!");
			restart();
			return true;
		}
		Piece winner = _board.getRows(0).getWinner();
		if (winner != Piece.None) {
			JOptionPane.showMessageDialog(_frame, winner.toString() + " wins!");
			restart();
			return true;
		}
		return false;
	}

	private void restart() {
		if (_assistentWorker != null) { 
			_assistentWorker.cancel(true);
			try {
				_assistentWorker.wait();
			} catch (InterruptedException e) {
			}
		}
		if (_aiWorker != null) {
			_aiWorker.cancel(true);
			try {
				_aiWorker.wait();
			} catch (InterruptedException e) {
			}			
		}
		
		_players[0] = new HumanPlayer(_board, Piece.Black);
		if (_mode == MODE_1a) {
			_assistent = new AIPlayer(_board, Piece.Black);
		} else {
			_assistent = null;
		}
		if (_mode == MODE_2) {
			_players[1] = new HumanPlayer(_board, Piece.Red);
		} else {
			_players[1] = new AIPlayer(_board, Piece.Red);
		}
		_activePlayer = 0;
		_assistentWorker = null;
		_aiWorker = null;
		
		_board.clear();
		UpdateField();
		
		if (_assistent == null) {
			UpdateButtons();
		} else {
			_assistentReady = true;
			_activePlayer = 1;
			_assistentWorker = new AssistentWorker(_assistent, this);
			_assistentWorker.execute();
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
		boolean isUnassisted = isHuman && _assistent == null;
		for (ColumnButton b: _buttons) {
			b.getButton().setEnabled(isHuman);
			b.getButton().setText(isUnassisted ? "O" : "");
		}
		if (isHuman && _assistent != null) {
			for (int col: _assistent.getBestColumns()) {
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
		_frame.addMouseListener(new PopupListener());

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

		_black = new ImageIcon(getClass().getResource("black.png"));
		_red = new ImageIcon(getClass().getResource("red.png"));
		_board = new Board();
		_players = new Player[2];
		_aiWorker = null;
		_assistentWorker = null;
		
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
			if (_board.getNumberOfInserts() == 0 ||
					JOptionPane.showConfirmDialog(_frame,
					"Restart game?", "Restart",
					JOptionPane.OK_CANCEL_OPTION)
					== JOptionPane.OK_OPTION) {
				restart();
			}
		}
	}

}
