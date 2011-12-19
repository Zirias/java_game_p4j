package de.palmen_it.games.p4j.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

class ColumnButton implements ActionListener {
	private int _col;
	private JButton _button;
	private P4J _owner;

	public JButton getButton() {
		return _button;
	}

	public ColumnButton(int col, P4J owner) {
		_col = col;
		_owner = owner;
		_button = new JButton("");
		_button.setEnabled(false);
		_button.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		_owner.ColumnClicked(_col);
	}
}