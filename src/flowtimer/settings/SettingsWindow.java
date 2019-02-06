package flowtimer.settings;

import java.awt.Dialog.ModalityType;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import flowtimer.FlowTimer;

public class SettingsWindow {

	public static final int WIDTH = 290;
	public static final int HEIGHT = 260;
	public static final String TITLE = "FlowTimer 1.6 - Settings";

	private JDialog dialog;

	private KeyInput startInput;
	private KeyInput stopInput;
	private KeyInput upInput;
	private KeyInput downInput;
	private JCheckBox globalStartReset;
	private JCheckBox globalUpDown;
	private JCheckBox visualCue;
	private JLabel beepSoundLabel;
	private JComboBox<String> beepSound;
	private JLabel keyTriggerLabel;
	private JComboBox<String> keyTrigger;

	public SettingsWindow(FlowTimer flowtimer) {
		dialog = new JDialog(flowtimer.getFrame(), TITLE, ModalityType.APPLICATION_MODAL);
		dialog.setSize(WIDTH, HEIGHT);
		dialog.setLayout(null);
		dialog.setLocationRelativeTo(null);
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dialog.setResizable(false);
		dialog.setVisible(false);

		startInput = new KeyInput(dialog, 0, "Start");
		stopInput = new KeyInput(dialog, 1, "Stop");
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
		flowtimer.getSoundMap().keySet().forEach(beepSound::addItem);
		beepSound.addActionListener(e -> {
			flowtimer.getSoundAction().setSound(flowtimer.getSoundMap().get(beepSound.getSelectedItem()));
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

		dialog.add(globalStartReset);
		dialog.add(globalUpDown);
		dialog.add(visualCue);
		dialog.add(beepSoundLabel);
		dialog.add(beepSound);
		dialog.add(keyTriggerLabel);
		dialog.add(keyTrigger);
	}

	public void show() {
		dialog.setVisible(true);
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

	public JCheckBox getGlobalStartReset() {
		return globalStartReset;
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

	public boolean isVisible() {
		return dialog.isVisible();
	}
}