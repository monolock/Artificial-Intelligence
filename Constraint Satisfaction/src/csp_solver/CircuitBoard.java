package csp_solver;

import javafx.util.Pair;

import java.util.*;

/**
 * Created by gejing on 2/19/16.
 */
public class CircuitBoard {
    private ConstraintSatisfactionProblem solver = new ConstraintSatisfactionProblem();
    private int rows, cols;
    private int[][] rec;

    public CircuitBoard(int ROWS, int COLS, int[][] REC) {
        rows = ROWS;
        cols = COLS;
        rec = REC;

        List<Set<Integer>> list = new LinkedList<>();

        // Create variables
        // since component can't rotate
        // we use a 'position' to record the left top of this component
        // position = row * COLS_NUMBER + col

        // Notice: the upper left corner of the board has coordinates (0, 0)
        for (int i = 0; i < rec[0].length; i++) {

            Set<Integer> domain = new HashSet<>();

            for (int j = 0; j < rows * cols; j++) {

                // calculate row and column of this component
                int row = j / cols, col = j % cols;
                // check overflow
                if(row + rec[0][i] <= rows && col + rec[1][i] <= cols)
                    domain.add(j);
            }

            list.add(domain);
            solver.addVariable(i, domain);
        }

        // Create constraints
        for (int i = 0; i < rec[0].length - 1; i++) {
            for (int j = i + 1; j < rec[0].length; j++) {

                Set<Pair<Integer, Integer>> constraint = new HashSet<>();

                for(Integer first : list.get(i)){

                    int l = first % cols, u = first / cols,
                            r = l + rec[1][i], d = u + rec[0][i];

                    for(Integer second : list.get(j)){

                        int ll = second % cols, uu = second / cols,
                                rr = ll + rec[1][j], dd = uu + rec[0][j];
                        int left = Math.max(l, ll), up = Math.max(u, uu),
                                right = Math.min(r, rr), down = Math.min(d, dd);

                        // check overlap
                        if(right <= left || down <= up)
                            constraint.add(new Pair<>(first, second));
                    }
                }
                solver.addConstraint(i, j, constraint);
            }
        }
    }

    public char[][] solve() {

        Map<Integer, Integer> solution = solver.solve();
        if (solution == null)
            return null;

        char[][] result = new char[rows][cols];

        for(int i = 0; i < rows; i++)
            for(int j = 0; j < cols; j++)
                result[i][j] = '.';

        for (Integer p : solution.keySet()) {
            char asc = (char)('a' + p);
            Integer pos = solution.get(p);
            int row = pos / cols, col = pos % cols;
            for(int i = 0; i < rec[0][p]; i++)
                for(int j = 0; j < rec[1][p]; j++)
                    result[row + i][col + j] = asc;
        }
        return result;
    }

    public static final void main(String[] args) {
        int ROWS = 4, COLS = 10;
        int[][] REC = new int[][]{{2, 2, 3, 1, 2, 1, 1},
                                  {3, 5, 2, 7, 1, 7, 2}};
//        int ROWS = 3, COLS = 10;
//        int[][] REC = new int[][]{{2, 2, 3, 1},
//                                  {3, 5, 2, 7}};
        char[][] solution = new CircuitBoard(ROWS, COLS, REC).solve();
        if(solution != null)
            for(int i = 0; i < solution.length; i++)
                System.out.println(Arrays.toString(solution[i]));
        else
            System.out.println("Solution not found");
    }
}
