package flowtimer.settings;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class KeyInput {
	
	public static final int LABEL_X = 10;
	public static final int FIELD_X = LABEL_X + 35;
	public static final int BUTTON_X = FIELD_X + 160;
	public static final int Y_BASE = 5;
	public static final int Y_STEP = 25;
	
	public static JDialog waitDialog;
	public static NamedInput selectedInput;
	
	private SettingsWindow parent;
	private JLabel label;
	private NamedInput primaryInput;
	private NamedInput secondaryInput;
	private JButton clearButton;
	private JCheckBox globalCheckbox;

	public KeyInput(SettingsWindow parent, int index, String actionName) {
		this.parent = parent;
		
		int yPos = Y_BASE + Y_STEP * index;
		
		label = new JLabel(actionName + ":");
		clearButton = new JButton("Clear");
		primaryInput = new NamedInput(parent, 0, yPos);
		secondaryInput = new NamedInput(parent, 1, yPos);
		primaryInput.getButton().addActionListener(new ButtonActionListener(parent, primaryInput));
		secondaryInput.getButton().addActionListener(new ButtonActionListener(parent, secondaryInput));
		
		label.setBounds(LABEL_X, yPos, 50, 20);
		clearButton.setBounds(BUTTON_X, yPos, 60, 23);
		
		clearButton.addActionListener(e -> {
			if(!secondaryInput.isCleared()) {
				secondaryInput.clear();
			} else {
				primaryInput.clear();
			}
		});
		
		parent.add(label);
		parent.add(clearButton);
	}
	
	public NamedInput getPrimaryInput() {
		return primaryInput;
	}

	public NamedInput getSecondaryInput() {
		return secondaryInput;
	}
	
	public boolean isPressed(int keyCode) {
		return (parent.getFlowtimer().getFrame().isFocused() || isGlobal()) && (primaryInput.getKeyCode() == keyCode || secondaryInput.getKeyCode() == keyCode);
	}
	
	public boolean isGlobal() {
		if(globalCheckbox == null) {
			return false;
		}
		return globalCheckbox.isSelected();
	}

	public JCheckBox getGlobalCheckbox() {
		return globalCheckbox;
	}

	public KeyInput setGlobalCheckbox(JCheckBox global) {
		this.globalCheckbox = global;
		return this;
	}

	private static class ButtonActionListener implements ActionListener {
		
		private JDialog parent;
		private NamedInput input;
		
		public ButtonActionListener(JDialog parent, NamedInput input) {
			this.parent = parent;
			this.input = input;
		}

		public void actionPerformed(ActionEvent e) {
			selectedInput = input;
			waitDialog = new JDialog(parent, "", ModalityType.APPLICATION_MODAL);
			waitDialog.setSize(100, 75);
			waitDialog.setLocationRelativeTo(null);
		
			JLabel label = new JLabel("Press any button...");
			label.setHorizontalAlignment(SwingConstants.CENTER);
			waitDialog.add(label);
			
			waitDialog.setVisible(true);
		}
	}
}