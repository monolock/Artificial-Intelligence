package Search;

import Game.GameInterface;
import Game.MoveInterface;

import java.util.HashMap;

/**
 * Created by gejing on 3/19/16.
 */
public class MiniMaxAlphaBetaAI {
    Integer defaultDepth = 3;
    SearchNode now;

    public MiniMaxAlphaBetaAI(GameInterface now) {
        super();
        this.now = new SearchNode(now);
    }

    private class SearchNode {
        GameInterface stage;
        HashMap<MoveInterface<?>, SearchNode> nextStages = new HashMap<MoveInterface<?>, SearchNode>();

        public SearchNode(GameInterface stage) {
            super();
            this.stage = stage;
        }
    }

    private Double getScore(SearchNode now, Integer searchDepth, Double beta) {
        Double score;
        if (now.stage.getToPlay().equals("AI")) {
            score = Double.MIN_VALUE;
        } else {
            score = Double.MAX_VALUE;
        }
        if (now.stage.getPossibleMoves().isEmpty()) {
            return 0.0;
        } else if (searchDepth == 0) {
            for (MoveInterface<?> move : now.stage.getPossibleMoves()) {
                GameInterface next;
                if (now.nextStages.get(move) != null) {
                    next = now.nextStages.get(move).stage;
                } else {
                    next = now.stage.moveToNext(move);
                    now.nextStages.put(move, new SearchNode(next));
                }
                if (next.getScore() > score) {
                    score = next.getScore();
                }
            }
        } else {
            for (MoveInterface<?> move : now.stage.getPossibleMoves()) {
                if (now.nextStages.get(move) == null) {
                    now.nextStages.put(move,
                            new SearchNode(now.stage.moveToNext(move)));
                }
                if (now.stage.getToPlay().equals("AI")) {
                    Double nextScore = getScore(now.nextStages.get(move),
                            searchDepth, score)
                            + now.nextStages.get(move).stage.getScore();
                    if (nextScore > score) {
                        score = nextScore;
                    }
                } else {
                    Double nextScore = getScore(now.nextStages.get(move),
                            searchDepth - 1, score);
                    if (nextScore < score) {
                        score = nextScore;
                    }
                    if (score <= beta){
                        return score;
                    }
                }
            }
        }
        return score;
    }

    public MoveInterface<?> getNextMove(MoveInterface<?> playerMove) {
        return getNextMove(playerMove, this.defaultDepth);
    }

    public MoveInterface<?> getNextMove(MoveInterface<?> playerMove,
                                        Integer searchDepth) {
        Double maxScore = Double.MIN_VALUE;
        MoveInterface<?> nextMove = null;
        if (playerMove != null) {
            if (now.nextStages.get(playerMove) == null) {
                GameInterface next = now.stage.moveToNext(playerMove);
                now.nextStages.put(playerMove, new SearchNode(next));
            } else {
                now = now.nextStages.get(playerMove);
            }
        }
        for (MoveInterface<?> move : now.stage.getPossibleMoves()) {
            if (now.nextStages.get(move) == null) {
                now.nextStages.put(move,
                        new SearchNode(now.stage.moveToNext(move)));
            }
            Double moveScore = getScore(now.nextStages.get(move), searchDepth, Double.MIN_VALUE)
                    + now.nextStages.get(move).stage.getScore();
            if (moveScore > maxScore) {
                maxScore = moveScore;
                nextMove = move;
            }
        }
        now = now.nextStages.get(nextMove);
        return nextMove;
    }
}
