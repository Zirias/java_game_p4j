package de.palmen_it.games.p4j.gui;

import javax.swing.SwingWorker;

import de.palmen_it.games.p4j.gamelogic.Player;

class AIWorker extends SwingWorker<Void, Void> {

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