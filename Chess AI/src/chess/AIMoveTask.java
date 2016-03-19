package chess;

import chesspresso.position.Position;
import javafx.concurrent.Task;

/**
 * Provided by Dartmouth College CS76 Artificial Intelligence
 */
public class AIMoveTask extends Task<Short> {
	
	private Position position = null;
	private ChessAI ai;
	
	public AIMoveTask(ChessAI ai, Position p) {
		super();
		position = p;	
		this.ai = ai;
		
	}

	@Override
	protected Short call() throws Exception {
		return ai.getMove(position);
	
	}
	
}
