package flowtimer;

import javax.swing.JButton;
import javax.swing.JDialog;

public class NamedInput {

	public static final int X_BASE = 45;
	public static final int X_STEP = 80;
	
	private JButton button;
	private int keyCode;
	
	public NamedInput(JDialog parent, int xIndex, int yPos) {
		this.button = new JButton("Unset");
		this.keyCode = -1;
		
		int xPos = X_BASE + X_STEP * xIndex;
		button.setBounds(xPos, yPos, X_STEP, 23);
		parent.add(button);
	}

	public void set(String name, int keyCode) {
		setName(name);
		setKeyCode(keyCode);
	}
	
	public void clear() {
		set("Unset", -1);
	}
	
	public boolean isCleared() {
		return keyCode == -1;
	}
	
	public String getName() {
		return button.getText();
	}

	public void setName(String name) {
		button.setText(name);
	}

	public int getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

	public JButton getButton() {
		return button;
	}
}