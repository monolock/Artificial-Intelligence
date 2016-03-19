package Game;

import java.util.List;

/**
 * Created by gejing on 3/6/14.
 */
public interface GameInterface {

	public String getToPlay();

	public Double getScore();

	public List<MoveInterface<?>> getPossibleMoves();

	public GameInterface moveToNext(MoveInterface<?> move);

	public void doMove(MoveInterface<?> move);

	public void undoMove();

	public boolean isTerminal();

	public boolean isDraw();

	public boolean isWin();
}
