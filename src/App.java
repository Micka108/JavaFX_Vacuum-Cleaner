import threads.Environnement;
import visual.Window;
import javafx.application.Application;

public class App {
	
	Window Window = new Window();
	
    public static void main(String[] args) throws Exception {
        System.out.println("Starting");
        Environnement env = new Environnement(); //creating and starting the Environnement Thread
        env.start();
        //Application.launch(Window.class, args);
    }
}