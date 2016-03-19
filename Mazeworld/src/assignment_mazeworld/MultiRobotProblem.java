package assignment_mazeworld;

import java.util.ArrayList;
import java.util.Arrays;
import assignment_mazeworld.SearchProblem.SearchNode;
import assignment_mazeworld.SimpleMazeProblem.SimpleMazeNode;

/**
 * Created by gejing on 1/18/16.
 */
public class MultiRobotProblem extends InformedSearchProblem {

	// robot may not move this turn
	private static int actions[][] = {Maze.NO_MOVE, Maze.NORTH, Maze.EAST, Maze.SOUTH, Maze.WEST};

	// goal of this game
	private int[][] goal;

	// number of robots
	private int num;

	private Maze maze;

	public MultiRobotProblem(Maze m, int[][] START, int[][] GOAL) {
		startNode = new MultiRobotNode(START, 0, 0);
		goal = GOAL;
		num = START.length;
		maze = m;
	}

	public class MultiRobotNode implements SearchNode {

		private SearchNode parent;

		// location of the agent in the maze
		protected int[][] state;

		// how far the current node is from the start.
		private double cost;

		// which robot will move this round
		private int index;

		public MultiRobotNode(int[][] start, double c, int n) {
			state = start;
			cost = c;
			index = n;
		}

		public ArrayList<SearchNode> getSuccessors() {

			ArrayList<SearchNode> successors = new ArrayList<SearchNode>();

			for (int[] action: actions) {
				int xNew = state[index][0] + action[0];
				int yNew = state[index][1] + action[1];

				// check if robot is on a legal position
				// or two robots are on the same position
				if(maze.isLegal(xNew, yNew) && isSafe(xNew, yNew)) {
					int succState[][] = new int[num][2];

					for(int i = 0; i < num; i++){
						succState[i][0] = state[i][0];
						succState[i][1] = state[i][1];
					}
					succState[index][0] = xNew;
					succState[index][1] = yNew;

					// index may larger than num, set it to 0
					MultiRobotNode succ = new MultiRobotNode(succState, getCost() + 1, (index + 1) % num);

					successors.add(succ);
				}
			}
			return successors;
		}


		// check if two robots are on the same position
		private boolean isSafe(int x, int y) {
			for(int i = 0; i < num; i++)
				if(i != this.index)
					if(state[i][0] == x && state[i][1] == y)
						return false;
			return true;
		}

		@Override
		public boolean goalTest() {
			return (Arrays.deepEquals(state, goal));
		}

		@Override
		public boolean equals(Object other) {return (Arrays.deepEquals(state, ((MultiRobotNode) other).state));}

		@Override
		public int hashCode() {

			/*

			// This method will cause overflow if maze's size is too large

			String hash = "";
			for(int [] s : state){
				hash += Integer.toString(s[0]);
				hash += Integer.toString(s[1]);
			}
			return Integer.parseInt(hash);
			*/

			return Arrays.deepHashCode(state) + index;

		}

		/*
		@Override
		public String toString() {
			String result = "Maze state ";
			for (int[] s : state)
				result += s[0] + " " + s[1];
			result += " depth " + getCost();
			return result;
		}
		*/

		@Override
		public double getCost() {
			return cost;
		}

		public SearchNode getParent() {
			return this.parent;
		}

		public void setParent(SearchNode current) {
			this.parent = current;
		}

		// Return the state
		public int[][] getState() {
			return state;
		}


		@Override
		public double heuristic() {
			// use Euclidean distance or manhattan distance
			double distance = 0;
			for (int i = 0; i < num; i++)
				//distance += Math.pow((goal[i][0] - state[i][0]), 2) + Math.pow((goal[i][1] - state[i][1]), 2);
				distance += Math.abs(goal[i][0] - state[i][0]) + Math.abs(goal[i][1] - state[i][1]);
			//distance = Math.sqrt(distance);
			return distance;
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