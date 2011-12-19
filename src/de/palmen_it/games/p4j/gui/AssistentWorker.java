package de.palmen_it.games.p4j.gui;

import javax.swing.SwingWorker;

import de.palmen_it.games.p4j.gamelogic.AIPlayer;

public class AssistentWorker extends SwingWorker<Void, Void> {
	private final AIPlayer _player;
	private final P4J _owner;

	public AssistentWorker(AIPlayer player, P4J owner) {
		_player = player;
		_owner = owner;
	}

	@Override
	protected Void doInBackground() throws Exception {
		_player.ComputeBestColumns();
		return null;
	}

	@Override
	public void done() {
		_owner.aiDone();
	}

}
