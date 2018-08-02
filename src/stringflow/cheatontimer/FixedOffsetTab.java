package stringflow.cheatontimer;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import stringflow.cheatontimer.timerFile.TimerFileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

public class FixedOffsetTab {
	
	private static final int ADD_BUTTON_BASE_X = 145;
	private static final int ADD_BUTTON_BASE_Y = 63;
	private static final int ADD_BUTTON_PADDING = 5;
	
	public static FixedOffsetTab instance;
	
	@FXML
	public AnchorPane layout;
	public TabPane tabPane;
	public Button addButton;
	public Button startButton;
	public Button resetButton;
	public Button settingsButton;
	public Button importButton;
	public Button exportButton;
	public Label timerLabel;
	public Rectangle visualCueRect;
	public ImageView pinIcon;
	public ImageView unpinIcon;
	
	public String saveLocationBuffer;
	
	private LinkedList<TimerEntry> timers = new LinkedList<>();
	private TimerEntry activeTimer;
	
	public FixedOffsetTab() {
		instance = this;
	}
	
	@FXML
	public void initialize() {
		addNewTimer("Initial", 2500, 500, 5, false);
		getTimer(0).select();
		recalcAddButton();
	}
	
	@FXML
	public void onAddButtonPress() {
		addNewTimer("Timer", 2500, 500, 5, true);
		getLastTimer().select();
	}
	
	@FXML
	public void onStartButtonPress() {
		if(activeTimer != null) {
			Timer.start();
		}
	}
	
	@FXML
	public void onResetButtonPress() {
		if(activeTimer != null) {
			Timer.reset();
		}
	}
	
	@FXML
	public void onSettingsButtonPress() {
		FlowTimer.settingsWindow.showAndWait();
	}
	
	@FXML
	public void onPinIconClicked() {
		setPin(!FlowTimer.mainFrame.isAlwaysOnTop());
	}
	
	public void setPin(boolean value) {
		FlowTimer.mainFrame.setAlwaysOnTop(value);
		if(value) {
			pinIcon.setVisible(false);
			unpinIcon.setVisible(true);
		} else {
			pinIcon.setVisible(true);
			unpinIcon.setVisible(false);
		}
	}
	
	@FXML
	public void onImportButtonPress() {
		FileChooser filechooser = new FileChooser();
		filechooser.setInitialDirectory(new File(saveLocationBuffer));
		filechooser.setTitle("Import Timers");
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("FTF files", "*.ftf", "*.ctf");
		filechooser.getExtensionFilters().add(extFilter);
		Stage stage = (Stage)layout.getScene().getWindow();
		File file = filechooser.showOpenDialog(stage);
		if(file == null) {
			return;
		}
		ArrayList<TimerEntry> loadedTimers = TimerFileUtil.loadTimers(file);
		if(loadedTimers != null) {
			for(TimerEntry timer : timers) {
				timer.removeAllElements(layout);
			}
			timers.clear();
			addAllTimers(loadedTimers);
			timers.get(0).select();
			AlertBox.showAlert(Alert.AlertType.INFORMATION, "FlowTimer", "Timers successfully imported.");
			saveLocationBuffer = file.getParent();
		}
	}
	
	@FXML
	public void onExportButtonPress() {
		FileChooser filechooser = new FileChooser();
		filechooser.setInitialDirectory(new File(saveLocationBuffer));
		filechooser.setTitle("Export Timers");
		Stage stage = (Stage)layout.getScene().getWindow();
		File file = filechooser.showSaveDialog(stage);
		if(file == null) {
			return;
		}
		if(!file.getName().endsWith(".ftf")) {
			file = new File(file.getAbsoluteFile() + ".ftf");
		}
		TimerFileUtil.saveTimers(file, timers);
		AlertBox.showAlert(Alert.AlertType.INFORMATION, "FlowTimer", "Timers successfully exported.");
		saveLocationBuffer = file.getParent();
	}
	
	public TimerEntry getSelectedTimer() {
		for(TimerEntry timer : timers) {
			if(timer.isSelected()) {
				return timer;
			}
		}
		return null;
	}
	
	public void setElements(boolean disabled) {
		addButton.setDisable(disabled);
		settingsButton.setDisable(disabled);
		importButton.setDisable(disabled);
		exportButton.setDisable(disabled);
		for(TimerEntry timer : timers) {
			timer.setElements(disabled);
		}
	}
	
	public void setTimerLabel(long time) {
		timerLabel.setText(String.format(Locale.ENGLISH, "%.3f", (double)time / 1000.0));
	}
	
	public TimerEntry getTimer(int index) {
		return timers.get(index);
	}
	
	public TimerEntry getLastTimer() {
		return timers.get(timers.size() - 1);
	}
	
	public void setActiveTimer(TimerEntry timer) {
		if(timer.isAllDataValid()) {
			Timer.calcCurrentTime(timer);
			setTimerLabel(timer.getOffsets()[0]);
			activeTimer = timer;
		} else {
			timerLabel.setText("Invalid");
			activeTimer = null;
		}
		startButton.setDisable(activeTimer == null);
		resetButton.setDisable(activeTimer == null);
	}
	
	public void addNewTimer(String name, long offset, long interval, int numBeeps, boolean addRemoveButton) {
		TimerEntry timer = new TimerEntry(timers.size(), name, new long[] {offset}, interval, numBeeps, addRemoveButton);
		timers.add(timer);
		timer.addAllElements(layout);
		timer.select();
		recalcAddButton();
	}
	
	public void addAllTimers(Collection<TimerEntry> inputTimers) {
		for(TimerEntry timer : inputTimers) {
			timers.add(timer);
			timer.addAllElements(layout);
		}
		recalcAddButton();
	}
	
	public void removeTimer(TimerEntry timer) {
		timers.remove(timer);
		timer.removeAllElements(layout);
		for(TimerEntry t : timers) {
			t.recalcPosition(timers.indexOf(t));
		}
		timers.get(0).select();
		recalcAddButton();
	}
	
	public void recalcAddButton() {
		double addButtonX = ADD_BUTTON_BASE_X;
		double addButtonY = ADD_BUTTON_BASE_Y + (addButton.getPrefHeight() + ADD_BUTTON_PADDING) * timers.size();
		addButton.setLayoutX(addButtonX);
		addButton.setLayoutY(addButtonY);
	}
}
