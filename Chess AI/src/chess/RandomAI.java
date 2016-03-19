package chess;

import chesspresso.position.Position;

import java.util.Random;

/**
 * Provided by Dartmouth College CS76 Artificial Intelligence
 */
public class RandomAI implements ChessAI {
	public short getMove(Position position) {
		if(position.isTerminal())
			System.out.print("Is Terminal!");
		short [] moves = position.getAllMoves();
		short move = moves[new Random().nextInt(moves.length)];
		return move;
	}
}
