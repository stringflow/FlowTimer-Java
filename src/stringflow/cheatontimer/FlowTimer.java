package stringflow.cheatontimer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.ini4j.Wini;
import org.jnativehook.GlobalScreen;
import stringflow.cheatontimer.audio.AudioEngine;
import stringflow.cheatontimer.audio.BeepSound;
import stringflow.cheatontimer.audio.javax.JavaXAudioEngine;
import stringflow.cheatontimer.audio.tinySound.TinySoundAudioEngine;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlowTimer extends Application {
	
	public static AudioEngine audioEngine;
	
	public static Stage mainFrame;
	public static Stage settingsWindow;
	public static BeepSound currentBeep;
	
	public static boolean visualCue = false;
	public static boolean audioCue = false;
	
	private static File folder = new File(System.getenv("APPDATA") + "/FlowTimer");
	private static File settingsFile = new File(System.getenv("APPDATA") + "/FlowTimer/settings.ini");
	
	public void start(Stage primaryStage) throws Exception {
		Wini ini = loadIniAndSetAudioEngine();
		String mode = ini.get("Audio", "mode");
		audioCue = mode.toLowerCase().contains("audio");
		visualCue = mode.toLowerCase().contains("visual");
		mainFrame = primaryStage;
		setupNativeHook();
		GlobalScreen.registerNativeHook();
		GlobalScreen.addNativeKeyListener(new GlobalScreenListener());
		primaryStage.setTitle("FlowTimer");
		primaryStage.setResizable(false);
		primaryStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/layout/mainFrame.fxml")), 430, 335));
		primaryStage.show();
		primaryStage.focusedProperty().addListener((arg0, arg1, arg2) -> { if(arg2 && !Timer.isTimerRunning) FixedOffsetTab.instance.setActiveTimer(FixedOffsetTab.instance.getSelectedTimer());});
		primaryStage.setOnCloseRequest(e -> {
			audioEngine.dispose();
			try {
				ini.load(settingsFile);
				ini.clear();
				Profile.Section generalSection = ini.add("General");
				generalSection.put("pin", mainFrame.isAlwaysOnTop());
				generalSection.put("saveLocation", FixedOffsetTab.instance.saveLocationBuffer);
				Profile.Section audioSection = ini.add("Audio");
				audioSection.put("engine", SettingsWindow.instance.javaxAudioEngine.isSelected() ? "java" : "tinysound");
				audioSection.put("file", currentBeep.name());
				audioSection.put("volume", SettingsWindow.instance.volumeSlider.valueProperty().getValue() / 100.0);
				audioSection.put("mode", SettingsWindow.instance.choiceBox.getValue());
				Profile.Section inputSection = ini.add("Input");
				inputSection.put("start1", SettingsWindow.instance.startInputField1.getKeyCode());
				inputSection.put("start2", SettingsWindow.instance.startInputField2.getKeyCode());
				inputSection.put("reset1", SettingsWindow.instance.resetInputField1.getKeyCode());
				inputSection.put("reset2", SettingsWindow.instance.resetInputField2.getKeyCode());
				inputSection.put("up1", SettingsWindow.instance.upInputField1.getKeyCode());
				inputSection.put("up2", SettingsWindow.instance.upInputField2.getKeyCode());
				inputSection.put("down1", SettingsWindow.instance.downInputField1.getKeyCode());
				inputSection.put("down2", SettingsWindow.instance.downInputField2.getKeyCode());
				inputSection.put("start1Name", SettingsWindow.instance.startInputField1.getParentField().getText());
				inputSection.put("start2Name", SettingsWindow.instance.startInputField2.getParentField().getText());
				inputSection.put("reset1Name", SettingsWindow.instance.resetInputField1.getParentField().getText());
				inputSection.put("reset2Name", SettingsWindow.instance.resetInputField2.getParentField().getText());
				inputSection.put("up1Name", SettingsWindow.instance.upInputField1.getParentField().getText());
				inputSection.put("up2Name", SettingsWindow.instance.upInputField2.getParentField().getText());
				inputSection.put("down1Name", SettingsWindow.instance.downInputField1.getParentField().getText());
				inputSection.put("down2Name", SettingsWindow.instance.downInputField2.getParentField().getText());
				inputSection.put("globalStartReset", SettingsWindow.instance.globalStartReset.isSelected());
				inputSection.put("globalUpDown", SettingsWindow.instance.globalUpDown.isSelected());
				ini.store(settingsFile);
			} catch(IOException e1) {
				e1.printStackTrace();
			}
			System.exit(0);
		});
		
		settingsWindow = new Stage();
		settingsWindow.setTitle("FlowTimer Settings");
		settingsWindow.setResizable(false);
		settingsWindow.setScene(new Scene(FXMLLoader.load(getClass().getResource("/layout/settingsWindow.fxml")), 265, 160));
		settingsWindow.initModality(Modality.APPLICATION_MODAL);
		
		// Check for 1.4.1 settings
		if(ini.get("General") == null) {
			ini.add("General");
		}
		Profile.Section generalSection = ((Profile.Section) ini.get("General"));
		if(!generalSection.containsKey("pin")) {
			generalSection.add("pin", false);
		}
		if(!generalSection.containsKey("saveLocation")) {
			generalSection.add("saveLocation", System.getProperty("user.home") + "\\Desktop");
		}
		ini.store();
	
		FixedOffsetTab.instance.setPin(Boolean.valueOf(String.valueOf(generalSection.get("pin"))));
		FixedOffsetTab.instance.saveLocationBuffer = String.valueOf(generalSection.get("saveLocation"));
		
		// load rest of the settings file
		(audioEngine instanceof JavaXAudioEngine ? SettingsWindow.instance.javaxAudioEngine : SettingsWindow.instance.tinySoundAudioEngine).setSelected(true);
		currentBeep = BeepSound.fromString(ini.get("Audio", "file"));
		if(currentBeep == BeepSound.BEEP) {
			SettingsWindow.instance.beepAudioFile.setSelected(true);
		} else if(currentBeep == BeepSound.DING) {
			SettingsWindow.instance.dingAudioFile.setSelected(true);
		} else if(currentBeep == BeepSound.TICK) {
			SettingsWindow.instance.tickAudioFile.setSelected(true);
		} else {
			SettingsWindow.instance.popAudioFile.setSelected(true);
		}
		audioEngine.setVolume(0);
		currentBeep.play();
		SettingsWindow.instance.startInputField1.set(ini.get("Input", "start1Name"), Integer.valueOf(String.valueOf(ini.get("Input", "start1"))));
		SettingsWindow.instance.startInputField2.set(ini.get("Input", "start2Name"), Integer.valueOf(String.valueOf(ini.get("Input", "start2"))));
		SettingsWindow.instance.resetInputField1.set(ini.get("Input", "reset1Name"), Integer.valueOf(String.valueOf(ini.get("Input", "reset1"))));
		SettingsWindow.instance.resetInputField2.set(ini.get("Input", "reset2Name"), Integer.valueOf(String.valueOf(ini.get("Input", "reset2"))));
		SettingsWindow.instance.upInputField1.set(ini.get("Input", "up1Name"), Integer.valueOf(String.valueOf(ini.get("Input", "up1"))));
		SettingsWindow.instance.upInputField2.set(ini.get("Input", "up2Name"), Integer.valueOf(String.valueOf(ini.get("Input", "up2"))));
		SettingsWindow.instance.downInputField1.set(ini.get("Input", "down1Name"), Integer.valueOf(String.valueOf(ini.get("Input", "down1"))));
		SettingsWindow.instance.downInputField2.set(ini.get("Input", "down2Name"), Integer.valueOf(String.valueOf(ini.get("Input", "down2"))));
		SettingsWindow.instance.globalStartReset.setSelected(Boolean.valueOf(String.valueOf(ini.get("Input", "globalStartReset"))));
		SettingsWindow.instance.globalUpDown.setSelected(Boolean.valueOf(String.valueOf(ini.get("Input", "globalUpDown"))));
		SettingsWindow.instance.volumeSlider.setValue(Double.valueOf(String.valueOf(ini.get("Audio", "volume"))) * 100.0);
		SettingsWindow.instance.choiceBox.setValue(mode);
		SettingsWindow.instance.setUpListeners();
		audioEngine.setVolume(Float.valueOf(String.valueOf(ini.get("Audio", "volume"))));
	}
	
	private static Wini loadIniAndSetAudioEngine() throws Exception {
		if(!folder.exists()) {
			folder.mkdirs();
		}
		Wini ini;
		if(!settingsFile.exists()) {
			ini = new Wini();
			Config config = new Config();
			config.setMultiOption(true);
			config.setMultiSection(true);
			ini.setConfig(config);
			Profile.Section audioSection = ini.add("Audio");
			audioSection.add("engine", "tinysound");
			audioSection.add("file", "beep");
			audioSection.add("volume", 1.0f);
			audioSection.add("mode", "Audio");
			Profile.Section inputSection = ini.add("Input");
			inputSection.add("start1", -1);
			inputSection.add("start2", -1);
			inputSection.add("reset1", -1);
			inputSection.add("reset2", -1);
			inputSection.add("up1", 57416);
			inputSection.add("up2", -1);
			inputSection.add("down1", 57424);
			inputSection.add("down2", -1);
			inputSection.add("start1Name", "");
			inputSection.add("start2Name", "");
			inputSection.add("reset1Name", "");
			inputSection.add("reset2Name", "");
			inputSection.add("up1Name", "Up");
			inputSection.add("up2Name", "");
			inputSection.add("down1Name", "Down");
			inputSection.add("down2Name", "");
			inputSection.add("globalStartReset", false);
			inputSection.add("globalUpDown", false);
			ini.store(settingsFile);
		}
		ini = new Wini(settingsFile);
		if(ini.get("Audio", "engine").equalsIgnoreCase("java")) {
			audioEngine = new JavaXAudioEngine();
		} else {
			audioEngine = new TinySoundAudioEngine();
		}
		audioEngine.init();
		return ini;
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