package flowtimer;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import flowtimer.actions.Action;
import flowtimer.actions.SoundAction;
import flowtimer.actions.VisualAction;
import flowtimer.parsing.Config;
import flowtimer.settings.KeyInput;
import flowtimer.settings.SettingsWindow;
import flowtimer.timers.BaseTimer;
import flowtimer.timers.DelayTimer;
import flowtimer.timers.VariableTimer;

public class FlowTimer {

	public static final int WIDTH = 451;
	public static final int HEIGHT = 287;
	public static final String TITLE = "FlowTimer 1.6";
	public static final String SETTINGS_FILE_LOCATION = System.getenv("appdata") + "\\flowtimer.config";
	public static final File SETTINGS_FILE = new File(SETTINGS_FILE_LOCATION);

	private JFrame frame;
	private JTabbedPane tabbedPane;
	private SettingsWindow settingsWindow;

	private DelayTimer delayTimer;
	private VariableTimer variableTimer;

	private JLabel timerLabel;
	private VisualPanel visualPanel;
	private MenuButton startButton;
	private MenuButton resetButton;
	private MenuButton settingsButton;
	private JLabel pinLabel;

	private Timer timers[];
	private boolean isTimerRunning;
	private boolean areActionsScheduled;
	private long timerStartTime;
	private TimerLabelUpdateThread timerLabelUpdateThread;

	private SoundAction soundAction;
	private VisualAction visualAction;
	private ArrayList<Action> actions;

	public FlowTimer() throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		OpenAL.init();
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);
		logger.setUseParentHandlers(false);
		GlobalScreen.registerNativeHook();
		GlobalScreen.addNativeKeyListener(new GlobalScreenListener());

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
				OpenAL.dispose();

				HashMap<String, String> map = new HashMap<>();
				map.put("fileSystemLocationBuffer", delayTimer.getFileSystemLocationBuffer());
				map.put("timerLocationBuffer", delayTimer.getTimerLocationBuffer());

				map.put("primaryStartKey", settingsWindow.getStartInput().getPrimaryInput().getKeyCode() + "");
				map.put("primaryResetKey", settingsWindow.getStopInput().getPrimaryInput().getKeyCode() + "");
				map.put("primaryUpKey", settingsWindow.getUpInput().getPrimaryInput().getKeyCode() + "");
				map.put("primaryDownKey", settingsWindow.getDownInput().getPrimaryInput().getKeyCode() + "");
				map.put("primaryStartKeyName", settingsWindow.getStartInput().getPrimaryInput().getName());
				map.put("primaryResetKeyName", settingsWindow.getStopInput().getPrimaryInput().getName());
				map.put("primaryUpKeyName", settingsWindow.getUpInput().getPrimaryInput().getName());
				map.put("primaryDownKeyName", settingsWindow.getDownInput().getPrimaryInput().getName());

				map.put("secondaryStartKey", settingsWindow.getStartInput().getSecondaryInput().getKeyCode() + "");
				map.put("secondaryResetKey", settingsWindow.getStopInput().getSecondaryInput().getKeyCode() + "");
				map.put("secondaryUpKey", settingsWindow.getUpInput().getSecondaryInput().getKeyCode() + "");
				map.put("secondaryDownKey", settingsWindow.getDownInput().getSecondaryInput().getKeyCode() + "");
				map.put("secondaryStartKeyName", settingsWindow.getStartInput().getSecondaryInput().getName());
				map.put("secondaryResetKeyName", settingsWindow.getStopInput().getSecondaryInput().getName());
				map.put("secondaryUpKeyName", settingsWindow.getUpInput().getSecondaryInput().getName());
				map.put("secondaryDownKeyName", settingsWindow.getDownInput().getSecondaryInput().getName());

				map.put("globalStartReset", settingsWindow.getGlobalStartStop().isSelected() + "");
				map.put("globalUpDown", settingsWindow.getGlobalUpDown().isSelected() + "");
				map.put("visualCue", settingsWindow.getVisualCue().isSelected() + "");
				map.put("beepSound", settingsWindow.getBeepSound().getSelectedItem() + "");

				map.put("visualCueColor", String.format("#%02X%02X%02X", visualAction.getColor().getRed(), visualAction.getColor().getGreen(), visualAction.getColor().getBlue()));
				map.put("visualCueLength", settingsWindow.getVisualCueLength().getValue() + "");
				map.put("pin", frame.isAlwaysOnTop() + "");
				map.put("key", settingsWindow.getKeyTrigger().getSelectedItem() + "");

				map.put("variableFps", variableTimer.getFpsComponent().getComponent().getSelectedItem() + "");
				if(variableTimer.getOffsetComponent().getComponent().isValidInt()) {
					map.put("variableOffset", variableTimer.getOffsetComponent().getComponent().getValue() + "");
				}
				if(variableTimer.getIntervalComponent().getComponent().isValidInt()) {
					map.put("variableInterval", variableTimer.getIntervalComponent().getComponent().getValue() + "");
				}
				if(variableTimer.getNumBeepsComponent().getComponent().isValidInt()) {
					map.put("variableNumBeeps", variableTimer.getNumBeepsComponent().getComponent().getValue() + "");
				}

				Config config = new Config(map);
				try {
					config.write(SETTINGS_FILE_LOCATION);
				} catch (Exception e1) {
					ErrorHandler.handleException(e1, false);
				}
			}
		});

		timerLabel = new JLabel("0.00");
		timerLabel.setBounds(11, 20, 120, 30);
		timerLabel.setFont(new Font("Consolas", Font.BOLD, 29));

		visualPanel = new VisualPanel(frame);

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

		actions = new ArrayList<>();
		soundAction = new SoundAction(this, 0);
		visualAction = new VisualAction(this, null);

		loadSettings();

		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(e -> {
			BaseTimer tab = getSelectedTimer();
			tab.add(timerLabel);
			tab.add(startButton);
			tab.add(resetButton);
			tab.add(settingsButton);
			tab.add(visualPanel);
			tab.add(pinLabel);
			tab.onLoad();
		});
		tabbedPane.addTab("Fixed Offset", delayTimer);
		tabbedPane.addTab("Variable Offset", variableTimer);
		tabbedPane.setBounds(0, 0, WIDTH, HEIGHT);

		frame.add(tabbedPane);

		frame.repaint();
	}

	private void loadSettings() throws Exception {
		settingsWindow = new SettingsWindow(this);

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

		defaultMap.put("visualCueColor", "#000000");
		defaultMap.put("visualCueLength", "20");
		defaultMap.put("pin", "false");

		defaultMap.put("key", "On Press");

		defaultMap.put("variableFps", "60.0");
		defaultMap.put("variableOffset", "0");
		defaultMap.put("variableInterval", "500");
		defaultMap.put("variableNumBeeps", "5");

		Config config;
		if(!SETTINGS_FILE.exists()) {
			config = new Config(defaultMap);
			config.write(SETTINGS_FILE_LOCATION);
		} else {
			config = new Config(SETTINGS_FILE_LOCATION);
		}
		config.setDefaultMap(defaultMap);

		delayTimer.setFileSystemLocationBuffer(config.getString("fileSystemLocationBuffer"));
		delayTimer.setTimerLocationBuffer(config.getString("timerLocationBuffer"));

		visualAction.setColor(Color.decode(config.getString("visualCueColor")));
		settingsWindow.getVisualCueLength().setValue(config.getInt("visualCueLength"));

		settingsWindow.getStartInput().getPrimaryInput().set(config.getString("primaryStartKeyName"), config.getInt("primaryStartKey"));
		settingsWindow.getStopInput().getPrimaryInput().set(config.getString("primaryResetKeyName"), config.getInt("primaryResetKey"));
		settingsWindow.getUpInput().getPrimaryInput().set(config.getString("primaryUpKeyName"), config.getInt("primaryUpKey"));
		settingsWindow.getDownInput().getPrimaryInput().set(config.getString("primaryDownKeyName"), config.getInt("primaryDownKey"));

		settingsWindow.getStartInput().getSecondaryInput().set(config.getString("secondaryStartKeyName"), config.getInt("secondaryStartKey"));
		settingsWindow.getStopInput().getSecondaryInput().set(config.getString("secondaryResetKeyName"), config.getInt("secondaryResetKey"));
		settingsWindow.getUpInput().getSecondaryInput().set(config.getString("secondaryUpKeyName"), config.getInt("secondaryUpKey"));
		settingsWindow.getDownInput().getSecondaryInput().set(config.getString("secondaryDownKeyName"), config.getInt("secondaryDownKey"));

		settingsWindow.getGlobalStartStop().setSelected(config.getBoolean("globalStartReset"));
		settingsWindow.getGlobalUpDown().setSelected(config.getBoolean("globalUpDown"));
		settingsWindow.getVisualCue().setSelected(config.getBoolean("visualCue"));
		settingsWindow.getBeepSound().setSelectedItem(config.getString("beepSound"));
		settingsWindow.getKeyTrigger().setSelectedItem(config.getString("key"));

		setPin(config.getBoolean("pin"));

		variableTimer.getFpsComponent().getComponent().setSelectedItem(config.getFloat("variableFps"));
		variableTimer.getOffsetComponent().getComponent().setText(config.getString("variableOffset"));
		variableTimer.getIntervalComponent().getComponent().setText(config.getString("variableInterval"));
		variableTimer.getNumBeepsComponent().getComponent().setText(config.getString("variableNumBeeps"));
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
			timerLabelUpdateThread = new TimerLabelUpdateThread(getSelectedTimer().getTimerLabelUpdateCallback());
			new Thread(timerLabelUpdateThread).start();
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
		timerLabelUpdateThread.stop();
		getSelectedTimer().onTimerStop();
		setInterface(true);
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

	public void setPin(boolean val) {
		frame.setAlwaysOnTop(val);
		pinLabel.setIcon(ImageLoader.loadImage("/image/pin_" + val + ".png"));
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

	public VisualPanel getVisualPanel() {
		return visualPanel;
	}

	public boolean isTimerRunning() {
		return isTimerRunning;
	}

	public boolean areActionsScheduled() {
		return areActionsScheduled;
	}

	public TimerLabelUpdateThread getTimerLabelUpdateThread() {
		return timerLabelUpdateThread;
	}

	public long getTimerStartTime() {
		return timerStartTime;
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

	public class TimerLabelUpdateThread implements Runnable {

		private ITimerLabelUpdateCallback timerLabelCallback;
		private boolean isStopped;

		public TimerLabelUpdateThread(ITimerLabelUpdateCallback timerLabelCallback) {
			this.timerLabelCallback = timerLabelCallback;
		}

		public void run() {
			while(!isStopped) {
				long time = timerLabelCallback.getTime(timerStartTime);
				getSelectedTimer().onTimerLabelUpdate(time);
				setTimerLabel(time);
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

		public ITimerLabelUpdateCallback getTimerLabelCallback() {
			return timerLabelCallback;
		}
	}
}