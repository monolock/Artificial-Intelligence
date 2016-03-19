package assignment_mazeworld;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test: Blind Robot Problem
 * Provided by Dartmouth College CS76 Artificial Intelligence
 */

/**
 * Created by gejing on 1/18/16.
 */
public class BlindRobotDriver extends Application {
    Maze maze;

    // instance variables used for graphical display
    private static final int PIXELS_PER_SQUARE = 32;
    MazeView mazeView;
    List<AnimationPath> animationPathList;

    // some basic initialization of the graphics; needs to be done before
    //  runSearches, so that the mazeView is available
    private void initMazeView() {
        maze = Maze.readFromFile("simple.maz");
        //maze = Maze.readFromFile("BlindTest1.maz");

        animationPathList = new ArrayList<AnimationPath>();
        // build the board
        mazeView = new MazeView(maze, PIXELS_PER_SQUARE);

    }

    // assumes maze and mazeView instance variables are already available
    private void runSearches() {

        Set<List<Integer>> start = new HashSet<List<Integer>>();
        Set<List<Integer>> goal = new HashSet<List<Integer>>();

        for(int i = 0; i < maze.width; i++){
            for(int j = 0; j < maze.height; j++){
                if(maze.isLegal(i, j)){
                    List<Integer> position = new ArrayList<Integer>();
                    position.add(i);
                    position.add(j);
                    start.add(position);
                }
            }
        }

        List<Integer> finalPosition = new ArrayList<Integer>();
        finalPosition.add(1);
        finalPosition.add(3);
        goal.add(finalPosition);

        BlindRobotProblem mazeProblem = new BlindRobotProblem(maze, start, goal);

        List<SearchProblem.SearchNode> astarPath = mazeProblem.astarSearch();
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
        private List<SearchProblem.SearchNode> searchPath;
        private int currentMove = 0;

        boolean animationDone = true;

        public AnimationPath(MazeView mazeView, List<SearchProblem.SearchNode> path) {
            searchPath = path;
            BlindRobotProblem.BlindRobotNode firstNode = (BlindRobotProblem.BlindRobotNode) searchPath.get(0);
            Set<List<Integer>> state = firstNode.getState();
            int length = state.size(), i = 0;
            piece = new Node[length];
            for(List<Integer> position : state){
                int x = position.get(0), y = position.get(1);
                piece[i++] = mazeView.addMonoPiece(x, y);
            }
        }

        // try to do the next step of the animation. Do nothing if
        // the mazeView is not ready for another step.
        public void doNextMove() {

            // animationDone is an instance variable that is updated
            //  using a callback triggered when the current animation
            //  is complete
            if (currentMove < searchPath.size() && animationDone) {
                BlindRobotProblem.BlindRobotNode mazeNode = (BlindRobotProblem.BlindRobotNode) searchPath
                        .get(currentMove);
                Set<List<Integer>> state = mazeNode.getState();
                int i = 0;
                if(currentMove > 0) {
                    BlindRobotProblem.BlindRobotNode prevNode = (BlindRobotProblem.BlindRobotNode) searchPath.get(currentMove - 1);
                    Set<List<Integer>> prevState = prevNode.getState();
                    for (List<Integer> position : prevState) {
                        int x = position.get(0), y = position.get(1);
                        piece[i++] = mazeView.removePiece(x, y);
                        animateMove(piece[0], 0, 0);
                    }
                }
                i = 0;
                for(List<Integer> position : state){
                    int x = position.get(0), y = position.get(1);
                    piece[i++] = mazeView.addMonoPiece(x, y);
                    animateMove(piece[0], 0, 0);
                }
                currentMove++;
            }
        }

        // move the piece n by dx, dy cells
        public void animateMove(Node n, int dx, int dy) {
            animationDone = false;
            TranslateTransition tt = new TranslateTransition(
                    Duration.millis(1800), n);
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
