package flowtimer.timers;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.jnativehook.keyboard.NativeKeyEvent;

import flowtimer.FlowTimer;
import flowtimer.ITimerLabelUpdateCallback;
import flowtimer.IntTextField;
import flowtimer.MenuButton;
import flowtimer.parsing.json.JSON;
import flowtimer.parsing.json.JSONArray;
import flowtimer.parsing.json.JSONObject;
import flowtimer.parsing.json.JSONValue;

public class DelayTimer extends BaseTimer {

	private static final long serialVersionUID = -913980123921622303L;

	private static final int ADD_BUTTON_BASE_X = 146;
	private static final int ADD_BUTTON_BASE_Y = 35;
	private static final int ADD_BUTTON_WIDTH = 55;
	private static final int ADD_BUTTON_HEIGHT = 22;
	private static final int ADD_BUTTON_PADDING = 3;

	private String fileSystemLocationBuffer;
	private String timerLocationBuffer;

	private LinkedList<TimerEntry> timers;
	private TimerEntry selectedTimer;
	private TimerEntry runningTimer;

	private ButtonGroup timerButtonGroup;
	private JButton addButton;
	private MenuButton loadTimersButton;
	private MenuButton saveTimersButton;
	private MenuButton saveTimersAsButton;
	private ColumnLabel nameLabel;
	private ColumnLabel offsetLabel;
	private ColumnLabel intervalLabel;
	private ColumnLabel numBeepsLabel;

	private String savedTimers;

	public DelayTimer(FlowTimer flowtimer) {
		super(flowtimer);

		timerButtonGroup = new ButtonGroup();

		addButton = new JButton("Add");
		addButton.setSize(ADD_BUTTON_WIDTH, ADD_BUTTON_HEIGHT);

		loadTimersButton = new MenuButton("Load Timers", 3);
		saveTimersButton = new MenuButton("Save Timers", 4);
		saveTimersAsButton = new MenuButton("Save Timers As", 5);

		nameLabel = new ColumnLabel("Name", 0);
		offsetLabel = new ColumnLabel("Offset", 1);
		intervalLabel = new ColumnLabel("Interval", 2);
		numBeepsLabel = new ColumnLabel("Beeps", 3);

		add(addButton);
		add(loadTimersButton);
		add(saveTimersButton);
		add(saveTimersAsButton);
		add(nameLabel);
		add(offsetLabel);
		add(intervalLabel);
		add(numBeepsLabel);

		timers = new LinkedList<>();

		addButton.addActionListener(e -> addDefaultTimer(true));
		loadTimersButton.addActionListener(e -> onLoadTimersPress());
		saveTimersButton.addActionListener(e -> onSaveTimersPress());
		saveTimersAsButton.addActionListener(e -> onSaveTimersAsPress());
	}

	public void onLoad() {
		flowtimer.setTimerLabel(0);
		selectedTimer.select();
		resizeWindow();
	}

	private void addDefaultTimer(boolean addRemoveButton) {
		addTimer("Timer", "5000", 500, 5, addRemoveButton);
	}

	public void loadTimers() {
		if(!new File(timerLocationBuffer).exists()) {
			if(!timerLocationBuffer.equals("null")) {
				JOptionPane.showMessageDialog(flowtimer.getFrame(), "Unable to locate the last used timer file. (It either got deleted or moved to a new directory)");
				timerLocationBuffer = "null";
			}
			addDefaultTimer(false);
		} else {
			loadTimers(timerLocationBuffer, false);
		}
	}

	public void addTimer(String name, String offset, long interval, int numBeeps, boolean addRemoveButton) {
		addNewTimer(new TimerEntry(timers.size(), name, offset, interval, numBeeps, addRemoveButton));
	}

	public void addNewTimer(TimerEntry timer) {
		timers.add(timer);
		timer.addAllElements(this);
		timer.select();
		recalcAddButton();
		repaint();
	}

	public void removeTimer(TimerEntry timer) {
		timers.remove(timer);
		timer.removeAllElements(this);
		for(TimerEntry t : timers) {
			t.recalcPosition(timers.indexOf(t));
		}
		if(timers.size() > 0) {
			timers.get(0).select();
		}
		recalcAddButton();
		repaint();
	}

	public void changeTimer(int amount) {
		int currentIndex = timers.indexOf(selectedTimer);
		currentIndex += amount;
		currentIndex = Math.floorMod(currentIndex, timers.size());
		timers.get(currentIndex).select();
	}

	public TimerEntry getTimer(int index) {
		return timers.get(index);
	}

	private void recalcAddButton() {
		int addButtonX = ADD_BUTTON_BASE_X;
		int addButtonY = ADD_BUTTON_BASE_Y + (addButton.getHeight() + ADD_BUTTON_PADDING) * timers.size();
		addButton.setBounds(addButtonX, addButtonY, addButton.getWidth(), addButton.getHeight());
		resizeWindow();
	}

	private void resizeWindow() {
		flowtimer.setSize(flowtimer.getFrame().getWidth(), Math.max(FlowTimer.HEIGHT, 25 * (timers.size() + 5) + 12));
	}

	public void onTimerStart(long startTime) {
		runningTimer = selectedTimer;
		flowtimer.scheduleActions(runningTimer.getOffsets(), runningTimer.getInterval(), runningTimer.getNumBeeps(), 0);
		setInterface(false);
	}

	public void onTimerStop() {
		flowtimer.setTimerLabel(selectedTimer.getMaxOffset());
		setInterface(true);
	}

	public void onTimerLabelUpdate(long time) {
	}

	public void onKeyEvent(NativeKeyEvent e) {
		if(flowtimer.getSettings().getUpInput().isPressed(e.getKeyCode())) {
			changeTimer(-1);
		}
		if(flowtimer.getSettings().getDownInput().isPressed(e.getKeyCode())) {
			changeTimer(+1);
		}
	}

	public ITimerLabelUpdateCallback getTimerLabelUpdateCallback() {
		return (startTime) -> runningTimer.getMaxOffset() - (System.nanoTime() - startTime) / 1_000_000;
	}

	public boolean canStartTimer() {
		return selectedTimer != null;
	}

	private void setInterface(boolean enabled) {
		timers.forEach(timer -> timer.setElements(enabled));
		saveTimersButton.setEnabled(enabled);
		loadTimersButton.setEnabled(enabled);
		saveTimersAsButton.setEnabled(enabled);
		addButton.setEnabled(enabled);
	}

	public void onSaveTimersPress() {
		if(timerLocationBuffer.equals("null")) {
			onSaveTimersAsPress();
		} else {
			saveTimers(timerLocationBuffer);
		}
	}

	public void onSaveTimersAsPress() {
		JFileChooser fileChooser = new JFileChooser(fileSystemLocationBuffer);
		fileChooser.setDialogTitle("Save Timers");
		int result = fileChooser.showSaveDialog(flowtimer.getFrame());
		fileSystemLocationBuffer = fileChooser.getCurrentDirectory().getAbsolutePath();
		if(result == JFileChooser.APPROVE_OPTION) {
			String filePath = fileChooser.getSelectedFile().getAbsolutePath();
			if(!filePath.toLowerCase().endsWith(".json")) {
				filePath += ".json";
			}
			saveTimers(filePath);
		}
	}

	private void saveTimers(String filePath) {
		JSONArray array = new JSONArray();
		for(TimerEntry timer : timers) {
			JSONObject object = new JSONObject();
			object.put("name", timer.getName());
			object.put("offsets", timer.getOffsetsString());
			object.put("interval", timer.getInterval());
			object.put("numBeeps", timer.getNumBeeps());
			object.put("removeButton", timer.hasRemoveButton());
			array.add(object);
		}
		JSON json = new JSON(array);
		json.write(filePath);
		timerLocationBuffer = filePath;
		savedTimers = timers.toString();
		JOptionPane.showMessageDialog(flowtimer.getFrame(), "Timers successfully saved to " + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);
	}

	public void onLoadTimersPress() {
		JFileChooser fileChooser = new JFileChooser(fileSystemLocationBuffer);
		fileChooser.setDialogTitle("Load Timers");
		fileChooser.setFileFilter(new FileNameExtensionFilter(".json files", "json"));
		int result = fileChooser.showOpenDialog(flowtimer.getFrame());
		fileSystemLocationBuffer = fileChooser.getCurrentDirectory().getAbsolutePath();
		if(result == JFileChooser.APPROVE_OPTION) {
			String filePath = fileChooser.getSelectedFile().getAbsolutePath();
			loadTimers(filePath, true);
		}
	}
	
	private void loadTimers(String filePath, boolean showSuccessMessage) {
		LinkedList<TimerEntry> loadedList = new LinkedList<>();
		try {
			List<JSONValue> jsonTimers = new JSON(new File(filePath)).get().asArray();
			int index = 0;
			for(JSONValue jsonTimer : jsonTimers) {
				Map<String, JSONValue> timer = jsonTimer.asObject();
				loadedList.add(new TimerEntry(index++, timer.get("name").asString(), timer.get("offsets").asString(), timer.get("interval").asLong(), timer.get("numBeeps").asInt(), timer.get("removeButton").asBoolean()));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(flowtimer.getFrame(), "Timers failed to load.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		savedTimers = loadedList.toString();
		LinkedList<TimerEntry> currentTimers = new LinkedList<>(timers);
		currentTimers.forEach(timer -> removeTimer(timer));
		loadedList.forEach(timer -> addNewTimer(timer));
		timers.getFirst().select();
		timerLocationBuffer = filePath;
		if(showSuccessMessage) {
			JOptionPane.showMessageDialog(flowtimer.getFrame(), "Timers successfully loaded.", "Success", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public boolean haveTimersChanged() {
		return savedTimers == null ? true : !savedTimers.equalsIgnoreCase(timers.toString());
	}

	public String getTimerLocationBuffer() {
		return timerLocationBuffer;
	}

	public void setTimerLocationBuffer(String timerLocationBuffer) {
		this.timerLocationBuffer = timerLocationBuffer;
	}

	public String getFileSystemLocationBuffer() {
		return fileSystemLocationBuffer;
	}

	public void setFileSystemLocationBuffer(String fileSystemLocationBuffer) {
		this.fileSystemLocationBuffer = fileSystemLocationBuffer;
	}

	public class ColumnLabel extends JLabel {

		private static final long serialVersionUID = 6275772904824072659L;

		public static final int WIDTH = 55;
		public static final int HEIGHT = 20;
		public static final int X_BASE = 146;
		public static final int X_PADDING = 5;
		public static final int Y_BASE = 16;

		public ColumnLabel(String text, int index) {
			super(text);
			setBounds(X_BASE + index * (X_PADDING + WIDTH), Y_BASE, WIDTH, HEIGHT);
		}
	}

	public class TimerEntry {

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

		private JRadioButton radioButton;
		private JTextField nameField;
		private JTextField offsetField;
		private IntTextField intervalField;
		private IntTextField numBeepsField;
		private JButton removeButton;
		private ArrayList<Component> elements;
		private long[] offsets;
		private long maxOffset;

		public TimerEntry(int index, String name, String offsets, long interval, int numBeeps, boolean addRemoveButton) {
			radioButton = new JRadioButton();
			nameField = new JTextField(String.valueOf(name));
			offsetField = new JTextField();
			offsetField.setDocument(new OffsetFieldDocument(offsetField));
			offsetField.setText(offsets);
			intervalField = new IntTextField(false);
			intervalField.setValue(interval);
			numBeepsField = new IntTextField(false);
			numBeepsField.setValue(numBeeps);
			elements = new ArrayList<>();
			elements.addAll(Arrays.asList(nameField, offsetField, intervalField, numBeepsField));
			if(addRemoveButton) {
				removeButton = new JButton("-");
				removeButton.addActionListener(e -> {
					removeTimer(this);
					getTimer(0).select();
					timerButtonGroup.remove(removeButton);
				});
				elements.add(removeButton);
			}
			recalcPosition(index);
			timerButtonGroup.add(radioButton);
			radioButton.setFocusable(false);
			radioButton.addActionListener(e -> select());
			radioButton.addItemListener(e -> {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					select();
				}
			});
			nameField.getDocument().addDocumentListener(new TimerEntryDocumentListener(this));
			offsetField.getDocument().addDocumentListener(new TimerEntryDocumentListener(this));
			intervalField.getDocument().addDocumentListener(new TimerEntryDocumentListener(this));
			numBeepsField.getDocument().addDocumentListener(new TimerEntryDocumentListener(this));
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

		public void addAllElements(JPanel parent) {
			parent.add(radioButton);
			elements.forEach(element -> parent.add(element));
		}

		public void removeAllElements(JPanel parent) {
			parent.remove(radioButton);
			elements.forEach(element -> parent.remove(element));
		}

		public void select() {
			radioButton.setSelected(true);
			if(isAllDataValid()) {
				offsets = Arrays.stream(offsetField.getText().split("/")).mapToLong(Long::parseLong).toArray();
				Arrays.sort(offsets);
				maxOffset = offsets[offsets.length - 1];
				selectedTimer = this;
				flowtimer.setTimerLabel(getMaxOffset());
			} else {
				selectedTimer = null;
				flowtimer.setTimerLabel("Error");
			}
		}

		public void setElements(boolean enabled) {
			elements.forEach(element -> element.setEnabled(enabled));
		}

		public String getName() {
			return nameField.getText();
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

		public int getInterval() {
			return Integer.valueOf(intervalField.getText());
		}

		public int getNumBeeps() {
			return Integer.valueOf(numBeepsField.getText());
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
			long minDelay = Integer.valueOf(intervalField.getText()).intValue() * Integer.valueOf(numBeepsField.getText()).intValue();
			String offsets[] = offsetField.getText().split("/");
			for(int offsetIndex = 0; offsetIndex < offsets.length; offsetIndex++) {
				long offset = Long.valueOf(offsets[offsetIndex]);
				if(offset < minDelay) {
					return false;
				}
			}
			return true;
		}

		public String toString() {
			return "TimerEntry [getName()=" + getName() + ", getOffsetsString()=" + getOffsetsString() + ", getInterval()=" + getInterval() + ", getNumBeeps()=" + getNumBeeps() + ", hasRemoveButton()=" + hasRemoveButton() + "]";
		}
	}
	
	private class OffsetFieldDocument extends PlainDocument {

		private static final long serialVersionUID = 5344594296265409409L;
		
		private JTextField textField;
		
		public OffsetFieldDocument(JTextField textField) {
			this.textField = textField;
		}

		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			if(!new StringBuffer(textField.getText()).insert(offs, str).toString().matches("^(\\d|\\/)*$")) {
				return;
			}
			super.insertString(offs, str, a);
		}
	}

	private class TimerEntryDocumentListener implements DocumentListener {

		private TimerEntry parent;

		public TimerEntryDocumentListener(TimerEntry parent) {
			this.parent = parent;
		}

		public void insertUpdate(DocumentEvent e) {
			onChange();
		}

		public void removeUpdate(DocumentEvent e) {
			onChange();
		}

		public void changedUpdate(DocumentEvent e) {
			onChange();
		}

		private void onChange() {
			parent.select();
		}
	}
}
