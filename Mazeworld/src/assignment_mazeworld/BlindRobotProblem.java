package assignment_mazeworld;

import java.util.*;

/**
 * Created by gejing on 1/17/16.
 */
public class BlindRobotProblem extends InformedSearchProblem {

    private static int actions[][] = {Maze.NORTH, Maze.EAST, Maze.SOUTH, Maze.WEST};

    private Set<List<Integer>> start = new HashSet<List<Integer>>();
    private Set<List<Integer>> goal = new HashSet<List<Integer>>();

    private Maze maze;

    public BlindRobotProblem(Maze m, Set<List<Integer>> START, Set<List<Integer>> GOAL) {
        startNode = new BlindRobotNode(START, 0);
        start = START;
        goal = GOAL;
        maze = m;
    }

    // node class used by searches.  Searches themselves are implemented
    // in SearchProblem.
    public class BlindRobotNode implements SearchNode {

        private SearchNode parent;

        // location of the agent in the maze
        protected  Set<List<Integer>> state;

        // how far the current node is from the start.  Not strictly required
        //  for uninformed search, but useful information for debugging,
        //  and for comparing paths
        private double cost;

        public BlindRobotNode(Set<List<Integer>> start, double c) {
            state = start;
            cost = c;
        }

        public SearchNode getParent() { return this.parent; }

        public void setParent(SearchNode current) { this.parent = current; }

        public ArrayList<SearchNode> getSuccessors() {

            ArrayList<SearchNode> successors = new ArrayList<SearchNode>();

            for (int[] action: actions) {

                Set<List<Integer>> nextStates = new HashSet<List<Integer>>();
                for (List<Integer> s : state){
                    int newX = s.get(0) + action[0];
                    int newY = s.get(1) + action[1];
                    if (maze.isLegal(newX, newY)) {

                        List<Integer> possibleState = new ArrayList<Integer>();
                        possibleState.add(newX);
                        possibleState.add(newY);
                        nextStates.add(possibleState);
                    }
                    else
                        nextStates.add(s);
                }

                SearchNode succ = new BlindRobotNode(nextStates, getCost() + 1);
                successors.add(succ);
            }
            return successors;
        }

        @Override
        public boolean goalTest() {
             return state.equals(goal);
            // return state.size() == goal.size();
        }

        // Return the state
        public Set<List<Integer>> getState() {
            return state;
        }

        // an equality test is required so that visited sets in searches
        // can check for containment of states
        @Override
        public boolean equals(Object other) {
            return state.equals(((BlindRobotNode) other).state);
        }

        @Override
        public int hashCode() {
            return state.hashCode();
        }

        /*
        @Override
        public String toString() {
            return new String("Maze state " + state[0] + ", " + state[1] + " "
                    + " depth " + getCost());
        }
        */

        @Override
        public double getCost() {
            return cost;
        }

        @Override
        public double heuristic() {
            double dx = 0, dy = 0, sum = 0, n = state.size();

            for(List<Integer> i : state){
                for(List<Integer> j : goal){
                    dx = Math.abs(i.get(0) - j.get(0));
                    dy = Math.abs(i.get(1) - j.get(1));
                    sum += dx + dy;
                }
            }

            return sum / n;
        }

        @Override
        public int compareTo(SearchNode o) {
            return (int) Math.signum(priority() - o.priority());
        }

        @Override
        public double priority() {
            return heuristic() + getCost();
        }

    }
}
