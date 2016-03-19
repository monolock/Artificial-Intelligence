package Game2048;

import Game.GameInterface;
import Game.MoveInterface;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by gejing on 3/8/16.
 */
public class Board2048 implements GameInterface {

    private static final int TARGET = 2048;
    int[][] board = new int[4][4];
    private String player;
    private Stack<int[][]> boardHistory;

    private static final String MOVES_DIRECTION[] = {"UP", "DOWN", "LEFT", "RIGHT"};
    public static final String AI_PLAYER = "AI";
    public static final String COMPUTER = "Computer";

    public static final double[][] SCORE = {
            {8, 2.1, 1.9, 1.6},
            {-1.4,-1.4,-1.3,-1.1},
            {-1.9,-2.0,-2.1,-2.3},
            {0,0,0,0}
    };

    public static final Double smoothWeight = 0.2,
                               monoWeight   = 1.3,
                               emptyWeight  = 3.0,
                               maxWeight    = 1.0;


    public Board2048(String player, int[][] Board) {
        copyBoard(this.board, Board);
        this.boardHistory = new Stack<>();
        this.player = new String(player);
    }

    @Override
    public String getToPlay() {
        return this.player;
    }

    @Override
    public Double getScore(){
        if (isTerminal() && !isWin()) {
            return -Double.MAX_VALUE;
        }
        if (isWin()) {
            return Double.MAX_VALUE;
        }

        Double sum = 0.0;
        int max = board[0][0];

        for(int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] > 0) {
                    max = Math.max(max, board[i][j]);
                    sum += Math.log(board[i][j]) * SCORE[i][j];
                }
            }
        }

        sum += max * maxWeight;
        sum += availableSpace().size() * emptyWeight;
        sum += smoothValue() * smoothWeight;
        sum += monotonicity() * monoWeight;

        return sum;
    }

    @Override
    public List<MoveInterface<?>> getPossibleMoves() {
        List<MoveInterface<?>> nextMoves = new ArrayList<MoveInterface<?>>();
        if (!isTerminal()) {
            if (this.player.equals(AI_PLAYER)) {
                for(int i = 0; i < MOVES_DIRECTION.length; i++)
                    if(canMove(board, MOVES_DIRECTION[i]))
                        nextMoves.add(new MoveOrAdd(MOVES_DIRECTION[i]));
            } else {
                List<Pair<Integer, Integer>> openSpace = availableSpace();
                for(Pair<Integer, Integer> p : openSpace) {
                    nextMoves.add(new MoveOrAdd("2 " + Integer.toString(p.getKey()) +
                            " " + Integer.toString(p.getValue())));
                    nextMoves.add(new MoveOrAdd("4 " + Integer.toString(p.getKey()) +
                            " " + Integer.toString(p.getValue())));
                }
            }
        }
        return nextMoves;
    }

    @Override
    public GameInterface moveToNext(MoveInterface<?> move) {
        String nowplayer;
        if (this.player.equals(COMPUTER)) {
            nowplayer = AI_PLAYER;
        } else {
            nowplayer = COMPUTER;
        }
        int[][] nextBoard = transfer(move);
        return new Board2048(nowplayer, nextBoard);
    }

    @Override
    public void doMove(MoveInterface<?> move) {
        int[][] nowBoard = new int[4][4];
        copyBoard(nowBoard, this.board);
        boardHistory.push(nowBoard);
        int[][] nextBoard = transfer(move);
        copyBoard(this.board, nextBoard);
        if(this.player == AI_PLAYER)
            player = COMPUTER;
        else
            player = AI_PLAYER;
    }

    @Override
    public void undoMove() {
        int[][] lastBoard = boardHistory.pop();
        copyBoard(this.board, lastBoard);
        if(this.player == AI_PLAYER)
            player = COMPUTER;
        else
            player = AI_PLAYER;
    }

    @Override
    public boolean isTerminal() {
        if (isFull()) {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    if(i - 1 >= 0 && board[i - 1][j] == board[i][j])
                        return false;
                    if(i + 1 < board.length && board[i + 1][j] == board[i][j])
                        return false;
                    if(j - 1 >= 0 && board[i][j - 1] == board[i][j])
                        return false;
                    if(j + 1 < board[0].length && board[i][j + 1] == board[i][j])
                        return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isDraw() {
        return false;
    }

    @Override
    public boolean isWin() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if(board[i][j] >= TARGET)
                    return true;
            }
        }
        return false;
    }

    private void copyBoard(int[][] a, int[][] b){
        for(int i = 0; i < a.length; i++)
            for(int j = 0; j < a[0].length; j++)
                a[i][j] = b[i][j];
    }

    public boolean canMove(int[][] board, String s) {
        if(s.equals(MOVES_DIRECTION[0])) {
            for(int i = 1; i < board.length; i++)
                for(int j = 0; j < board[i].length; j++)
                    if(board[i][j] != 0 && (board[i - 1][j] == 0 || board[i][j] == board[i - 1][j]))
                        return true;
            return false;
        } else if(s.equals(MOVES_DIRECTION[1])) {
            for(int i = 0; i < board.length - 1; i++)
                for(int j = 0; j < board[i].length; j++)
                    if(board[i][j] != 0 && (board[i + 1][j] == 0 || board[i][j] == board[i + 1][j]))
                        return true;
            return false;
        } else if(s.equals(MOVES_DIRECTION[2])) {
            for(int i = 0; i < board.length; i++)
                for(int j = 1; j < board[i].length; j++)
                    if(board[i][j] != 0 && (board[i][j - 1] == 0 || board[i][j] == board[i][j - 1]))
                        return true;
            return false;
        } else {
            for(int i = 0; i < board.length; i++)
                for(int j = 0; j < board[i].length - 1; j++)
                    if(board[i][j] != 0 && (board[i][j + 1] == 0 || board[i][j] == board[i][j + 1]))
                        return true;
            return false;
        }
    }

    public boolean isFull() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if(board[i][j] == 0)
                    return false;
            }
        }
        return true;
    }


    private List<Pair<Integer, Integer>> availableSpace() {
        final List<Pair<Integer, Integer>> list = new ArrayList<Pair<Integer, Integer>>(16);
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if(board[i][j] == 0 && (i == 0 || j == 0 || i == 3 || j == 3))
                    list.add(new Pair<>(i, j));
            }
        }
        return list;
    }

    private int[][] transfer(MoveInterface<?> t) {
        String s = t.getMove().toString();

        int[][] nextBoard = new int[4][4];
        copyBoard(nextBoard, this.board);

        if(s.equals(MOVES_DIRECTION[0])) {
            moveUp(nextBoard);
        } else if(s.equals(MOVES_DIRECTION[1])) {
            moveDown(nextBoard);
        } else if(s.equals(MOVES_DIRECTION[2])) {
            moveLeft(nextBoard);
        } else if(s.equals(MOVES_DIRECTION[3])) {
            moveRight(nextBoard);
        } else {
            String[] add = s.split("\\s+");
            Integer num = Integer.parseInt(add[0]);
            Integer i = Integer.parseInt(add[1]);
            Integer j = Integer.parseInt(add[2]);
            nextBoard[i][j] = num;
        }
        return nextBoard;
    }

    private Double monotonicity() {
        Double[] rec = {0.0, 0.0};
        for(int i = 0; i < 4; i++){
            int cur = 0, next = 1;
            while(next < board.length){
                while(next < board.length && board[i][next] == 0){
                    next++;
                }
                if(next >= 4)
                    next--;
                Double curVal = board[i][cur] == 0 ? 0 : Math.log(board[i][cur]);
                Double nextVal = board[i][next] == 0 ? 0 : Math.log(board[i][next]);
                if(curVal > nextVal)
                    rec[0] += nextVal - curVal;
                else
                    rec[1] += curVal - nextVal;
                cur = next;
                next++;
            }
        }
        return Math.max(rec[0], rec[1]);
    }

    private Double smoothValue() {
        Double smooth = 0.0;
        for(int i = 0; i < board.length; i++)
            for(int j = 0; j < board[i].length; j++){
                if(board[i][j] != 0){
                    Double val = Math.log(board[i][j]);
                    int x = i + 1, y = j;
                    while(x < board.length && board[x][y] != 0){
                        x++;
                    }
                    if(x < board.length && board[x][y] != 0)
                        smooth -= Math.abs(val - Math.log(board[x][y]));

                    x = i;
                    y = j + 1;
                    while(y < board.length && board[x][y] != 0){
                        y++;
                    }
                    if(y < board.length && board[x][y] != 0)
                        smooth -= Math.abs(val - Math.log(board[x][y]));
                }
            }
        return smooth;
    }

    int findTarget(int array[], int x, int stop) {
        int t;
        if (x == 0) {
            return x;
        }
        for(t = x - 1; t >= 0; t--) {
            if (array[t] != 0) {
                if (array[t] != array[x]) {
                    return t + 1;
                }
                return t;
            } else {
                if (t == stop) {
                    return t;
                }
            }
        }
        return x;
    }

    int[] slideArray(int[] array) {
        int x,t,stop = 0;

        for (x = 0; x < 4; x++) {
            if (array[x] != 0) {
                t = findTarget(array, x, stop);
                if (t != x) {
                    if (array[t] == 0) {
                        array[t] = array[x];
                    } else if (array[t] == array[x]) {
                        array[t] *= 2;
                        stop = t + 1;
                    }
                    array[x] = 0;
                }
            }
        }

        return array;
    }

    void rotateBoard(int board[][]) {
        int i, j, n = 4;
        int tmp;
        for (i = 0; i< n / 2; i++) {
            for (j = i; j < n -i - 1; j++) {
                tmp = board[i][j];
                board[i][j] = board[j][n - i - 1];
                board[j][n-i-1] = board[n - i - 1][n - j - 1];
                board[n - i - 1][n - j - 1] = board[n - j - 1][i];
                board[n - j - 1][i] = tmp;
            }
        }
    }

    int[][] moveLeft(int[][] board) {
        int x;
        for (x = 0; x < 4; x++) {
            board[x] = slideArray(board[x]);
        }
        return board;
    }

    int[][]  moveUp(int[][] board) {
        rotateBoard(board);
        moveLeft(board);
        rotateBoard(board);
        rotateBoard(board);
        rotateBoard(board);
        return board;
    }

    int[][]  moveRight(int[][] board) {
        rotateBoard(board);
        rotateBoard(board);
        moveLeft(board);
        rotateBoard(board);
        rotateBoard(board);
        return board;
    }

    int[][]  moveDown(int[][] board) {
        rotateBoard(board);
        rotateBoard(board);
        rotateBoard(board);
        moveLeft(board);
        rotateBoard(board);
        return board;
    }

    // main only for test
    public static void main(String[] args) {

    }
}
