package flowtimer;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jnativehook.GlobalScreen;

import flowtimer.parsing.Config;
import flowtimer.parsing.json.JSON;
import flowtimer.parsing.json.JSONArray;
import flowtimer.parsing.json.JSONObject;
import flowtimer.parsing.json.JSONValue;

public class FlowTimer {

	public static final int WIDTH = 451;
	public static final int HEIGHT = 262;
	public static final String TITLE = "FlowTimer 1.5";

	public static final int ADD_BUTTON_BASE_X = 146;
	public static final int ADD_BUTTON_BASE_Y = 35;
	public static final int ADD_BUTTON_WIDTH = 55;
	public static final int ADD_BUTTON_HEIGHT = 22;
	public static final int ADD_BUTTON_PADDING = 3;

	public static final String SETTINGS_FILE_LOCATION = System.getenv("appdata") + "\\flowtimer.config";
	public static final File SETTINGS_FILE = new File(SETTINGS_FILE_LOCATION);
	public static final HashMap<String, Integer> BEEP_MAP = new HashMap<>();

	private static LinkedList<Timer> timers = new LinkedList<>();
	private static Timer activeTimer;
	private static Timer selectedTimer;
	private static ScheduledExecutorService scheduler;
	private static ScheduledFuture<?>[] beepFutures;
	private static TimerLabelUpdateThread timerLabelUpdateThread;
	private static VisualCueThread visualCueThread;
	private static boolean isTimerRunning;
	private static long timerStartTime;
	private static int beepSound;
	private static int visualCueLength;

	private static JFrame frame;
	private static JLabel timerLabel;
	private static VisualCuePanel visualCuePanel;
	private static JButton addButton;
	private static MenuButton startButton;
	private static MenuButton resetButton;
	private static MenuButton settingsButton;
	private static MenuButton loadTimersButton;
	private static MenuButton saveTimersButton;
	private static MenuButton saveTimersAsButton;
	private static ColumnLabel nameLabel;
	private static ColumnLabel offsetLabel;
	private static ColumnLabel intervalLabel;
	private static ColumnLabel numBeepsLabel;
	
	private static JLabel pinLabel;

	private static String fileSystemLocationBuffer;
	private static String timerLocationBuffer;
	
	static {
		AudioEngine.init();
		BEEP_MAP.put("ping1", AudioEngine.createSource("/audio/ping1.wav"));
		BEEP_MAP.put("ping2", AudioEngine.createSource("/audio/ping2.wav"));
		BEEP_MAP.put("clack", AudioEngine.createSource("/audio/clack.wav"));
		BEEP_MAP.put("click1", AudioEngine.createSource("/audio/click1.wav"));
		BEEP_MAP.put("clap", AudioEngine.createSource("/audio/clap.wav"));
		BEEP_MAP.put("beep", AudioEngine.createSource("/audio/beep.wav"));
	}

	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		frame = new JFrame();
		pinLabel = new JLabel();
		SettingsWindow.create(frame);

		try {
			Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
			logger.setLevel(Level.OFF);
			logger.setUseParentHandlers(false);

			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(new GlobalScreenListener());

			HashMap<String, String> defaultMap = new HashMap<>();
			defaultMap.put("fileSystemLocationBuffer", System.getProperty("user.home") + "\\Desktop");
			defaultMap.put("timerLocationBuffer", "null");
			
			defaultMap.put("primaryStartKey", "-1");
			defaultMap.put("primaryStartKeyName", "Unset");
			defaultMap.put("primaryResetKey", "-1");
			defaultMap.put("primaryResetKeyName", "Unset");
			defaultMap.put("primaryUpKey", "57416");
			defaultMap.put("primaryUpKeyName", "Up");
			defaultMap.put("primaryDownKey", "57424");
			defaultMap.put("primaryDownKeyName", "Down");
			
			defaultMap.put("secondaryStartKey", "-1");
			defaultMap.put("secondaryStartKeyName", "Unset");
			defaultMap.put("secondaryResetKey", "-1");
			defaultMap.put("secondaryResetKeyName", "Unset");
			defaultMap.put("secondaryUpKey", "-1");
			defaultMap.put("secondaryUpKeyName", "Unset");
			defaultMap.put("secondaryDownKey", "-1");
			defaultMap.put("secondaryDownKeyName", "Unset");
			
			defaultMap.put("globalStartReset", "true");
			defaultMap.put("globalUpDown", "true");
			defaultMap.put("visualCue", "false");
			defaultMap.put("beepSound", "ping1");
			
			defaultMap.put("visualCueLength", "20");
			defaultMap.put("pin", "false");

			Config config;
			if(!SETTINGS_FILE.exists()) {
				config = new Config(defaultMap);
				config.write(SETTINGS_FILE_LOCATION);
			} else {
				config = new Config(SETTINGS_FILE_LOCATION);
			}
			fileSystemLocationBuffer = config.getStringWithDefault("fileSystemLocationBuffer", defaultMap.get("fileSystemLocationBuffer"));
			timerLocationBuffer = config.getStringWithDefault("timerLocationBuffer", defaultMap.get("timerLocationBuffer"));
			
			visualCueLength = config.getIntWithDefault("visualCueLength", defaultMap.get("visualCueLength"));
			
			SettingsWindow.getStartInput().getPrimaryInput().set(config.getStringWithDefault("primaryStartKeyName", defaultMap.get("primaryStartKeyName")), config.getIntWithDefault("primaryStartKey", defaultMap.get("primaryStartKey")));
			SettingsWindow.getResetInput().getPrimaryInput().set(config.getStringWithDefault("primaryResetKeyName", defaultMap.get("primaryResetKeyName")), config.getIntWithDefault("primaryResetKey", defaultMap.get("primaryResetKey")));
			SettingsWindow.getUpInput().getPrimaryInput().set(config.getStringWithDefault("primaryUpKeyName", defaultMap.get("primaryUpKeyName")), config.getIntWithDefault("primaryUpKey", defaultMap.get("primaryUpKey")));
			SettingsWindow.getDownInput().getPrimaryInput().set(config.getStringWithDefault("primaryDownKeyName", defaultMap.get("primaryDownKeyName")), config.getIntWithDefault("primaryDownKey", defaultMap.get("primaryDownKey")));

			SettingsWindow.getStartInput().getSecondaryInput().set(config.getStringWithDefault("secondaryStartKeyName", defaultMap.get("secondaryStartKeyName")), config.getIntWithDefault("secondaryStartKey", defaultMap.get("secondaryStartKey")));
			SettingsWindow.getResetInput().getSecondaryInput().set(config.getStringWithDefault("secondaryResetKeyName", defaultMap.get("secondaryResetKeyName")), config.getIntWithDefault("secondaryResetKey", defaultMap.get("secondaryResetKey")));
			SettingsWindow.getUpInput().getSecondaryInput().set(config.getStringWithDefault("secondaryUpKeyName", defaultMap.get("secondaryUpKeyName")), config.getIntWithDefault("secondaryUpKey", defaultMap.get("secondaryUpKey")));
			SettingsWindow.getDownInput().getSecondaryInput().set(config.getStringWithDefault("secondaryDownKeyName", defaultMap.get("secondaryDownKeyName")), config.getIntWithDefault("secondaryDownKey", defaultMap.get("secondaryDownKey")));

			SettingsWindow.getGlobalStartReset().setSelected(config.getBooleanWithDefault("globalStartReset", defaultMap.get("globalStartReset")));
			SettingsWindow.getGlobalUpDown().setSelected(config.getBooleanWithDefault("globalUpDown", defaultMap.get("globalUpDown")));
			SettingsWindow.getVisualCue().setSelected(config.getBooleanWithDefault("visualCue", defaultMap.get("visualCue")));
			SettingsWindow.getBeepSound().setSelectedItem(config.getStringWithDefault("beepSound", defaultMap.get("beepSound")));
		
			setPin(config.getBooleanWithDefault("pin", defaultMap.get("pin")));
		} catch (Exception e) {
			ErrorHandler.handleException(e, true);
		}

		frame.setSize(WIDTH, HEIGHT);
		frame.setTitle(TITLE);
		frame.setLayout(null);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);

		timerLabel = new JLabel();
		timerLabel.setBounds(11, 20, 120, 30);
		timerLabel.setFont(new Font("Consolas", Font.BOLD, 29));
		
		pinLabel.setBounds(413, 5, 16, 16);
		pinLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				setPin(!frame.isAlwaysOnTop());
			}
		});

		visualCuePanel = new VisualCuePanel(frame);

		addButton = new JButton("Add");
		addButton.setSize(ADD_BUTTON_WIDTH, ADD_BUTTON_HEIGHT);

		startButton = new MenuButton("Start", 0);
		resetButton = new MenuButton("Reset", 1);
		settingsButton = new MenuButton("Settings", 2);
		loadTimersButton = new MenuButton("Load Timers", 3);
		saveTimersButton = new MenuButton("Save Timers", 4);
		saveTimersAsButton = new MenuButton("Save Timers As", 5);

		nameLabel = new ColumnLabel("Name", 0);
		offsetLabel = new ColumnLabel("Offset", 1);
		intervalLabel = new ColumnLabel("Interval", 2);
		numBeepsLabel = new ColumnLabel("Beeps", 3);

		addButton.addActionListener(e -> addDefaultTimer(true));

		scheduler = Executors.newScheduledThreadPool(2);
		beepFutures = new ScheduledFuture[0];
		timerLabelUpdateThread = new TimerLabelUpdateThread();
		visualCueThread = new VisualCueThread();

		startButton.addActionListener(e -> onStartButtonPress());
		resetButton.addActionListener(e -> onResetButtonPress());
		loadTimersButton.addActionListener(e -> onLoadTimersPress());
		saveTimersButton.addActionListener(e -> onSaveTimersPress());
		saveTimersAsButton.addActionListener(e -> onSaveTimersAsPress());
		settingsButton.addActionListener(e -> SettingsWindow.show());

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				AudioEngine.dispose();

				HashMap<String, String> map = new HashMap<>();
				map.put("fileSystemLocationBuffer", fileSystemLocationBuffer);
				map.put("timerLocationBuffer", timerLocationBuffer);
				
				map.put("primaryStartKey", SettingsWindow.getStartInput().getPrimaryInput().getKeyCode() + "");
				map.put("primaryResetKey", SettingsWindow.getResetInput().getPrimaryInput().getKeyCode() + "");
				map.put("primaryUpKey", SettingsWindow.getUpInput().getPrimaryInput().getKeyCode() + "");
				map.put("primaryDownKey", SettingsWindow.getDownInput().getPrimaryInput().getKeyCode() + "");
				map.put("primaryStartKeyName", SettingsWindow.getStartInput().getPrimaryInput().getName());
				map.put("primaryResetKeyName", SettingsWindow.getResetInput().getPrimaryInput().getName());
				map.put("primaryUpKeyName", SettingsWindow.getUpInput().getPrimaryInput().getName());
				map.put("primaryDownKeyName", SettingsWindow.getDownInput().getPrimaryInput().getName());
				
				map.put("secondaryStartKey", SettingsWindow.getStartInput().getSecondaryInput().getKeyCode() + "");
				map.put("secondaryResetKey", SettingsWindow.getResetInput().getSecondaryInput().getKeyCode() + "");
				map.put("secondaryUpKey", SettingsWindow.getUpInput().getSecondaryInput().getKeyCode() + "");
				map.put("secondaryDownKey", SettingsWindow.getDownInput().getSecondaryInput().getKeyCode() + "");
				map.put("secondaryStartKeyName", SettingsWindow.getStartInput().getSecondaryInput().getName());
				map.put("secondaryResetKeyName", SettingsWindow.getResetInput().getSecondaryInput().getName());
				map.put("secondaryUpKeyName", SettingsWindow.getUpInput().getSecondaryInput().getName());
				map.put("secondaryDownKeyName", SettingsWindow.getDownInput().getSecondaryInput().getName());
				
				map.put("globalStartReset", SettingsWindow.getGlobalStartReset().isSelected() + "");
				map.put("globalUpDown", SettingsWindow.getGlobalUpDown().isSelected() + "");
				map.put("visualCue", SettingsWindow.getVisualCue().isSelected() + "");
				map.put("beepSound", SettingsWindow.getBeepSound().getSelectedItem() + "");

				map.put("visualCueLength", visualCueLength + "");
				map.put("pin", frame.isAlwaysOnTop() + "");
				
				Config config = new Config(map);
				try {
					config.write(SETTINGS_FILE_LOCATION);
				} catch (Exception e1) {
					ErrorHandler.handleException(e1, false);
				}
			}
		});

		if(timerLocationBuffer.equals("null")) {
			addDefaultTimer(false);
		} else {
			loadTimers(timerLocationBuffer, false);
		}

		frame.add(timerLabel);
		frame.add(visualCuePanel);
		frame.add(startButton);
		frame.add(resetButton);
		frame.add(settingsButton);
		frame.add(loadTimersButton);
		frame.add(saveTimersButton);
		frame.add(saveTimersAsButton);
		frame.add(nameLabel);
		frame.add(offsetLabel);
		frame.add(intervalLabel);
		frame.add(numBeepsLabel);
		frame.add(addButton);
		frame.add(pinLabel);
		frame.repaint();
	}

	public static void onStartButtonPress() {
		startTimer();
	}

	public static void onResetButtonPress() {
		if(isTimerRunning) {
			stopTimer();
			setTimerLabel(selectedTimer.getMaxOffset());
		}
	}

	public static void onUpKeyPress() {
		changeTimer(-1);
	}

	public static void onDownKeyPress() {
		changeTimer(+1);
	}

	public static void changeTimer(int amount) {
		int currentIndex = timers.indexOf(selectedTimer);
		currentIndex += amount;
		currentIndex %= timers.size();
		if(currentIndex < 0) {
			currentIndex = timers.size() - 1;
		}
		timers.get(currentIndex).select();
	}

	public static void startTimer() {
		if(selectedTimer != null) {
			if(isTimerRunning) {
				stopTimer();
			}
			isTimerRunning = true;
			Timer t = selectedTimer;
			activeTimer = selectedTimer;
			long offsets[] = t.getOffsets();
			long beeps[][] = t.getBeeps();
			beepFutures = new ScheduledFuture[offsets.length];
			timerStartTime = System.nanoTime();
			for(int i = 0; i < offsets.length; i++) {
				BeepThread beepThread = new BeepThread(i, i == offsets.length - 1, t.getNumBeeps());
				beepFutures[i] = scheduler.scheduleAtFixedRate(beepThread, beeps[i][0] * 1_000_000, t.getInterval() * 1_000_000, TimeUnit.NANOSECONDS);
			}
			setInterface(false);
			new Thread(timerLabelUpdateThread).start();
		}
	}

	public static void stopTimer() {
		for(ScheduledFuture<?> future : beepFutures) {
			future.cancel(false);
		}
		isTimerRunning = false;
		timerLabelUpdateThread.stop();
		setInterface(true);
		setTimerLabel(0);
	}

	public static void stopSegment(int index) {
		beepFutures[index].cancel(false);
	}

	public static void addNewTimer(String name, String offset, long interval, int numBeeps, boolean addRemoveButton) {
		addNewTimer(new Timer(timers.size(), name, offset, interval, numBeeps, addRemoveButton));
	}

	public static void addNewTimer(Timer timer) {
		timers.add(timer);
		timer.addAllElements(frame);
		timer.select();
		recalcAddButton();
		frame.repaint();
	}

	public static void removeTimer(Timer timer) {
		timers.remove(timer);
		timer.removeAllElements(frame);
		for(Timer t : timers) {
			t.recalcPosition(timers.indexOf(t));
		}
		if(timers.size() > 0) {
			timers.get(0).select();
		}
		recalcAddButton();
		frame.repaint();
	}

	public static void onSaveTimersPress() {
		if(timerLocationBuffer.equals("null")) {
			onSaveTimersAsPress();
		} else {
			saveTimers(timerLocationBuffer);
		}
	}

	public static void onSaveTimersAsPress() {
		JFileChooser fileChooser = new JFileChooser(fileSystemLocationBuffer);
		fileChooser.setDialogTitle("Save Timers");
		int result = fileChooser.showSaveDialog(frame);
		fileSystemLocationBuffer = fileChooser.getCurrentDirectory().getAbsolutePath();
		if(result == JFileChooser.APPROVE_OPTION) {
			String filePath = fileChooser.getSelectedFile().getAbsolutePath();
			if(!filePath.toLowerCase().endsWith(".json")) {
				filePath += ".json";
			}
			saveTimers(filePath);
		}
	}

	public static void saveTimers(String filePath) {
		JSONArray array = new JSONArray();
		for(Timer timer : timers) {
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
		JOptionPane.showMessageDialog(null, "Timers successfully saved to " + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);
	}

	public static void onLoadTimersPress() {
		JFileChooser fileChooser = new JFileChooser(fileSystemLocationBuffer);
		fileChooser.setDialogTitle("Load Timers");
		fileChooser.setFileFilter(new FileNameExtensionFilter(".json files", "json"));
		int result = fileChooser.showOpenDialog(frame);
		fileSystemLocationBuffer = fileChooser.getCurrentDirectory().getAbsolutePath();
		if(result == JFileChooser.APPROVE_OPTION) {
			String filePath = fileChooser.getSelectedFile().getAbsolutePath();
			loadTimers(filePath, true);
		}
	}

	public static void loadTimers(String filePath, boolean showSuccessMessage) {
		LinkedList<Timer> loadedList = new LinkedList<>();
		try {
			List<JSONValue> jsonTimers = new JSON(new File(filePath)).get().asArray();
			int index = 0;
			for(JSONValue jsonTimer : jsonTimers) {
				Map<String, JSONValue> timer = jsonTimer.asObject();
				loadedList.add(new Timer(index++, timer.get("name").asString(), timer.get("offsets").asString(), timer.get("interval").asLong(), timer.get("numBeeps").asInt(), timer.get("removeButton").asBoolean()));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Timers failed to load.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		LinkedList<Timer> currentTimers = new LinkedList<>(timers);
		currentTimers.forEach(timer -> removeTimer(timer));
		loadedList.forEach(timer -> addNewTimer(timer));
		timers.getFirst().select();
		timerLocationBuffer = filePath;
		if(showSuccessMessage) {
			JOptionPane.showMessageDialog(null, "Timers successfully loaded.", "Success", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public static Timer getTimer(int index) {
		return timers.get(index);
	}

	public static Timer getLastTimer() {
		return timers.get(timers.size() - 1);
	}

	public static Timer getSelectedTimer() {
		return selectedTimer;
	}

	public static void setSelectedTimer(Timer selectedTimer) {
		FlowTimer.selectedTimer = selectedTimer;
	}

	public static void setTimerLabel(String text) {
		timerLabel.setText(text);
	}

	public static void setTimerLabel(long time) {
		timerLabel.setText(String.format(Locale.ENGLISH, "%.3f", ((double) time / 1000.0)));
	}

	public static boolean isFocused() {
		return frame.isActive();
	}
	
	public static int getBeepSound() {
		return beepSound;
	}

	public static void setBeepSound(int beepSound) {
		FlowTimer.beepSound = beepSound;
	}
	
	public static void setPin(boolean val) {
		frame.setAlwaysOnTop(val);
		pinLabel.setIcon(new ImageIcon(loadImage("/image/pin_" + val + ".png")));
	}

	private static void addDefaultTimer(boolean addRemoveButton) {
		addNewTimer("Timer", "5000", 500, 5, addRemoveButton);
	}

	private static void recalcAddButton() {
		int addButtonX = ADD_BUTTON_BASE_X;
		int addButtonY = ADD_BUTTON_BASE_Y + (addButton.getHeight() + ADD_BUTTON_PADDING) * timers.size();
		addButton.setBounds(addButtonX, addButtonY, addButton.getWidth(), addButton.getHeight());
		frame.setSize(frame.getWidth(), Math.max(HEIGHT, 25 * (timers.size() + 4) + 12));
	}

	private static void setInterface(boolean enabled) {
		timers.forEach(timer -> timer.setElements(enabled));
		saveTimersButton.setEnabled(enabled);
		loadTimersButton.setEnabled(enabled);
		settingsButton.setEnabled(enabled);
		saveTimersAsButton.setEnabled(enabled);
		addButton.setEnabled(enabled);
	}
	
	private static BufferedImage loadImage(String fileName) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(FlowTimer.class.getResourceAsStream(fileName));
		} catch (IOException e) {
			ErrorHandler.handleException(e, false);
		}
		return image;
	}

	private static class BeepThread implements Runnable {

		private int index;
		private boolean isLast;
		private int numInvocations;
		private int numMaxInvocations;

		public BeepThread(int index, boolean isLast, int numMaxInvocations) {
			this.index = index;
			this.isLast = isLast;
			this.numInvocations = 0;
			this.numMaxInvocations = numMaxInvocations;
		}

		public void run() {
			AudioEngine.playSource(beepSound);
			if(SettingsWindow.getVisualCue().isSelected()) {
				new Thread(visualCueThread).start();
			}
			numInvocations++;
			if(numInvocations >= numMaxInvocations) {
				if(isLast) {
					stopTimer();
				} else {
					stopSegment(index);
				}
			}
		}
	}

	private static class VisualCueThread implements Runnable {

		public void run() {
			visualCuePanel.toggleVisualCue();
			try {
				Thread.sleep(visualCueLength);
			} catch (InterruptedException e) {
				ErrorHandler.handleException(e, false);
			}
			visualCuePanel.toggleVisualCue();
		}
	}

	private static class TimerLabelUpdateThread implements Runnable {

		private boolean isStopped;
		
		public void run() {
			isStopped = false;
			while(!isStopped) {
				setTimerLabel(activeTimer.getMaxOffset() - (System.nanoTime() - timerStartTime) / 1_000_000);
				try {
					// arbitrary sleep time to lower cpu usage
					Thread.sleep(3);
				} catch (InterruptedException e) {
					ErrorHandler.handleException(e, false);
				}
			}
		}

		public void stop() {
			isStopped = true;
		}
	}
}