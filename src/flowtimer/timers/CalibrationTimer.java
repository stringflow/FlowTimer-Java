// credit to entrpntr for the logic: https://plnkr.co/edit/w0hvCuTtvJVC6qRAZhGE?p=preview

package flowtimer.timers;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jnativehook.keyboard.NativeKeyEvent;

import flowtimer.FlowTimer;
import flowtimer.Gaussian;
import flowtimer.ITimerLabelUpdateCallback;
import flowtimer.IntTextField;

public class CalibrationTimer extends BaseTimer {

	private static final long serialVersionUID = 7515112877872220358L;

	private CalibrationComponent<JComboBox<Double>> fpsComponent;
	private CalibrationComponent<IntTextField> targetFrameComponent;
	private CalibrationComponent<IntTextField> initialOffsetComponent;
	private CalibrationComponent<IntTextField> intervalComponent;
	private CalibrationComponent<IntTextField> beepsComponent;
	private JButton startButton;
	private JButton restartButton;
	private CalibrationComponent<IntTextField> currentOffsetComponent;
	private CalibrationComponent<IntTextField> actualFrameComponent;
	private JButton useRecommendation;
	private JButton enter;
	private JLabel percentageLabel;
	private JTextArea resultsArea;

	private HashMap<Integer, ArrayDeque<Integer>> framesHit;

	private boolean started;
	private double beta;
	private double mu;
	private double v;
	private double meanSigma;

	public CalibrationTimer(FlowTimer flowtimer) {
		super(flowtimer);

		framesHit = new HashMap<>();

		fpsComponent = new CalibrationComponent<JComboBox<Double>>(0, "FPS", new JComboBox<Double>(new Double[] { 60.0, 59.7275, 59.8261 }), 80, 20);
		initialOffsetComponent = new CalibrationComponent<IntTextField>(1, "Inital Offset", new IntTextField(false), 80, 20);
		targetFrameComponent = new CalibrationComponent<IntTextField>(2, "Target Frame", new IntTextField(false), 80, 20);
		intervalComponent = new CalibrationComponent<IntTextField>(3, "Interval", new IntTextField(false), 80, 20);
		beepsComponent = new CalibrationComponent<IntTextField>(4, "Beeps", new IntTextField(false), 80, 20);
		currentOffsetComponent = new CalibrationComponent<IntTextField>(6, "Current Offset", new IntTextField(false), 80, 20);
		actualFrameComponent = new CalibrationComponent<IntTextField>(7, "Actual Frame", new IntTextField(true), 80, 20);
		percentageLabel = new JLabel();
		resultsArea = new JTextArea();
		resultsArea.setEditable(false);
		resultsArea.setLineWrap(true);
		resultsArea.setWrapStyleWord(true);

		startButton = new JButton("Start");
		restartButton = new JButton("Restart");

		useRecommendation = new JButton();
		enter = new JButton("Enter");

		startButton.setBounds(CalibrationComponent.X_BASE, 152, 80, 20);
		restartButton.setBounds(CalibrationComponent.X_BASE + 80, 152, 80, 20);
		useRecommendation.setBounds(currentOffsetComponent.getX() + 85, currentOffsetComponent.getY(), 100, 20);
		enter.setBounds(actualFrameComponent.getX() + 85, actualFrameComponent.getY(), 100, 20);
		percentageLabel.setBounds(90, 195, FlowTimer.WIDTH - 40, 180);
		resultsArea.setBounds(10, 345, FlowTimer.WIDTH - 40, 80);

		startButton.setEnabled(false);
		restartButton.setEnabled(false);
		enter.setEnabled(false);

		FieldDocumentListener startDocumentListener = new FieldDocumentListener() {
			public void onChange() {
				startButton.setEnabled(targetFrameComponent.getComponent().isValidInt() && initialOffsetComponent.getComponent().isValidInt());
			}
		};
		targetFrameComponent.getComponent().getDocument().addDocumentListener(startDocumentListener);
		initialOffsetComponent.getComponent().getDocument().addDocumentListener(startDocumentListener);

		FieldDocumentListener enterDocumentListener = new FieldDocumentListener() {
			public void onChange() {
				enter.setEnabled(actualFrameComponent.getComponent().isValidInt() && currentOffsetComponent.getComponent().isValidInt());
			}
		};
		currentOffsetComponent.getComponent().getDocument().addDocumentListener(enterDocumentListener);
		actualFrameComponent.getComponent().getDocument().addDocumentListener(enterDocumentListener);

		currentOffsetComponent.getComponent().getDocument().addDocumentListener(new FieldDocumentListener() {
			public void onChange() {
				if(currentOffsetComponent.getComponent().isValidInt()) {
					flowtimer.setTimerLabel(currentOffsetComponent.getComponent().getValue());
				} else {
					flowtimer.setTimerLabel("Error");
				}
			}
		});

		enter.addActionListener(e -> {
			int curOffset = currentOffsetComponent.getComponent().getValue();
			int actualFrame = actualFrameComponent.getComponent().getValue();
			if(!framesHit.containsKey(curOffset)) {
				framesHit.put(curOffset, new ArrayDeque<>());
			}
			framesHit.get(curOffset).add(actualFrame);
			double offsetDiff = curOffset - getInitialOffset();
			double expectedFrame = offsetDiff / 1000.0 * getFps() + targetFrameComponent.getComponent().getValue();
			double frameDiff = actualFrame - expectedFrame;
			beta += v * Math.pow(frameDiff - mu, 2) / (2.0 * v + 2.0);
			mu = (v * mu + frameDiff) / (v + 1);
			v++;
			updateStats();
			actualFrameComponent.getComponent().setText("");
		});

		useRecommendation.addActionListener(e -> currentOffsetComponent.getComponent().setValue(Integer.valueOf(useRecommendation.getText().split(" ")[1])));

		startButton.addActionListener(e -> {
			started = true;
			updateInitialComponents();
		});

		restartButton.addActionListener(e -> {
			started = false;
			updateInitialComponents();
		});

		fpsComponent.add(this);
		targetFrameComponent.add(this);
		initialOffsetComponent.add(this);
		intervalComponent.add(this);
		beepsComponent.add(this);
		add(startButton);
		add(restartButton);
	}

	public void onLoad() {
		flowtimer.setSize(flowtimer.getFrame().getWidth(), FlowTimer.HEIGHT + 205);
	}

	public void onTimerStart(long startTime) {
		flowtimer.scheduleActions(new long[] { currentOffsetComponent.getComponent().getValue() }, intervalComponent.getComponent().getValue(), beepsComponent.getComponent().getValue(), 0);
	}

	public void onTimerStop() {
		flowtimer.setTimerLabel(currentOffsetComponent.getComponent().getValue());
	}

	public void onTimerLabelUpdate(long time) {
	}

	public void onKeyEvent(NativeKeyEvent e) {
	}

	public ITimerLabelUpdateCallback getTimerLabelUpdateCallback() {
		return (startTime) -> currentOffsetComponent.getComponent().getValue() - (System.nanoTime() - startTime) / 1_000_000;
	}

	public boolean canStartTimer() {
		return currentOffsetComponent.getComponent().isValidInt() && targetFrameComponent.getComponent().isValidInt() && started;
	}

	public CalibrationComponent<IntTextField> getIntervalComponent() {
		return intervalComponent;
	}

	public CalibrationComponent<IntTextField> getBeepsComponent() {
		return beepsComponent;
	}

	public CalibrationComponent<JComboBox<Double>> getFpsComponent() {
		return fpsComponent;
	}

	public double getFps() {
		return (double) fpsComponent.getComponent().getSelectedItem();
	}

	public int getInitialOffset() {
		return initialOffsetComponent.getComponent().getValue();
	}

	private void updateInitialComponents() {
		fpsComponent.setEnabled(!started);
		targetFrameComponent.setEnabled(!started);
		initialOffsetComponent.setEnabled(!started);
		startButton.setEnabled(!started);
		restartButton.setEnabled(started);
		if(started) {
			currentOffsetComponent.add(this);
			actualFrameComponent.add(this);
			add(useRecommendation);
			add(enter);
			add(percentageLabel);
			add(resultsArea);
			currentOffsetComponent.getComponent().setText(initialOffsetComponent.getComponent().getText());
			useRecommendation.setText("Use " + initialOffsetComponent.getComponent().getText());
			framesHit.clear();
			beta = 2.345;
			mu = 0;
			v = 4;
			updateStats();
			flowtimer.setTimerLabel(currentOffsetComponent.getComponent().getValue());
		} else {
			currentOffsetComponent.remove(this);
			actualFrameComponent.remove(this);
			remove(useRecommendation);
			remove(enter);
			remove(percentageLabel);
			remove(resultsArea);
		}
		repaint();
	}

	private void updateStats() {
		int recommendedOffset = (int) Math.round(getInitialOffset() - 1000.0 * (mu / getFps()));
		meanSigma = Math.sqrt(2.0 * beta / (v - 1.0));
		//sdSigma = Math.sqrt(beta / (0.5 * v - 1.0) - meanSigmaSq);
		useRecommendation.setText("Use " + recommendedOffset);
		resultsArea.setText("Results = " + framesHit);
		
		final int max = 8;
		String percentages = "<html>";
		for(int i = 1; i <= max; i++) {
			double f = (double) i;
			double percentage = (Gaussian.cdf(f / 2.0, 0.0, meanSigma) - Gaussian.cdf(f / -2.0, 0.0, meanSigma)) * 100.0;
			percentages += String.format(Locale.ENGLISH, "You should hit %d-frame manips %.1f%% of the time.", i, percentage);
			if(i != max) {
				percentages += "<br/>";
			}
		}
		percentages += "</html>";
		percentageLabel.setText(percentages);
	}

	public class CalibrationComponent<E extends JComponent> {

		public static final int X_BASE = 150;
		public static final int X_OFFSET = 80;
		public static final int Y_BASE = 20;
		public static final int Y_PADDING = 25;

		private JLabel label;
		private E component;

		public CalibrationComponent(int index, String name, E component, int width, int height) {
			int y = Y_BASE + index * Y_PADDING;
			label = new JLabel(name + ":");
			label.setBounds(X_BASE, y, X_OFFSET - 5, 35);
			this.component = component;
			component.setBounds(X_BASE + X_OFFSET, y + (35 - height) / 2, width, height);
		}

		public void add(JPanel parent) {
			parent.add(label);
			parent.add(component);
		}

		public void remove(JPanel parent) {
			parent.remove(label);
			parent.remove(component);
		}

		public void setEnabled(boolean enabled) {
			component.setEnabled(enabled);
		}

		public JLabel getLabel() {
			return label;
		}

		public E getComponent() {
			return component;
		}

		public int getX() {
			return (int) component.getBounds().getX();
		}

		public int getY() {
			return (int) component.getBounds().getY();
		}
	}

	public abstract class FieldDocumentListener implements DocumentListener {

		public void insertUpdate(DocumentEvent e) {
			onChange();
		}

		public void removeUpdate(DocumentEvent e) {
			onChange();
		}

		public void changedUpdate(DocumentEvent e) {
			onChange();
		}

		public abstract void onChange();
	}
}