package stringflow.cheatontimer;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.util.Arrays;
import java.util.List;

public class SettingsWindow {
	
	public static SettingsWindow instance = null;
	
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
	
	private List<InputField> inputFields;
	
	public SettingsWindow() {
		instance = this;
	}
	
	public void initialize() {
		inputFields = Arrays.asList(startInputField1 = new InputField(startInputField1Internal), startInputField2 = new InputField(startInputField2Internal), resetInputField1 = new InputField(resetInputField1Internal), resetInputField2 = new InputField(resetInputField2Internal), upInputField1 = new InputField(upInputField1Internal), upInputField2 = new InputField(upInputField2Internal), downInputField1 = new InputField(downInputField1Internal), downInputField2 = new InputField(downInputField2Internal));
		for(InputField inputField : inputFields) {
			inputField.getParentField().addEventFilter(KeyEvent.KEY_PRESSED, e -> inputField.getParentField().setText(e.getCode().getName()));
			inputField.getParentField().addEventFilter(KeyEvent.KEY_TYPED, e -> e.consume());
			inputField.getParentField().focusedProperty().addListener((arg0, arg1, newValue) -> focuedField = newValue ? inputField : null);
		}
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
}
