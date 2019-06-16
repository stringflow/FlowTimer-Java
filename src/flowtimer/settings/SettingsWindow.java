package flowtimer.settings;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import flowtimer.ErrorHandler;
import flowtimer.FlowTimer;
import flowtimer.IntTextField;
import flowtimer.OpenAL;

public class SettingsWindow extends JDialog {

	private static final long serialVersionUID = 6382472812309191157L;

	public static final int WIDTH = 290;
	public static final int HEIGHT = 280;
	public static final String TITLE = "FlowTimer 1.6 - Settings";

	private FlowTimer flowtimer;

	private KeyInput startInput;
	private KeyInput stopInput;
	private KeyInput upInput;
	private KeyInput downInput;
	private JCheckBox visualCue;
	private JCheckBox globalStartStop;
	private JCheckBox globalUpDown;
	private JLabel visualCueLengthLabel;
	private IntTextField visualCueLength;
	private JLabel beepSoundLabel;
	private JComboBox<String> beepSound;
	private JLabel keyTriggerLabel;
	private JComboBox<String> keyTrigger;
	private JButton importBeepButton;
	private JButton visualCueColorButton;
	private JCheckBox darkMode;

	private String beepImportLocationBuffer;

	public SettingsWindow(FlowTimer flowtimer) {
		super(flowtimer.getFrame(), TITLE, ModalityType.APPLICATION_MODAL);
		this.flowtimer = flowtimer;
		setSize(WIDTH, HEIGHT);
		setLayout(null);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);
		setVisible(false);

		visualCue = new JCheckBox("Visual Cue");
		globalStartStop = new JCheckBox("Global Start/Stop");
		globalUpDown = new JCheckBox("Global Up/Down");
		darkMode = new JCheckBox("Dark Mode");

		visualCue.setBounds(5, 155, 120, 20);
		globalStartStop.setBounds(5, 175, 130, 20);
		globalUpDown.setBounds(5, 195, 120, 20);
		darkMode.setBounds(5, 215, 120, 20);

		visualCueLengthLabel = new JLabel("Visual Length:");
		visualCueLengthLabel.setBounds(138, 155, 120, 20);

		visualCueLength = new IntTextField(false);
		visualCueLength.setBounds(209, 156, 55, 18);

		startInput = new KeyInput(this, 0, "Start").setGlobalCheckbox(globalStartStop);
		stopInput = new KeyInput(this, 1, "Stop").setGlobalCheckbox(globalStartStop);
		upInput = new KeyInput(this, 2, "Up").setGlobalCheckbox(globalUpDown);
		downInput = new KeyInput(this, 3, "Down").setGlobalCheckbox(globalUpDown);

		// TODO: Maybe pull these directly from the directory instead of hard coding them like this
		String defaultBeeps[] = { "beep", "clack", "clap", "click1", "ping1", "ping2" };
		beepSound = new JComboBox<>(defaultBeeps);
		beepSound.setBounds(46, 106, 108, 21);
		((JLabel) beepSound.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		beepSound.addActionListener(e -> {
			try {
				if(Arrays.stream(defaultBeeps).anyMatch(beepSound.getSelectedItem().toString()::equals)) {
					flowtimer.getSoundAction().setSound(OpenAL.createSource("/sound/" + beepSound.getSelectedItem() + ".wav"));
				} else {
					flowtimer.getSoundAction().setSound(OpenAL.createSource(new File(FlowTimer.IMPORTED_BEEPS_FOLDER + "\\" + beepSound.getSelectedItem() + ".wav")));
				}
			} catch (Exception e1) {
				ErrorHandler.handleException(e1, false);
			}
			if(isVisible()) {
				flowtimer.getSoundAction().run();
			}
		});

		for(File customBeep : FlowTimer.IMPORTED_BEEPS_FOLDER.listFiles()) {
			if(customBeep.getName().endsWith(".wav")) {
				beepSound.addItem(customBeep.getName().split("\\.")[0]);
			}
		}

		beepSoundLabel = new JLabel("Beep:");
		beepSoundLabel.setBounds(10, 105, 50, 20);

		importBeepButton = new JButton("Import Beep");
		importBeepButton.setBounds(155, 105, 110, 23);
		importBeepButton.addActionListener(e -> {
			JFileChooser fileChooser = new JFileChooser(beepImportLocationBuffer);
			fileChooser.setDialogTitle("Import Beep");
			fileChooser.setFileFilter(new FileNameExtensionFilter(".wav files", "wav"));
			int result = fileChooser.showOpenDialog(this);
			beepImportLocationBuffer = fileChooser.getCurrentDirectory().getAbsolutePath();
			if(result == JFileChooser.APPROVE_OPTION) {
				try {
					ArrayList<String> allBeepSounds = getAllBeepSounds();
					File file = fileChooser.getSelectedFile();
					String fileName = file.getName().split("\\.")[0];
					if(Arrays.stream(defaultBeeps).anyMatch(fileName::equalsIgnoreCase)) {
						JOptionPane.showMessageDialog(this, "This file name is already in use by a default beep.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					boolean overwrite = allBeepSounds.stream().anyMatch(fileName::equalsIgnoreCase);
					if(overwrite) {
						if(JOptionPane.showConfirmDialog(this, "This file name is already in use by a custom beep. Do you want to overwrite?", "Overwrite?", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
							return;
						}
					}
					OpenAL.createSource(file);
					Files.copy(Paths.get(file.getPath()), Paths.get(FlowTimer.IMPORTED_BEEPS_FOLDER + "\\" + file.getName()), StandardCopyOption.REPLACE_EXISTING);
					if(!overwrite) {
						beepSound.addItem(fileName);
					}
					JOptionPane.showMessageDialog(this, "Beep sound successfully imported.", "Success", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(this, "Beep sound failed to load.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		visualCueColorButton = new JButton("Visual Cue Color");
		visualCueColorButton.setBounds(155, 131, 110, 23);
		visualCueColorButton.addActionListener(e -> {
			Color newColor = JColorChooser.showDialog(this, "Pick a color", flowtimer.getVisualAction().getColor());
			if(newColor != null) {
				flowtimer.getVisualAction().setColor(newColor);
			}
		});

		keyTrigger = new JComboBox<>();
		keyTrigger.setBounds(46, 131, 108, 21);
		((JLabel) keyTrigger.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		keyTrigger.addItem("On Press");
		keyTrigger.addItem("On Release");

		keyTriggerLabel = new JLabel("Key:");
		keyTriggerLabel.setBounds(10, 130, 50, 20);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(visualCueLength.getText().trim().isEmpty()) {
					visualCueLength.setValue(20);
				}
			}
		});

		darkMode.addActionListener(e -> {
			JOptionPane.showMessageDialog(this, "FlowTimer needs to be restarted in order for this change to take effect.");
		});

		add(globalStartStop);
		add(globalUpDown);
		add(visualCue);
		add(beepSoundLabel);
		add(beepSound);
		add(keyTriggerLabel);
		add(keyTrigger);
		add(visualCueLengthLabel);
		add(visualCueLength);
		add(importBeepButton);
		add(visualCueColorButton);
		add(darkMode);
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

	public IntTextField getVisualCueLength() {
		return visualCueLength;
	}

	public JCheckBox getDarkMode() {
		return darkMode;
	}

	public void setVisualCueLength(IntTextField visualCueLength) {
		this.visualCueLength = visualCueLength;
	}

	public String getBeepImportLocationBuffer() {
		return beepImportLocationBuffer;
	}

	public void setBeepImportLocationBuffer(String beepImportLocationBuffer) {
		this.beepImportLocationBuffer = beepImportLocationBuffer;
	}

	public FlowTimer getFlowtimer() {
		return flowtimer;
	}

	public void setBeepSound(String name) {
		if(!getAllBeepSounds().stream().anyMatch(name::equalsIgnoreCase)) {
			name = "beep";
		}
		beepSound.setSelectedItem(name);
	}

	public ArrayList<String> getAllBeepSounds() {
		ArrayList<String> result = new ArrayList<>();
		ComboBoxModel<String> model = beepSound.getModel();
		for(int i = 0; i < model.getSize(); i++) {
			result.add(model.getElementAt(i));
		}
		return result;
	}

}