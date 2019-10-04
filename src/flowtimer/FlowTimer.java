package flowtimer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import flowtimer.actions.Action;
import flowtimer.actions.SoundAction;
import flowtimer.actions.VisualAction;
import flowtimer.parsing.config.Config;
import flowtimer.parsing.config.ConfigComment;
import flowtimer.parsing.config.ConfigEntryBoolean;
import flowtimer.parsing.config.ConfigEntryFloat;
import flowtimer.parsing.config.ConfigEntryInt;
import flowtimer.parsing.config.ConfigEntryString;
import flowtimer.parsing.config.ConfigNewLine;
import flowtimer.settings.KeyInput;
import flowtimer.settings.NamedInput;
import flowtimer.settings.SettingsWindow;
import flowtimer.timers.BaseTimer;
import flowtimer.timers.CalibrationTimer;
import flowtimer.timers.DelayTimer;
import flowtimer.timers.VariableTimer;

public class FlowTimer {

	public static final int WIDTH = 451;
	public static final int HEIGHT = 287;
	public static final String TITLE = "FlowTimer 1.8";
	public static final File MAIN_FOLDER = new File(System.getenv("appdata") + "\\flowtimer");
	public static final File SETTINGS_FILE = new File(MAIN_FOLDER.getPath() + "\\flowtimer.config");
	public static final File IMPORTED_BEEPS_FOLDER = new File(MAIN_FOLDER.getPath() + "\\beeps");
	public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
	
	private JFrame frame;
	private JTabbedPane tabbedPane;
	private SettingsWindow settingsWindow;

	private DelayTimer delayTimer;
	private VariableTimer variableTimer;
	private CalibrationTimer calibrationTimer;
	
	private Config config;
	
	private Color visualCueColor;

	private JLabel timerLabel;
	private MenuButton startButton;
	private MenuButton resetButton;
	private MenuButton settingsButton;
	private JLabel pinLabel;

	private Timer timers[];
	private boolean isTimerRunning;
	private boolean areActionsScheduled;
	private long timerStartTime;

	private SoundAction soundAction;
	private VisualAction visualAction;
	private ArrayList<Action> actions;

	public FlowTimer() throws Exception {
		initSwing();

		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);
		logger.setUseParentHandlers(false);
		GlobalScreen.registerNativeHook();
		GlobalScreen.addNativeKeyListener(new GlobalScreenListener());
		OpenAL.init();

		frame = new JFrame();
		frame.setSize(WIDTH, HEIGHT);
		frame.setTitle(TITLE);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLayout(null);
		frame.setIconImage(ImageLoader.loadImage("/image/icon.png").getImage());
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(delayTimer.haveTimersChanged()) {
					if(JOptionPane.showConfirmDialog(frame, "You've changed your timers without saving. Would you like to save your timers?", "Save timers?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						delayTimer.onSaveTimersPress();
					}
				}

				OpenAL.dispose();
				try {
					config.save(SETTINGS_FILE);
				} catch (Exception e1) {
					ErrorHandler.handleException(e1, false);
				}
			}
		});

		visualCueColor = TRANSPARENT;
		timerLabel = new JLabel("0.00") {
			private static final long serialVersionUID = 1L;
			
			public void paint(Graphics g) {
				super.paint(g);
				g.setColor(visualCueColor);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		timerLabel.setBounds(11, 20, 110, 30);
		timerLabel.setOpaque(true);
		timerLabel.setFont(new Font("Consolas", Font.BOLD, 29));

		startButton = new MenuButton("Start", 0);
		startButton.addActionListener(e -> startTimer());

		resetButton = new MenuButton("Stop", 1);
		resetButton.addActionListener(e -> stopTimer());

		settingsButton = new MenuButton("Settings", 2);
		settingsButton.addActionListener(e -> settingsWindow.setVisible(true));

		pinLabel = new JLabel();
		pinLabel.setBounds(413, 5, 16, 16);
		pinLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				setPin(!frame.isAlwaysOnTop());
			}
		});

		delayTimer = new DelayTimer(this);
		variableTimer = new VariableTimer(this);
		calibrationTimer = new CalibrationTimer(this);

		actions = new ArrayList<>();
		soundAction = new SoundAction(this, 0);
		visualAction = new VisualAction(this, null);

		tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, WIDTH, HEIGHT);
		tabbedPane.addChangeListener(e -> {
			BaseTimer tab = getSelectedTimer();
			tab.add(timerLabel);
			tab.add(startButton);
			tab.add(resetButton);
			tab.add(settingsButton);
			tab.add(pinLabel);
			tab.onLoad();
		});

		settingsWindow = new SettingsWindow(this);
		loadConfig();
		delayTimer.loadTimers();

		tabbedPane.addTab("Fixed Offset", delayTimer);
		tabbedPane.addTab("Variable Offset", variableTimer);
		tabbedPane.addTab("Offset Calibration", calibrationTimer);

		frame.add(tabbedPane);

		frame.repaint();
	}
	
	private void initSwing() throws Exception {
		if(!MAIN_FOLDER.exists()) {
			MAIN_FOLDER.mkdirs();
		}
		if(!IMPORTED_BEEPS_FOLDER.exists()) {
			IMPORTED_BEEPS_FOLDER.mkdirs();
		}

		// 1.7 file migration
		File oldSettings = new File(System.getenv("appdata") + "\\flowtimer.config");
		if(oldSettings.exists() && !SETTINGS_FILE.exists()) {
			Files.copy(Paths.get(oldSettings.getAbsolutePath()), Paths.get(SETTINGS_FILE.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
			oldSettings.delete();
		}

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}

	private void loadConfig() throws Exception {
		config = new Config();
		config.put(new ConfigComment("File System"));
		config.put(new ConfigEntryString("fileSystemLocationBuffer", System.getProperty("user.home") + "\\Desktop", value -> delayTimer.setFileSystemLocationBuffer(value), () -> delayTimer.getFileSystemLocationBuffer()));
		config.put(new ConfigEntryString("beepImportLocationBuffer", System.getProperty("user.home") + "\\Desktop", value -> settingsWindow.setBeepImportLocationBuffer(value), () -> settingsWindow.getBeepImportLocationBuffer()));
		config.put(new ConfigEntryString("timerLocationBuffer", "null", value -> delayTimer.setTimerLocationBuffer(value), () -> delayTimer.getTimerLocationBuffer()));
		
		config.put(new ConfigNewLine());
		config.put(new ConfigComment("Visual Cue"));
		config.put(new ConfigEntryBoolean("visualCueEnabled", false, value -> settingsWindow.getVisualCue().setSelected(value), () -> settingsWindow.getVisualCue().isSelected()));
		config.put(new ConfigEntryString("visualCueColor", "#000000", value -> visualAction.setColor(Color.decode(value)), () -> String.format("#%02X%02X%02X", visualAction.getColor().getRed(), visualAction.getColor().getGreen(), visualAction.getColor().getBlue())));
		config.put(new ConfigEntryInt("visualCueLength", 20, value -> settingsWindow.getVisualCueLength().setValue(value), () -> settingsWindow.getVisualCueLength().getValue()));

		config.put(new ConfigNewLine());
		config.put(new ConfigComment("Input"));
		for(KeyInput keyInput : settingsWindow.getInputs()) {
			String actionName = keyInput.getActionName().replace(":", "");
			HashMap<String, NamedInput> inputs = new HashMap<String, NamedInput>();
			inputs.put("primary", keyInput.getPrimaryInput());
			inputs.put("secondary", keyInput.getSecondaryInput());
			for(Map.Entry<String, NamedInput> input : inputs.entrySet()) {
				config.put(new ConfigEntryString(input.getKey() + actionName + "KeyName", "Unset", value -> input.getValue().setName(value), () -> input.getValue().getName()));
				config.put(new ConfigEntryInt(input.getKey() + actionName + "Key", -1, value -> input.getValue().setKeyCode(value), () -> input.getValue().getKeyCode()));
			}
		}

		config.put(new ConfigNewLine());
		config.put(new ConfigComment("Variable Timer"));
		config.put(new ConfigEntryFloat("variableFps", 59.7275f, value -> variableTimer.getFpsComponent().getComponent().setSelectedItem(value), () -> Float.valueOf(String.valueOf(variableTimer.getFpsComponent().getComponent().getSelectedItem()))));
		config.put(new ConfigEntryInt("variableOffset", 0, value -> variableTimer.getOffsetComponent().getComponent().setValue(value), () -> variableTimer.getOffsetComponent().getComponent().getValue(), () -> variableTimer.getOffsetComponent().getComponent().isValidInt()));
		config.put(new ConfigEntryInt("variableInterval", 500, value -> variableTimer.getIntervalComponent().getComponent().setValue(value), () -> variableTimer.getIntervalComponent().getComponent().getValue()));
		config.put(new ConfigEntryInt("variableNumBeeps", 5, value -> variableTimer.getNumBeepsComponent().getComponent().setValue(value), () -> variableTimer.getNumBeepsComponent().getComponent().getValue()));
		
		config.put(new ConfigNewLine());
		config.put(new ConfigComment("Calibration Timer"));
		config.put(new ConfigEntryFloat("calibrationFps", 59.7275f, value -> calibrationTimer.getFpsComponent().getComponent().setSelectedItem(value), () -> Float.valueOf(String.valueOf(calibrationTimer.getFpsComponent().getComponent().getSelectedItem()))));
		config.put(new ConfigEntryInt("calibrationInterval", 500, value -> calibrationTimer.getIntervalComponent().getComponent().setValue(value), () -> calibrationTimer.getIntervalComponent().getComponent().getValue()));
		config.put(new ConfigEntryInt("calibrationNumBeeps", 5, value -> calibrationTimer.getNumBeepsComponent().getComponent().setValue(value), () -> calibrationTimer.getNumBeepsComponent().getComponent().getValue()));
		
		config.put(new ConfigNewLine());
		config.put(new ConfigComment("Misc. Settings"));
		config.put(new ConfigEntryBoolean("globalStartStop", true, value -> settingsWindow.getGlobalStartStop().setSelected(value), () -> settingsWindow.getGlobalStartStop().isSelected()));
		config.put(new ConfigEntryBoolean("globalUpDown", true, value -> settingsWindow.getGlobalUpDown().setSelected(value), () -> settingsWindow.getGlobalUpDown().isSelected()));
		config.put(new ConfigEntryString("beepSound", "ping1", value -> settingsWindow.setBeepSound(value), () -> String.valueOf(settingsWindow.getBeepSound().getSelectedItem())));
		config.put(new ConfigEntryString("key", "On Press", value -> settingsWindow.getKeyTrigger().setSelectedItem(value), () -> String.valueOf(settingsWindow.getKeyTrigger().getSelectedItem())));
		config.put(new ConfigEntryBoolean("ping", false, value -> setPin(value), () -> frame.isAlwaysOnTop()));
		
		config.load(SETTINGS_FILE);
	}

	public void startTimer() {
		if(getSelectedTimer().canStartTimer()) {
			if(isTimerRunning) {
				stopTimer();
			}
			isTimerRunning = true;
			timerStartTime = System.nanoTime();
			getSelectedTimer().onTimerStart(timerStartTime);
			setInterface(false);
		}
	}

	public void stopTimer() {
		if(!isTimerRunning) {
			return;
		}
		if(timers != null) {
			for(int i = 0; i < timers.length; i++) {
				timers[i].cancel();
			}
		}
		isTimerRunning = false;
		areActionsScheduled = false;
		getSelectedTimer().onTimerStop();
		setInterface(true);
		frame.repaint();
	}

	public void stopTimerSegment(int index) {
		timers[index].cancel();
	}

	public void scheduleActions(long offsets[], int interval, int numBeeps, long universalOffset) {
		timers = new Timer[offsets.length];
		for(int i = 0; i < offsets.length; i++) {
			ActionThread actionThread = new ActionThread(i, i == offsets.length - 1, numBeeps);
			timers[i] = new Timer();
			timers[i].scheduleAtFixedRate(actionThread, (offsets[i] - interval * (numBeeps - 1)) + universalOffset, interval);
		}
		areActionsScheduled = true;
	}

	public void setSize(int width, int height) {
		frame.setSize(width, height);
		tabbedPane.setSize(width, height);
	}

	public void setInterface(boolean enabled) {
		settingsButton.setEnabled(enabled);
		tabbedPane.setEnabled(enabled);
	}

	public void setPin(boolean value) {
		frame.setAlwaysOnTop(value);
		pinLabel.setIcon(ImageLoader.loadImage("/image/pin_" + value + ".png"));
	}

	public BaseTimer getSelectedTimer() {
		return (BaseTimer) tabbedPane.getSelectedComponent();
	}

	public void setTimerLabel(String text) {
		timerLabel.setText(text);
	}

	public void setTimerLabel(long time) {
		timerLabel.setText(String.format(Locale.ENGLISH, "%.3f", ((double) time / 1000.0)));
	}

	public JFrame getFrame() {
		return frame;
	}

	public SoundAction getSoundAction() {
		return soundAction;
	}

	public VisualAction getVisualAction() {
		return visualAction;
	}

	public ArrayList<Action> getActions() {
		return actions;
	}

	public SettingsWindow getSettings() {
		return settingsWindow;
	}

	public void setVisualCueColor(Color visualCueColor) {
		this.visualCueColor = visualCueColor;
	}

	public boolean isTimerRunning() {
		return isTimerRunning;
	}

	public boolean areActionsScheduled() {
		return areActionsScheduled;
	}

	public long getTimerStartTime() {
		return timerStartTime;
	}
	
	public JLabel getTimerLabel() {
		return timerLabel;
	}

	public static void main(String[] args) {
		try {
			new FlowTimer();
		} catch (Exception e) {
			ErrorHandler.handleException(e, true);
		}
	}

	public class GlobalScreenListener implements NativeKeyListener {

		public void nativeKeyPressed(NativeKeyEvent e) {
			if(KeyInput.waitDialog != null) {
				KeyInput.selectedInput.set(NativeKeyEvent.getKeyText(e.getKeyCode()), e.getKeyCode());
				KeyInput.waitDialog.dispose();
				KeyInput.waitDialog = null;
			} else {
				if(settingsWindow.getKeyTrigger().getSelectedItem().toString().contains("Press")) {
					processEvent(e);
				}
			}
		}

		public void nativeKeyReleased(NativeKeyEvent e) {
			if(settingsWindow.getKeyTrigger().getSelectedItem().toString().contains("Release")) {
				processEvent(e);
			}
		}

		public void nativeKeyTyped(NativeKeyEvent e) {
		}

		private void processEvent(NativeKeyEvent e) {
			if(!settingsWindow.isVisible()) {
				if(frame.isFocused() && e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
					frame.requestFocus();
				}
				if(settingsWindow.getStartInput().isPressed(e.getKeyCode())) {
					startTimer();
				}
				if(settingsWindow.getStopInput().isPressed(e.getKeyCode())) {
					stopTimer();
				}
				getSelectedTimer().onKeyEvent(e);
			}
		}
	}

	public class ActionThread extends TimerTask {

		private int index;
		private boolean isLast;
		private int numInvocations;
		private int numMaxInvocations;

		public ActionThread(int index, boolean isLast, int numMaxInvocations) {
			this.index = index;
			this.isLast = isLast;
			this.numInvocations = 0;
			this.numMaxInvocations = numMaxInvocations;
		}

		public void run() {
			for(Action action : actions) {
				if(action.shouldExecute()) {
					new Thread(action).start();
				}
			}
			numInvocations++;
			if(numInvocations >= numMaxInvocations) {
				if(isLast) {
					stopTimer();
				} else {
					stopTimerSegment(index);
				}
			}
		}
	}
}