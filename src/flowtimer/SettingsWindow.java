package flowtimer;

import java.awt.Dialog.ModalityType;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class SettingsWindow {

	public static final int WIDTH = 290;
	public static final int HEIGHT = 260;
	public static final String TITLE = "FlowTimer 1.5 - Settings";

	private static JDialog dialog;

	private static KeyInput startInput;
	private static KeyInput resetInput;
	private static KeyInput upInput;
	private static KeyInput downInput;
	private static JCheckBox globalStartReset;
	private static JCheckBox globalUpDown;
	private static JCheckBox visualCue;
	private static JLabel beepSoundLabel;
	private static JComboBox<String> beepSound;
	private static JLabel keyTriggerLabel;
	private static JComboBox<String> keyTrigger;

	public static void create(JFrame parent) {
		dialog = new JDialog(parent, TITLE, ModalityType.APPLICATION_MODAL);
		dialog.setSize(WIDTH, HEIGHT);
		dialog.setLayout(null);
		dialog.setLocationRelativeTo(null);
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dialog.setResizable(false);
		dialog.setVisible(false);

		startInput = new KeyInput(dialog, 0, "Start");
		resetInput = new KeyInput(dialog, 1, "Reset");
		upInput = new KeyInput(dialog, 2, "Up");
		downInput = new KeyInput(dialog, 3, "Down");

		globalStartReset = new JCheckBox("Global Start/Reset");
		globalUpDown = new JCheckBox("Global Up/Down");
		visualCue = new JCheckBox("Visual Cue");

		globalStartReset.setBounds(5, 155, 120, 20);
		globalUpDown.setBounds(5, 175, 120, 20);
		visualCue.setBounds(5, 195, 120, 20);

		beepSound = new JComboBox<>();
		beepSound.setBounds(46, 106, 85, 21);
		((JLabel)beepSound.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		FlowTimer.BEEP_MAP.keySet().forEach(beepSound::addItem);
		beepSound.addActionListener(e -> {
			FlowTimer.setBeepSound(FlowTimer.BEEP_MAP.get(beepSound.getSelectedItem()));
			if(isVisible()) {
				AudioEngine.playSource(FlowTimer.getBeepSound());
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

		dialog.add(globalStartReset);
		dialog.add(globalUpDown);
		dialog.add(visualCue);
		dialog.add(beepSoundLabel);
		dialog.add(beepSound);
		dialog.add(keyTriggerLabel);
		dialog.add(keyTrigger);
	}

	public static void show() {
		dialog.setVisible(true);
	}

	public static KeyInput getStartInput() {
		return startInput;
	}

	public static KeyInput getResetInput() {
		return resetInput;
	}

	public static KeyInput getUpInput() {
		return upInput;
	}

	public static KeyInput getDownInput() {
		return downInput;
	}

	public static JCheckBox getGlobalStartReset() {
		return globalStartReset;
	}

	public static JCheckBox getGlobalUpDown() {
		return globalUpDown;
	}

	public static JCheckBox getVisualCue() {
		return visualCue;
	}

	public static JComboBox<String> getBeepSound() {
		return beepSound;
	}

	public static JComboBox<String> getKeyTrigger() {
		return keyTrigger;
	}

	public static boolean isVisible() {
		return dialog.isVisible();
	}
}