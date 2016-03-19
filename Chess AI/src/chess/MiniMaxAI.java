package chess;

import chesspresso.move.IllegalMoveException;
import chesspresso.position.Position;

/**
 * Created by gejing on 2/6/16.
 */
public class MiniMaxAI implements ChessAI {

    // max search depth
    private int DEPTH;
    // AI player uses white or black?
    private static int AI;
    // utility of win, lose and draw state
    private static int WIN_UTILITY = Integer.MAX_VALUE;
    private static int LOSE_UTILITY = Integer.MIN_VALUE;
    private static int DRAW_UTILITY = 0;
    // a flag tells us if we could stop iterative deepening search
    private boolean stopSearch;
    // record how many states we have visited (may contain duplicates if we don't have a check function)
    private int visitedStates;

    public  MiniMaxAI(int depth) {
        // max depth in the search
        this.DEPTH = depth;
    }

    public short getMove(Position ThisPosition) {
        if(ThisPosition.isTerminal()) {
            System.out.print("Game over!!!");
            while(true) { }
        }

        // we'd better use a new Object since this is a multi threads program
        // if two AIs are playing against each other, it will cause collision
        Position position = new Position(ThisPosition);
        // indicates AI is black or white
        AI = position.getToPlay();
        // we will stop searching if we find that
        // 1. in a specific depth, the best move could guarantee a win
        // 2. in a specific depth, the best move will cause a lose (we will definitely lose)
        stopSearch = false;
        short bestMove = 0;

        for(int i = 1; i <= DEPTH && !stopSearch; i++) {
            visitedStates = 0;
            bestMove = minimaxSearch(position, i);
            System.out.print("Search in depth " + i + "\n");
            System.out.print("States visited is " + visitedStates + "\n");
            System.out.print("The best move in this depth is " + bestMove + "\n");
        }

        System.out.print("The final move is: " + bestMove + "\n\n");
        return bestMove;
    }

    public short minimaxSearch(Position position, int maxDepth) {
        int maxVal = Integer.MIN_VALUE;
        short bestMove = 0;

        for (short move : position.getAllMoves()) {
            try {
                position.doMove(move);
                // start from a minValue search
                int temp = minValue(position, 1, maxDepth);
                if (maxVal <= temp){
                    maxVal = temp;
                    bestMove = move;
                }
                position.undoMove();
            } catch (IllegalMoveException e) {
                e.printStackTrace();
            }
        }

        // if we find that we will definitely win or definitely lose
        // then we know we don't need to search in a deeper depth
        if((maxVal == WIN_UTILITY || maxVal == LOSE_UTILITY)) {
            stopSearch = true;
            System.out.print("Reach end in depth " + maxDepth + ". We will definitely " +
                    (maxVal == WIN_UTILITY ? "Win " : "Lose ") + "this game.\n");
        }

        return bestMove;
    }

    private int maxValue(Position position, int depth, int maxDepth) {
        visitedStates++;
        if (cutoffTest(position, depth, maxDepth)) {
            return utility(position);
        }
        int v = Integer.MIN_VALUE;
        for (short a : position.getAllMoves()) {
            try {
                position.doMove(a);
            } catch (IllegalMoveException e) {
                e.printStackTrace();
            }
            v = Integer.max(v, minValue(position, depth + 1, maxDepth));
            position.undoMove();
        }
        return v;
    }

    private int minValue(Position position, int depth, int maxDepth) {
        visitedStates++;
        if (cutoffTest(position, depth, maxDepth)){
            return utility(position);
        }
        int v = Integer.MAX_VALUE;
        for (short move : position.getAllMoves()) {
            try{
                position.doMove(move);
                v = Math.min(v, maxValue(position, depth + 1, maxDepth));
                position.undoMove();
            }catch (IllegalMoveException e){
                e.printStackTrace();
            }
        }
        return v;
    }

    private boolean cutoffTest(Position position, int depth, int maxDepth) {
        return position.isTerminal() || depth == maxDepth;
    }

    private int utility(Position position) {
        // If reach terminal state
        if (position.isTerminal()) {
            if (position.isStaleMate())
                return DRAW_UTILITY;
            else if (position.isMate() && AI == position.getToPlay())
                return LOSE_UTILITY;
            else if (position.isMate() && AI != position.getToPlay())
                return WIN_UTILITY;
        }
        return evaluation(position);
    }

    private int evaluation(Position position) {
        if(AI == position.getToPlay())
            return position.getMaterial();
        else
            return position.getMaterial() * -1;
    }
}
