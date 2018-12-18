package flowtimer;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class Timer {

	public static final int BASE_X = 146;
	public static final int BASE_Y = 36;
	public static final int LABEL_WIDTH = 55;
	public static final int LABEL_HEIGHT = 20;
	public static final int PADDING_X = 5;
	public static final int PADDING_Y = 5;
	
	public static final int RADIO_BUTTON_BASE_X = 126;
	public static final int RADIO_BUTTON_BASE_Y = 35;
	public static final int RADIO_BUTTON_WIDTH = 20;
	public static final int RADIO_BUTTON_HEIGHT = 20;
	
	public static final int REMOVE_BUTTON_X_OFFSET = 59;
	public static final int REMOVE_BUTTON_WIDTH = 40;
	
	public static ButtonGroup group = new ButtonGroup();
	
	private JRadioButton radioButton;
	private JTextField nameField;
	private JTextField offsetField;
	private JTextField intervalField;
	private JTextField numBeepsField;
	private JButton removeButton;
	private ArrayList<Component> elements;
	private long[] offsets;
	private long maxOffset;
	private long[][] beeps;
	
	public Timer(int index, String name, String offsets, long interval, int numBeeps, boolean addRemoveButton) {
		radioButton = new JRadioButton();
		nameField = new JTextField(String.valueOf(name));
		offsetField = new JTextField(offsets);
		intervalField = new JTextField(String.valueOf(interval));
		numBeepsField = new JTextField(String.valueOf(numBeeps));
		elements = new ArrayList<>();
		elements.addAll(Arrays.asList(nameField, offsetField, intervalField, numBeepsField));
		if(addRemoveButton) {
			removeButton = new JButton("-");
			removeButton.addActionListener(e -> {
				FlowTimer.removeTimer(this);
				FlowTimer.getTimer(0).select();
				group.remove(removeButton);
			});
			elements.add(removeButton);
		}
		recalcPosition(index);
		group.add(radioButton);
		radioButton.setFocusable(false);
		radioButton.addActionListener(e -> select());
		radioButton.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				select();
			}
		});
		nameField.getDocument().addDocumentListener(new TimerDocumentListener(this));
		offsetField.getDocument().addDocumentListener(new TimerDocumentListener(this));
		intervalField.getDocument().addDocumentListener(new TimerDocumentListener(this));
		numBeepsField.getDocument().addDocumentListener(new TimerDocumentListener(this));
	}
	
	public void recalcPosition(int index) {
		int xIndex = 0;
		setFieldPosition(nameField, xIndex++, index);
		setFieldPosition(offsetField, xIndex++, index);
		setFieldPosition(intervalField, xIndex++, index);
		setFieldPosition(numBeepsField, xIndex++, index);
		
		int radioButtonY = RADIO_BUTTON_BASE_Y + (RADIO_BUTTON_HEIGHT + PADDING_Y) * index;
		radioButton.setBounds(RADIO_BUTTON_BASE_X, radioButtonY, RADIO_BUTTON_WIDTH, RADIO_BUTTON_HEIGHT);
		if(removeButton != null) {
			removeButton.setBounds(numBeepsField.getX() + REMOVE_BUTTON_X_OFFSET, nameField.getY(), REMOVE_BUTTON_WIDTH, LABEL_HEIGHT);
		}
	}
	
	private void setFieldPosition(JTextField field, int xIndex, int yIndex) {
		int xPos = BASE_X + (LABEL_WIDTH + PADDING_X) * xIndex;
		int yPos = BASE_Y + (LABEL_HEIGHT + PADDING_Y) * yIndex;
		field.setBounds(xPos, yPos, LABEL_WIDTH, LABEL_HEIGHT);
	}
	
	public void addAllElements(JFrame parent) {
		parent.add(radioButton);
		elements.forEach(element -> parent.add(element));
	}
	
	public void removeAllElements(JFrame parent) {
		parent.remove(radioButton);
		elements.forEach(element -> parent.remove(element));
	}
	
	public void select() {
		radioButton.setSelected(true);
		if(isAllDataValid()) {
			offsets = Arrays.stream(offsetField.getText().split("/")).mapToLong(Long::parseLong).toArray();
			Arrays.sort(offsets);
			maxOffset = offsets[offsets.length - 1];
			beeps = calcBeeps();
			FlowTimer.setSelectedTimer(this);
			FlowTimer.setTimerLabel(getMaxOffset());
		} else {
			FlowTimer.setSelectedTimer(null);
			FlowTimer.setTimerLabel("Error");
		}
	}
	
	public void setElements(boolean enabled) {
		elements.forEach(element -> element.setEnabled(enabled));
	}
	
	public String getName() {
		return nameField.getText();
	}
	
	private long[][] calcBeeps() {
		long[] offsets = getOffsets();
		long interval = getInterval();
		int numBeeps = getNumBeeps();
		long[][] result = new long[offsets.length][numBeeps];
		for(int i = 0; i < offsets.length; i++) {
			long offset = offsets[i];
			for(int j = 0; j < numBeeps; j++) {
				result[i][j] = (offset - (interval * numBeeps) + (interval * (j + 1)));
			}
		}
		return result;
	}
	
	public long[] getOffsets() {
		return offsets;
	}
	
	public long getMaxOffset() {
		return maxOffset;
	}
	
	public String getOffsetsString() {
		return offsetField.getText();
	}
	
	public long getInterval() {
		return Long.valueOf(intervalField.getText());
	}
	
	public int getNumBeeps() {
		return Integer.valueOf(numBeepsField.getText());
	}
	
	public long[][] getBeeps() {
		return beeps;
	}
	
	public boolean isSelected() {
		return radioButton.isSelected();
	}

	public boolean hasRemoveButton() {
		return removeButton != null;
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