package stringflow.cheatontimer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
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
	public Label timerLabel;
	public Rectangle visualCueRect;
	
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
		TimerEntry timer = new TimerEntry(timers.size(), name, offset, interval, numBeeps, addRemoveButton);
		timers.add(timer);
		timer.addAllElements(layout);
		timer.select();
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
