package chess;

import chesspresso.Chess;
import chesspresso.move.IllegalMoveException;
import chesspresso.position.Position;

import java.util.HashMap;

/**
 * Created by gejing on 2/7/16.
 */
public class MiniMaxAlphaBetaAI implements ChessAI {

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
    // record states we have visited
    public HashMap<Long, Entry> table = new HashMap<>();

    public  MiniMaxAlphaBetaAI(int depth) {
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
        // iterative deepening search
        for(int i = 1; i <= DEPTH && !stopSearch; i++) {
            visitedStates = 0;
            bestMove = alphabetaSearch(position, i);
            System.out.print("Search in depth " + i + "\n");
            System.out.print("States visited is " + visitedStates + "\n");
            System.out.print("The best move in this depth is " + bestMove + "\n");
        }
        System.out.print("Table size after this search: " + table.size() + "\n");
        System.out.print("The final move is: " + bestMove + "\n\n");
        table.clear();
        return bestMove;
    }

    public short alphabetaSearch(Position position, int maxDepth) {
        short[] moves = position.getAllMoves();
        int maxVal = Integer.MIN_VALUE;
        short bestMove = 0;

        for (short move : moves) {
            try {
                position.doMove(move);
            } catch (IllegalMoveException e) {
                e.printStackTrace();
            }

            // start from a minValue search
            int temp = minValue(position, 1, maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (maxVal <= temp){
                maxVal = temp;
                bestMove = move;
            }

            position.undoMove();
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

    private int maxValue(Position position, int depth, int maxDepth, int a, int b) {
        int alpha = a, beta = b;
        visitedStates++;
        if (cutoffTest(position, depth, maxDepth))
            return utility(position);
        int minVal = Integer.MIN_VALUE;
        for (short move : position.getAllMoves()) {
            try {
                position.doMove(move);

//                // use Transposition table
//                Long hashcode = position.getHashCode();
//                if(table.containsKey(hashcode)) {
//                    if(table.get(hashcode).depth < depth) {
//                        minVal = table.get(hashcode).value;
//                    } else {
//                        minVal = Math.max(minVal, minValue(position, depth + 1, maxDepth, alpha, beta));
//                        table.get(hashcode).setEntry(minVal, depth);
//                    }
//                } else {
//                    minVal = Math.max(minVal, minValue(position, depth + 1, maxDepth, alpha, beta));
//                    table.put(position.getHashCode(), new Entry(minVal, depth));
//                }

                // do not use Transposition table
                 minVal = Math.max(minVal, minValue(position, depth + 1, maxDepth, alpha, beta));
                
                position.undoMove();
                if(minVal >= beta)
                    return minVal;
                alpha = Math.max(minVal, alpha);
            } catch (IllegalMoveException e) {
                e.printStackTrace();
            }
        }
        return minVal;
    }

    private int minValue(Position position, int depth, int maxDepth, int a, int b) {
        int alpha = a, beta = b;
        visitedStates++;
        if (cutoffTest(position, depth, maxDepth))
            return utility(position);
        int maxVal = Integer.MAX_VALUE;
        for (short move : position.getAllMoves()) {
            try{
                position.doMove(move);

                // use Transposition table
//                Long hashcode = position.getHashCode();
//                if(table.containsKey(hashcode)) {
//                    if(table.get(hashcode).depth < depth) {
//                        maxVal = table.get(hashcode).value;
//                    } else {
//                        maxVal = Math.min(maxVal, maxValue(position, depth + 1, maxDepth, alpha, beta));
//                        table.get(hashcode).setEntry(maxVal, depth);
//                    }
//                } else {
//                    maxVal = Math.min(maxVal, maxValue(position, depth + 1, maxDepth, alpha, beta));
//                    table.put(position.getHashCode(), new Entry(maxVal, depth));
//                }

                // do not use Transposition table
                 maxVal = Math.min(maxVal, maxValue(position, depth + 1, maxDepth, alpha, beta));
                
                position.undoMove();
                // if max value is less than the lower bound, return it
                if(maxVal <= alpha)
                    return maxVal;
                // if max value is less than the upper bound, change the upper bound to that value
                beta = Math.min(beta, maxVal);
            }catch (IllegalMoveException e){
                e.printStackTrace();
            }
        }
        return maxVal;
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
          // use piece square values
//        int num = getMaterialByBoard(position);
//        AI ^= 1;
//        int opp = getMaterialByBoard(position);
//        AI ^= 1;
//        if(AI == Chess.WHITE)
//            num -= opp;
//        else
//            num = opp - num;
//        if(AI == position.getToPlay())
//            return position.getMaterial() + num;
//        else {
//
//            return (position.getMaterial() + num) * -1;
//        }
        if(AI == position.getToPlay())
            return position.getMaterial();
        else
            return position.getMaterial() * -1;
    }

    // This function returns the piece square values of
    // WHITE and BLACK
    // But it not works well in my evaluation function
    private int getMaterialByBoard(Position ThisPosition){
        Position position = new Position(ThisPosition);
        String[] splited = position.toString().split("\\s+");
        String ss = splited[0];
        String[] level = ss.split("/");
        boolean noQueen = true;
        for(int i = 0; i < level.length; i++){
            char[] board = level[i].toCharArray();
            String s = "";
            for(char c : board){
                if(!Character.isDigit(c)) {
                    s += Character.toString(c);
                    if(c == 'q' || c == 'Q')
                        noQueen = false;
                }
                else{
                    int j = c - '0';
                    for(int k = 0; k < j; k++)
                        s += "1";
                }
            }
            level[i] = s;
        }
        int num = 0;
        if(AI == Chess.BLACK){
            for(int i = 0, j = level.length - 1; i < j; i++, j--){
                String temp = level[j];
                level[j] = level[i];
                level[i] = temp;
            }
            for(int i = 0; i < 8; i++){
                char[] row = level[i].toCharArray();
                for(int m = 0, n = 7; m < n; m++, n--){
                    char temp = row[m];
                    row[m] = row[n];
                    row[n] = temp;
                }
                for(int j = 0; j < 8; j++){
                    char c = row[j];
                    if(c >= 'a' && c <= 'z'){
                        if(c == 'p')
                            num += PawnTable[i * 8 + j];
                        else if(c == 'r')
                            num += RookTable[i * 8 + j];
                        else if(c == 'n')
                            num += KnightTable[i * 8 + j];
                        else if(c == 'b')
                            num += BishopTable[i * 8 + j];
                        else if(c == 'q')
                            num += QueenTable[i * 8 + j];
                        else {
                            if(noQueen)
                                num += KingEndGameTable[i * 8 + j];
                            else
                                num += KingMiddleGameTable[i * 8 + j];
                        }
                    }
                }
            }
        } else {
            for(int i = 0; i < 8; i++){
                char[] row = level[i].toCharArray();
                for(int j = 0; j < 8; j++){
                    char c = row[j];
                    if(c >= 'A' && c <= 'Z'){
                        if(c == 'P')
                            num += PawnTable[i * 8 + j];
                        else if(c == 'R')
                            num += RookTable[i * 8 + j];
                        else if(c == 'N')
                            num += KnightTable[i * 8 + j];
                        else if(c == 'B')
                            num += BishopTable[i * 8 + j];
                        else if(c == 'Q')
                            num += QueenTable[i * 8 + j];
                        else {
                            if(noQueen)
                                num += KingEndGameTable[i * 8 + j];
                            else
                                num += KingMiddleGameTable[i * 8 + j];
                        }
                    }
                }
            }
        }
        return num;
    }

    private static int[] PawnTable = new int[] {
            0,  0,  0,  0,  0,  0,  0,  0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5,  5, 10, 25, 25, 10,  5,  5,
            0,  0,  0, 20, 20,  0,  0,  0,
            5, -5,-10,  0,  0,-10, -5,  5,
            5, 10, 10,-20,-20, 10, 10,  5,
            0,  0,  0,  0,  0,  0,  0,  0
    };


    private static int[] KnightTable = new int[] {
            -50,-40,-30,-30,-30,-30,-40,-50,
            -40,-20,  0,  0,  0,  0,-20,-40,
            -30,  0, 10, 15, 15, 10,  0,-30,
            -30,  5, 15, 20, 20, 15,  5,-30,
            -30,  0, 15, 20, 20, 15,  0,-30,
            -30,  5, 10, 15, 15, 10,  5,-30,
            -40,-20,  0,  5,  5,  0,-20,-40,
            -50,-40,-30,-30,-30,-30,-40,-50,
    };


    private static int[] BishopTable = new int[] {
            -20,-10,-10,-10,-10,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5, 10, 10,  5,  0,-10,
            -10,  5,  5, 10, 10,  5,  5,-10,
            -10,  0, 10, 10, 10, 10,  0,-10,
            -10, 10, 10, 10, 10, 10, 10,-10,
            -10,  5,  0,  0,  0,  0,  5,-10,
            -20,-10,-10,-10,-10,-10,-10,-20,
    };

    private static int[] RookTable = new int[] {
            0,  0,  0,  0,  0,  0,  0,  0,
            5, 10, 10, 10, 10, 10, 10,  5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            0,  0,  0,  5,  5,  0,  0,  0,
    };

    private static int[] QueenTable = new int[] {
            -20,-10,-10, -5, -5,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5,  5,  5,  5,  0,-10,
            -5,  0,  5,  5,  5,  5,  0, -5,
            0,  0,  5,  5,  5,  5,  0, -5,
            -10,  5,  5,  5,  5,  5,  0,-10,
            -10,  0,  5,  0,  0,  0,  0,-10,
            -20,-10,-10, -5, -5,-10,-10,-20,
    };


    private static int[] KingMiddleGameTable = new int[] {
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -20,-30,-30,-40,-40,-30,-30,-20,
            -10,-20,-20,-20,-20,-20,-20,-10,
            20, 20,  0,  0,  0,  0, 20, 20,
            20, 30, 10,  0,  0, 10, 30, 20,
    };


    private static int[] KingEndGameTable = new int[] {
            -50,-40,-30,-20,-20,-30,-40,-50,
            -30,-20,-10,  0,  0,-10,-20,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-30,  0,  0,  0,  0,-30,-30,
            -50,-30,-30,-30,-30,-30,-30,-50,
    };
}
