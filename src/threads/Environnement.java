package threads;

import java.util.*;

import javafx.application.Application;
import visual.Direction;
import visual.Window;
import javafx.stage.Stage;
import javafx.application.Platform;
import search.Actions;;

public class Environnement extends Thread {
	private int[][] grid	; // values 0: empty, 1: dust, 2: jewel, 3:both
    private int score;
    private int actions;
    private int dusts;
    private int suckedDusts;
    private int jewels;
    private int pickedJewels;
    private int suckedJewels;
    private Window window;
	private Thread t;

    //Constructor
    public Environnement() throws InterruptedException {
        super();
        //Filling the grid with 0 values
        this.grid = new int[5][5];
        for (int[] row : this.grid) {
            Arrays.fill(row, 0);
        }
        this.score = 0;
        this.actions = 0;
        this.dusts = 0;
        this.suckedDusts = 0;
        this.jewels = 0;
        this.pickedJewels = 0;
        this.suckedJewels = 0;
        this.execWindow();
        Window.printDirection("Creating the Environnement");
    }
    
    //Setting up and launching the GUI Thread
    private void execWindow() throws InterruptedException {
    	this.window = ((Window) this.window);
        this.t = new Thread(() -> Application.launch(Window.class));
        this.t.start();

        Window.awaitFXToolkit();
        Window.initRobot(4, 4);
    }

    public int getScore(){
        return this.score;
    }
    
    //Randomly generating Dust or Jewel (10% to generate Jewel)
    private void generate() throws InterruptedException {
        double prob = Math.random();
        if (prob >= 0.10 && this.isGridFullOfDust() == false) {
            Random rand = new Random();
            int x;
            int y;
            do {
                x = rand.nextInt(5);
                y = rand.nextInt(5);
            } while (this.grid[x][y] == 1 || this.grid[x][y] == 3);
            this.changeGridState(x, y, Actions.NewDust);
        } else if (prob < 0.10 && this.isGridFullOfJewels() == false) {
            Random rand = new Random();
            int x;
            int y;
            do {
                x = rand.nextInt(5);
                y = rand.nextInt(5);
            } while (this.grid[x][y] == 2 || this.grid[x][y] == 3);
            this.changeGridState(x, y, Actions.NewJewel);
        }
    }

    //Updating the score
    //Each Action/Energy used : -1
    //Dust generated : -1
    //Dust sucked : +5
    //Jewel picked : +10
    //Jewel sucked : -50
    public void updateScore() {
        this.score = (this.actions * -1) + (this.suckedDusts * 5) + (this.dusts * -1) + (this.pickedJewels * 10)
                + (this.suckedJewels * -50);
        int[] scores = {this.actions, this.dusts, this.suckedDusts, this.jewels, this.suckedJewels, this.pickedJewels, this.score};
        Window.setScore(scores);
    }

    //Used by Agent Sensors to determinate his state
    public boolean isGridEmpty(){
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (this.grid[i][j] != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    //Used to know if any more Dust can be geenrated on the Grid
    private boolean isGridFullOfDust() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (this.grid[i][j] == 0 || this.grid[i][j] == 2) {
                    return false;
                }
            }
        }
        return true;
    }

    //Same than previous method, for Jewel
    private boolean isGridFullOfJewels() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (this.grid[i][j] == 0 || this.grid[i][j] == 1) {
                    return false;
                }
            }
        }
        return true;
    }

    //Return a deep copy of the Grid
    public int[][] getGrid() {
        return deepCopyOfGrid(this.grid);
    }

    //Create a deep copy for any given Grid
    public static int[][] deepCopyOfGrid(int[][] grid){
        int[][] newGrid = new int[5][5];
        for (int i = 0; i < 5; i++)
            newGrid[i] = Arrays.copyOf(grid[i], 5);
        return newGrid;
    }

    //Change the state of a given Grid case, depending on the action given
    public boolean changeGridState(int x, int y, Actions keyword) throws InterruptedException {
        // keywords : newDust -> +1, if dust already here does nothing
        // newJewel -> +2, if jewel already does nothing
        // suck -> check value, set it to 0, check if jewel was sucked.
        // pick -> check value, -2 if jewel was here, otherwise nothing
        // sleep -> agent is idle, decrease actions
        if (keyword == Actions.NewDust && (this.grid[x][y] != 1 || this.grid[x][y] != 3)) {
            this.dusts++;
            this.grid[x][y] += 1;
            //Used to change the display on the GUI, and send a message to print in the scrollBox
            Platform.runLater(
                () -> {
                    Window.printDirection("New dust at ("+x+";"+y+")");
                    Window.addDirt(x, y);
                }
            );
        } else if (keyword == Actions.NewJewel && (this.grid[x][y] != 2 || this.grid[x][y] != 3)) {
            this.jewels++;
            this.grid[x][y] += 2;
            Platform.runLater(() -> {
                Window.printDirection("New jewel at ("+x+";"+y+")");
                Window.addJewel(x, y);
            });
        } else if (keyword == Actions.Suck) {
            this.actions++;
            if (this.grid[x][y] == 3) {
                this.suckedDusts++;
                this.suckedJewels++;
                this.grid[x][y] = 0;
                Platform.runLater(
                    () -> {
                        Window.removeDirt(x, y);
                        Window.removeJewel(x, y);
                    }
                );
                this.updateScore();
                return true;
            } else if (this.grid[x][y] == 2) {
                this.suckedJewels++;  	
                this.grid[x][y] = 0;
                Platform.runLater(
                    () -> {
                        Window.removeJewel(x, y);
                    }
                );
                this.updateScore();
                return true;
            } else if (this.grid[x][y] == 1) {
                this.suckedDusts++;
                this.grid[x][y] = 0;
                Platform.runLater(
                    () -> {
                        Window.removeDirt(x, y);
                    }
                );
                this.updateScore();
                return true;
            }
        } else if (keyword == Actions.Pick) {
            this.actions++;
            if (this.grid[x][y] == 2 || this.grid[x][y] == 3) {
                this.pickedJewels++;
                this.grid[x][y] -= 2;
                Platform.runLater(
                    () -> {
                        Window.removeJewel(x, y);
                    }
                );
                this.updateScore();
                return true;
            }
        } else if (keyword == Actions.Sleep){
            this.actions--;
        }
        this.updateScore();
        return false;
    }

    //Get a set of position and a direction, return to the Agent his new position
    public int[] moveAgentOnGrid(int x, int y, Actions keyword) throws InterruptedException {
        if (keyword == Actions.MoveTop) {
            this.actions++;
            int[] newPos = {x,y-1};
            Platform.runLater(
                    () -> {
                        Window.moveRobot(Direction.Top);
                    }
                );
            return newPos;
        } else if (keyword == Actions.MoveDown) {
            this.actions++;
            int[] newPos = {x,y+1};
            Platform.runLater(
                    () -> {
                        Window.moveRobot(Direction.Down);
                    }
                );
            return newPos;
        } else if (keyword == Actions.MoveRight) {
            this.actions++;
            int[] newPos = {x+1,y};
            Platform.runLater(
                    () -> {
                        Window.moveRobot(Direction.Right);
                    }
                );
            return newPos;
        } else if (keyword == Actions.MoveLeft) {
            this.actions++;
            int[] newPos = {x-1,y};
            Platform.runLater(
                    () -> {
                        Window.moveRobot(Direction.Left);
                    }
                );
            return newPos;
        }
        return null;
    }

    //Thread main loop
    @Override
    public void run() {
        while(true){
            try {
                this.generate();
			} catch (InterruptedException e1) {
                ;
            }
            
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}