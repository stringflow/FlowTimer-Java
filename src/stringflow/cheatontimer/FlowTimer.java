package stringflow.cheatontimer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import stringflow.cheatontimer.audio.AudioEngine;
import stringflow.cheatontimer.audio.IAudioFile;
import stringflow.cheatontimer.audio.tinySound.TinySoundAudioEngine;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FlowTimer extends Application {
	
	public static final AudioEngine audioEngine = new TinySoundAudioEngine();
	
	public static IAudioFile audioFile;
	public static Stage mainFrame;
	public static Stage settingsWindow;
	
	public void start(Stage primaryStage) throws Exception {
		mainFrame = primaryStage;
		setupNativeHook();
		GlobalScreen.registerNativeHook();
		GlobalScreen.addNativeKeyListener(new GlobalScreenListener());
		audioEngine.init();
		primaryStage.setTitle("FlowTimer");
		primaryStage.setResizable(false);
		primaryStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/layout/mainFrame.fxml")), 430, 285));
		primaryStage.show();
		audioFile = audioEngine.loadAudioFileInternal("/audio/pop.wav");
		primaryStage.setOnCloseRequest(e -> {
			audioEngine.dispose();
			System.exit(0);
		});
		
		settingsWindow = new Stage();
		settingsWindow.setTitle("FlowTimer Settings");
		settingsWindow.setResizable(false);
		settingsWindow.setScene(new Scene(FXMLLoader.load(getClass().getResource("/layout/settingsWindow.fxml")), 265, 160));
		settingsWindow.initModality(Modality.APPLICATION_MODAL);
	}
	
	private void setupNativeHook() {
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);
		logger.setUseParentHandlers(false);
	}
	
	public static void main(String args[]) {
		launch(args);
	}
}