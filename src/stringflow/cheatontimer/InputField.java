package stringflow.cheatontimer;

import javafx.scene.control.TextField;

public class InputField {

	private int keyCode;
	private TextField parentField;
	
	public InputField(TextField parentField) {
		this.parentField = parentField;
		this.keyCode = -1;
	}
	
	public TextField getParentField() {
		return parentField;
	}
	
	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}
	
	public int getKeyCode() {
		return keyCode;
	}
	
	public boolean isBound() {
		return keyCode > -1;
	}
	
	public void clear() {
		parentField.setText("");
		keyCode = -1;
	}
}