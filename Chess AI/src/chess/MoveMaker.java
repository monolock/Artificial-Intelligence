package chess;// a MoveMaker wraps the process that decides on a move given a position,
// whether the move is gotten from the UI, the server, or a local AI

import chesspresso.position.Position;
import javafx.concurrent.Worker;

/**
 * Provided by Dartmouth College CS76 Artificial Intelligence
 */
interface MoveMaker {

	public abstract void start(Position position);
	public void reset();   // set state to READY
	
	public Worker.State getState();
	public short getMove();
	
}
