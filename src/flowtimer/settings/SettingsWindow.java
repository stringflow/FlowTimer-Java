package flowtimer.settings;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import flowtimer.FlowTimer;
import flowtimer.OpenAL;

public class SettingsWindow extends JDialog {

	private static final long serialVersionUID = 6382472812309191157L;
	
	public static final int WIDTH = 290;
	public static final int HEIGHT = 260;
	public static final String TITLE = "FlowTimer 1.6 - Settings";

	private FlowTimer flowtimer;

	private KeyInput startInput;
	private KeyInput stopInput;
	private KeyInput upInput;
	private KeyInput downInput;
	private JCheckBox globalStartStop;
	private JCheckBox globalUpDown;
	private JCheckBox visualCue;
	private JLabel beepSoundLabel;
	private JComboBox<String> beepSound;
	private JLabel keyTriggerLabel;
	private JComboBox<String> keyTrigger;

	public SettingsWindow(FlowTimer flowtimer) {
		super(flowtimer.getFrame(), TITLE, ModalityType.APPLICATION_MODAL);
		this.flowtimer = flowtimer;
		setSize(WIDTH, HEIGHT);
		setLayout(null);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);
		setVisible(false);
		
		globalStartStop = new JCheckBox("Global Start/Stop");
		globalUpDown = new JCheckBox("Global Up/Down");

		startInput = new KeyInput(this, 0, "Start").setGlobalCheckbox(globalStartStop);
		stopInput = new KeyInput(this, 1, "Stop").setGlobalCheckbox(globalStartStop);
		upInput = new KeyInput(this, 2, "Up").setGlobalCheckbox(globalUpDown);
		downInput = new KeyInput(this, 3, "Down").setGlobalCheckbox(globalUpDown);

		visualCue = new JCheckBox("Visual Cue");

		globalStartStop.setBounds(5, 155, 120, 20);
		globalUpDown.setBounds(5, 175, 120, 20);
		visualCue.setBounds(5, 195, 120, 20);

		//TODO: Maybe pull these directly from the directory instead of hard coding them like this
		beepSound = new JComboBox<>(new String[] { "beep", "clack", "clap", "click1", "ping1", "ping2" });
		beepSound.setBounds(46, 106, 85, 21);
		((JLabel)beepSound.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		beepSound.addActionListener(e -> {
			flowtimer.getSoundAction().setSound(OpenAL.createSource("/sound/" + beepSound.getSelectedItem() + ".wav"));
			if(isVisible()) {
				flowtimer.getSoundAction().run();
			}
		});
		
		beepSoundLabel = new JLabel("Beep:");
		beepSoundLabel.setBounds(10, 105, 50, 20);
		
		keyTrigger = new JComboBox<>();
		keyTrigger.setBounds(46, 131, 85, 21);
		((JLabel)keyTrigger.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		keyTrigger.addItem("On Press");
		keyTrigger.addItem("On Release");
		
		keyTriggerLabel = new JLabel("Key:");
		keyTriggerLabel.setBounds(10, 130, 50, 20);

		add(globalStartStop);
		add(globalUpDown);
		add(visualCue);
		add(beepSoundLabel);
		add(beepSound);
		add(keyTriggerLabel);
		add(keyTrigger);
	}

	public KeyInput getStartInput() {
		return startInput;
	}

	public KeyInput getStopInput() {
		return stopInput;
	}

	public KeyInput getUpInput() {
		return upInput;
	}

	public KeyInput getDownInput() {
		return downInput;
	}

	public JCheckBox getGlobalStartStop() {
		return globalStartStop;
	}

	public JCheckBox getGlobalUpDown() {
		return globalUpDown;
	}

	public JCheckBox getVisualCue() {
		return visualCue;
	}

	public JComboBox<String> getBeepSound() {
		return beepSound;
	}

	public JComboBox<String> getKeyTrigger() {
		return keyTrigger;
	}

	public FlowTimer getFlowtimer() {
		return flowtimer;
	}
}