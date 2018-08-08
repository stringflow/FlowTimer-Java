package stringflow.flowtimer;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;

import java.util.Arrays;

public class TimerEntry {
	
	public static ToggleGroup buttonGroup = new ToggleGroup();
	
	private static final int BASE_X = 145;
	private static final int BASE_Y = 33;
	private static final int LABEL_WIDTH = 55;
	private static final int LABEL_HEIGHT = 20;
	private static final int PADDING_X = 5;
	private static final int PADDING_Y = 5;
	
	private static final int RADIO_BUTTON_BASE_X = 125;
	private static final int RADIO_BUTTON_BASE_Y = 33;
	private static final int RADIO_BUTTON_WIDTH = 20;
	private static final int RADIO_BUTTON_HEIGHT = 20;
	
	private static final int REMOVE_BUTTON_BASE_X = 387;
	private static final int REMOVE_BUTTON_BASE_Y = 33;
	private static final int REMOVE_BUTTON_WIDTH = 30;
	private static final int REMOVE_BUTTON_HEIGHT = 20;
	
	private RadioButton radioButton;
	private TextField nameField;
	private TextField offsetField;
	private TextField intervalField;
	private TextField numBeepsField;
	private Button removeButton;
	
	public TimerEntry(int index, String name, long offsets[], long interval, int numBeeps, boolean addRemoveButton) {
		radioButton = new RadioButton();
		nameField = new TextField(String.valueOf(name));
		offsetField = new TextField(Util.convertArrayToString(offsets, "/"));
		intervalField = new TextField(String.valueOf(interval));
		numBeepsField = new TextField(String.valueOf(numBeeps));
		if(addRemoveButton) {
			removeButton = new Button("-");
			removeButton.setOnAction(e -> {
				FixedOffsetTab.instance.removeTimer(this);
				FixedOffsetTab.instance.getTimer(0).select();
				buttonGroup.getToggles().remove(radioButton);
			});
		}
		recalcPosition(index);
		radioButton.setToggleGroup(buttonGroup);
		radioButton.setFocusTraversable(false);
		radioButton.setOnAction(e -> select());
		radioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue) {
					if(!Timer.isTimerRunning) {
						Platform.runLater(() -> select());
					}
				}
			}
		});
		nameField.textProperty().addListener((observable, oldValue, newValue) -> select());
		offsetField.textProperty().addListener((observable, oldValue, newValue) -> select());
		intervalField.textProperty().addListener((observable, oldValue, newValue) -> select());
		numBeepsField.textProperty().addListener((observable, oldValue, newValue) -> select());
	}
	
	public void recalcPosition(int index) {
		int xIndex = 0;
		setFieldPosition(nameField, xIndex++, index);
		setFieldPosition(offsetField, xIndex++, index);
		setFieldPosition(intervalField, xIndex++, index);
		setFieldPosition(numBeepsField, xIndex++, index);
		
		radioButton.setMinSize(RADIO_BUTTON_WIDTH, RADIO_BUTTON_HEIGHT);
		radioButton.setPrefSize(RADIO_BUTTON_WIDTH, RADIO_BUTTON_HEIGHT);
		radioButton.setMaxSize(RADIO_BUTTON_WIDTH, RADIO_BUTTON_HEIGHT);
		double radioButtonY = RADIO_BUTTON_BASE_Y + (RADIO_BUTTON_HEIGHT + PADDING_Y) * index;
		radioButton.setLayoutX(RADIO_BUTTON_BASE_X);
		radioButton.setLayoutY(radioButtonY);
		if(removeButton != null) {
			removeButton.setMinSize(REMOVE_BUTTON_WIDTH, REMOVE_BUTTON_HEIGHT);
			removeButton.setPrefSize(REMOVE_BUTTON_WIDTH, REMOVE_BUTTON_HEIGHT);
			removeButton.setMaxSize(REMOVE_BUTTON_WIDTH, REMOVE_BUTTON_HEIGHT);
			double removeButtonY = REMOVE_BUTTON_BASE_Y + (removeButton.getPrefHeight() + PADDING_Y) * index;
			removeButton.setLayoutX(REMOVE_BUTTON_BASE_X);
			removeButton.setLayoutY(removeButtonY);
		}
	}
	
	private void setFieldPosition(TextField field, int xIndex, int yIndex) {
		int xPos = BASE_X + (LABEL_WIDTH + PADDING_X) * xIndex;
		int yPos = BASE_Y + (LABEL_HEIGHT + PADDING_Y) * yIndex;
		field.setMinSize(LABEL_WIDTH, LABEL_HEIGHT);
		field.setPrefSize(LABEL_WIDTH, LABEL_HEIGHT);
		field.setMaxSize(LABEL_WIDTH, LABEL_HEIGHT);
		field.setLayoutX(xPos);
		field.setLayoutY(yPos);
	}
	
	public void addAllElements(AnchorPane parent) {
		parent.getChildren().addAll(nameField, offsetField, intervalField, numBeepsField, radioButton);
		if(removeButton != null) {
			parent.getChildren().add(removeButton);
		}
	}
	
	public void removeAllElements(AnchorPane parent) {
		parent.getChildren().removeAll(nameField, offsetField, intervalField, numBeepsField, radioButton);
		if(removeButton != null) {
			parent.getChildren().remove(removeButton);
		}
	}
	
	public void select() {
		radioButton.setSelected(true);
		FixedOffsetTab.instance.setActiveTimer(this);
	}
	
	public void setElements(boolean disabled) {
		nameField.setDisable(disabled);
		offsetField.setDisable(disabled);
		intervalField.setDisable(disabled);
		numBeepsField.setDisable(disabled);
		if(removeButton != null) {
			removeButton.setDisable(disabled);
		}
	}
	
	public String getName() {
		return nameField.getText();
	}
	
	public long[] getOffsets() {
		return Arrays.stream(offsetField.getText().split("/")).mapToLong(Long::parseLong).toArray();
	}
	
	public long getInterval() {
		return Long.valueOf(intervalField.getText());
	}
	
	public int getNumBeeps() {
		return Integer.valueOf(numBeepsField.getText());
	}
	
	public boolean isSelected() {
		return radioButton.isSelected();
	}
	
	public boolean isAllDataValid() {
		if(!offsetField.getText().matches("^\\d+(\\/\\d+)*$")) {
			return false;
		}
		if(!intervalField.getText().matches("^0*[1-9]\\d*$")) {
			return false;
		}
		if(!numBeepsField.getText().matches("^0*[1-9]\\d*$")) {
			return false;
		}
		String offsets[] = offsetField.getText().split("/");
		for(int i = 0; i < offsets.length; i++) {
			if(Long.valueOf(offsets[i]) <= 0) {
				return false;
			}
		}
		return true;
	}
}