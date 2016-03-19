package Game2048;

import Game.MoveInterface;

/**
 * Created by gejing on 3/8/16.
 */
public class MoveOrAdd implements MoveInterface {
    private String move;

    public MoveOrAdd(String Move) {
        super();
        this.move = Move;
    }

    @Override
    public String getMove() {
        return move;
    }

    @Override
    public String toString() {
        return move;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((move == null) ? 0 : move.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MoveOrAdd other = (MoveOrAdd) obj;
        if (move == null) {
            if (other.move != null)
                return false;
        } else if (!move.equals(other.move))
            return false;
        return true;
    }
}
