package stringflow.cheatontimer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import stringflow.cheatontimer.audio.BeepSound;

import java.util.Arrays;
import java.util.List;

public class SettingsWindow {
	
	public static SettingsWindow instance = null;
	
	public ToggleGroup audioEngineGroup;
	public ToggleGroup audioFileGroup;
	
	public InputField startInputField1;
	public InputField startInputField2;
	public InputField resetInputField1;
	public InputField resetInputField2;
	public InputField upInputField1;
	public InputField upInputField2;
	public InputField downInputField1;
	public InputField downInputField2;
	
	public InputField focuedField;
	
	@FXML
	public TextField startInputField1Internal;
	public TextField startInputField2Internal;
	public TextField resetInputField1Internal;
	public TextField resetInputField2Internal;
	public TextField upInputField1Internal;
	public TextField upInputField2Internal;
	public TextField downInputField1Internal;
	public TextField downInputField2Internal;
	public CheckBox globalStartReset;
	public CheckBox globalUpDown;
	public RadioButton javaxAudioEngine;
	public RadioButton tinySoundAudioEngine;
	public RadioButton beepAudioFile;
	public RadioButton popAudioFile;
	public RadioButton dingAudioFile;
	public RadioButton tickAudioFile;
	public Slider volumeSlider;
	
	private List<InputField> inputFields;
	
	public SettingsWindow() {
		instance = this;
	}
	
	public void initialize() {
		audioEngineGroup = new ToggleGroup();
		audioFileGroup = new ToggleGroup();
		javaxAudioEngine.setToggleGroup(audioEngineGroup);
		tinySoundAudioEngine.setToggleGroup(audioEngineGroup);
		beepAudioFile.setToggleGroup(audioFileGroup);
		popAudioFile.setToggleGroup(audioFileGroup);
		dingAudioFile.setToggleGroup(audioFileGroup);
		tickAudioFile.setToggleGroup(audioFileGroup);
		beepAudioFile.selectedProperty().addListener(new AudioFileListener(BeepSound.BEEP));
		popAudioFile.selectedProperty().addListener(new AudioFileListener(BeepSound.POP));
		dingAudioFile.selectedProperty().addListener(new AudioFileListener(BeepSound.DING));
		tickAudioFile.selectedProperty().addListener(new AudioFileListener(BeepSound.TICK));
		inputFields = Arrays.asList(startInputField1 = new InputField(startInputField1Internal), startInputField2 = new InputField(startInputField2Internal), resetInputField1 = new InputField(resetInputField1Internal), resetInputField2 = new InputField(resetInputField2Internal), upInputField1 = new InputField(upInputField1Internal), upInputField2 = new InputField(upInputField2Internal), downInputField1 = new InputField(downInputField1Internal), downInputField2 = new InputField(downInputField2Internal));
		for(InputField inputField : inputFields) {
			inputField.getParentField().addEventFilter(KeyEvent.KEY_PRESSED, e -> inputField.getParentField().setText(e.getCode().getName()));
			inputField.getParentField().addEventFilter(KeyEvent.KEY_TYPED, e -> e.consume());
			inputField.getParentField().focusedProperty().addListener((arg0, arg1, newValue) -> focuedField = newValue ? inputField : null);
		}
		volumeSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(oldValue && !newValue) {
					FlowTimer.audioEngine.setVolume((float)volumeSlider.getValue() / 100.0f);
					FlowTimer.currentBeep.play();
				}
			}
		});
	}
	
	public void setUpListeners() {
		javaxAudioEngine.selectedProperty().addListener(new AudioEngineListener());
		tinySoundAudioEngine.selectedProperty().addListener(new AudioEngineListener());
	}
	
	@FXML
	public void onStartClearPressed() {
		if(startInputField2.isBound()) {
			startInputField2.clear();
		} else {
			startInputField1.clear();
		}
	}
	
	@FXML
	public void onResetClearPressed() {
		if(resetInputField2.isBound()) {
			resetInputField2.clear();
		} else {
			resetInputField1.clear();
		}
	}
	
	@FXML
	public void onUpClearPressed() {
		if(upInputField2.isBound()) {
			upInputField2.clear();
		} else {
			upInputField1.clear();
		}
	}
	
	@FXML
	public void onDownClearPressed() {
		if(downInputField2.isBound()) {
			downInputField2.clear();
		} else {
			downInputField1.clear();
		}
	}
	
	private class AudioEngineListener implements ChangeListener<Boolean> {
		
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if(javaxAudioEngine.isSelected() || tinySoundAudioEngine.isSelected()) {
				if(newValue) {
					AlertBox.showAlert("FlowTimer", "Please restart FlowTimer for this change to take effect.");
				}
			}
		}
	}
	
	private class AudioFileListener implements ChangeListener<Boolean> {
		
		private BeepSound sound;
		
		public AudioFileListener(BeepSound sound) {
			this.sound = sound;
		}
		
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if(newValue) {
				FlowTimer.currentBeep = sound;
				FlowTimer.currentBeep.play();
			}
		}
	}
}
