package assignment_mazeworld;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import assignment_mazeworld.MultiRobotProblem.MultiRobotNode;
import assignment_mazeworld.SearchProblem.SearchNode;
import assignment_mazeworld.SimpleMazeProblem.SimpleMazeNode;

/**
 * Test: Multi Robots Problem
 * Provided by Dartmouth College CS76 Artificial Intelligence
 */

/**
 * Created by gejing on 1/18/16.
 */
public class MultiRobotDriver extends Application {

	Maze maze;

	// instance variables used for graphical display
	private static final int PIXELS_PER_SQUARE = 30;
	MazeView mazeView;
	List<AnimationPath> animationPathList;

	// some basic initialization of the graphics; needs to be done before 
	//  runSearches, so that the mazeView is available
	private void initMazeView() {
		// maze = Maze.readFromFile("MultiTest1.maz");
		// maze = Maze.readFromFile("MultiTest2.maz");
		// maze = Maze.readFromFile("MultiTest3.maz");
		maze = Maze.readFromFile("MultiTest4.maz");
		// maze = Maze.readFromFile("MultiTestLargeMaze.maz");
		// PIXELS_PER_SQUARE = 12; // if you choose large maze
		animationPathList = new ArrayList<AnimationPath>();
		// build the board
		mazeView = new MazeView(maze, PIXELS_PER_SQUARE);

	}

	// assumes maze and mazeView instance variables are already available
	private void runSearches() {
		// test 1
		// int[][] start = new int[][] {{0,0}, {0,1}, {0,2}};
		// int[][] goal = new int[][] {{5,4},{5,2}, {5,3}};
		// test 2
		// int[][] start = new int[][] {{3,0}, {2,0}, {4,0}};
		// int[][] goal = new int[][] {{2,6},{4,6}, {3,6}};
		// test 3
		// int[][] start = new int[][] {{3,0}, {2,0}, {4,0}};
		// int[][] goal = new int[][] {{3,6},{4,6}, {2,6}};
		// test 4
		int[][] start = new int[][] {{0,0}, {6,0},{0,6}, {6,6}};
		int[][] goal = new int[][] {{2,3},{3,4}, {3,2},{4,3}};
		// test 5
		// int[][] start = new int[][] {{0,0}, {1,0}, {2,0}};
		// int[][] goal = new int[][] {{39,26},{38,26}, {37,26}};
		// 8 puzzle
		// int[][] start = new int[][] { {0,0}, {0,1}, {0,2}, {1,0}, {1,1}, {1,2},{2,0},{2,1}};
		// int[][] goal = new int[][] {{1,1}, {1,2},{2,2},{2,1},{0,0}, {0,1}, {0,2}, {1,0} };

		MultiRobotProblem mazeProblem = new MultiRobotProblem(maze, start, goal);

		List<SearchNode> astarPath = mazeProblem.astarSearch();
		animationPathList.add(new AnimationPath(mazeView, astarPath));
		System.out.println("A*:  ");
		mazeProblem.printStats();
	}


	public static void main(String[] args) {
		launch(args);
	}

	// javafx setup of main view window for mazeworld
	@Override
	public void start(Stage primaryStage) {

		initMazeView();

		primaryStage.setTitle("CS 76 Mazeworld");

		// add everything to a root stackpane, and then to the main window
		StackPane root = new StackPane();
		root.getChildren().add(mazeView);
		primaryStage.setScene(new Scene(root));

		primaryStage.show();

		// do the real work of the driver; run search tests
		runSearches();

		// sets mazeworld's game loop (a javafx Timeline)
		Timeline timeline = new Timeline(1.0);
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().add(
				new KeyFrame(Duration.seconds(.05), new GameHandler()));
		timeline.playFromStart();

	}

	// every frame, this method gets called and tries to do the next move
	//  for each animationPath.
	private class GameHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent e) {
			// System.out.println("timer fired");
			for (AnimationPath animationPath : animationPathList) {
				// note:  animationPath.doNextMove() does nothing if the
				//  previous animation is not complete.  If previous is complete,
				//  then a new animation of a piece is started.
				animationPath.doNextMove();
			}
		}
	}

	// each animation path needs to keep track of some information:
	// the underlying search path, the "piece" object used for animation,
	// etc.
	private class AnimationPath {
		private Node[] piece;
		private List<SearchNode> searchPath;
		private int currentMove = 0;

		private int[] lastX;
		private int[] lastY;

		boolean animationDone = true;

		public AnimationPath(MazeView mazeView, List<SearchNode> path) {
			searchPath = path;
			MultiRobotNode firstNode = (MultiRobotNode) searchPath.get(0);

			int[][] state = firstNode.getState();
			int length = state.length;
			piece = new Node[length];
			lastX = new int[length];
			lastY = new int[length];
			for (int i = 0; i < length; i++){
				piece[i] = mazeView.addPiece(state[i][0], state[i][1]);
				lastX[i] = state[i][0];
				lastY[i] = state[i][1];
			}
		}

		// try to do the next step of the animation. Do nothing if
		// the mazeView is not ready for another step.
		public void doNextMove() {

			// animationDone is an instance variable that is updated
			//  using a callback triggered when the current animation
			//  is complete
			if (currentMove < searchPath.size() && animationDone) {
				MultiRobotNode mazeNode = (MultiRobotNode) searchPath
						.get(currentMove);

				int[][] state = mazeNode.getState();

				for (int i = 0; i < state.length; i++) {
					int dx = state[i][0] - lastX[i];
					int dy = state[i][1] - lastY[i];
					if(dx != 0 || dy != 0)
						animateMove(piece[i], dx, dy);
					lastX[i] = state[i][0];
					lastY[i] = state[i][1];
				}

				currentMove++;
			}

		}

		// move the piece n by dx, dy cells
		public void animateMove(Node n, int dx, int dy) {
			animationDone = false;
			TranslateTransition tt = new TranslateTransition(
					Duration.millis(800), n);
			tt.setByX(PIXELS_PER_SQUARE * dx);
			tt.setByY(-PIXELS_PER_SQUARE * dy);
			// set a callback to trigger when animation is finished
			tt.setOnFinished(new AnimationFinished());

			tt.play();

		}

		// when the animation is finished, set an instance variable flag
		//  that is used to see if the path is ready for the next step in the
		//  animation
		private class AnimationFinished implements EventHandler<ActionEvent> {
			@Override
			public void handle(ActionEvent event) {
				animationDone = true;
			}
		}
	}
}