package flowtimer.timers;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jnativehook.keyboard.NativeKeyEvent;

import flowtimer.FlowTimer;
import flowtimer.ITimerLabelUpdateCallback;
import flowtimer.IntTextField;

public class VariableTimer extends BaseTimer {

	private static final long serialVersionUID = 8201416389693271334L;
	
	private VariableComponent<IntTextField> frameComponent;
	private VariableComponent<JComboBox<Float>> fpsComponent;
	private VariableComponent<IntTextField> offsetComponent;
	private VariableComponent<IntTextField> intervalComponent;
	private VariableComponent<IntTextField> numBeepsComponent;
	private JButton submitButton;

	public VariableTimer(FlowTimer flowtimer) {
		super(flowtimer);

		frameComponent = new VariableComponent<IntTextField>(0, "Frame", new IntTextField(false), 80, 20);
		fpsComponent = new VariableComponent<JComboBox<Float>>(1, "FPS", new JComboBox<Float>(new Float[] { 60.0f, 59.7275f, 59.8261f }), 80, 20);
		offsetComponent = new VariableComponent<IntTextField>(2, "Offset", new IntTextField(true), 80, 20);
		intervalComponent = new VariableComponent<IntTextField>(3, "Interval", new IntTextField(false), 80, 20);
		numBeepsComponent = new VariableComponent<IntTextField>(4, "Beeps", new IntTextField(false), 80, 20);

		frameComponent.getComponent().getDocument().addDocumentListener(new VariableElementDocumentListener());
		offsetComponent.getComponent().getDocument().addDocumentListener(new VariableElementDocumentListener());
		intervalComponent.getComponent().getDocument().addDocumentListener(new VariableElementDocumentListener());
		numBeepsComponent.getComponent().getDocument().addDocumentListener(new VariableElementDocumentListener());
		
		submitButton = new JButton("Submit");
		submitButton.setBounds(285, 26, 80, 22);
		submitButton.setEnabled(false);
		submitButton.addActionListener(e -> {
			long passedTime = flowtimer.getTimerLabelUpdateThread().getTimerLabelCallback().getTime(flowtimer.getTimerStartTime());
			long offsets[] = { getVariableOffset() - passedTime };
			int interval = intervalComponent.getComponent().getValue();
			int numBeeps = numBeepsComponent.getComponent().getValue();
			long universalOffset = offsetComponent.getComponent().getValue();
			flowtimer.scheduleActions(offsets, interval, numBeeps, universalOffset);
			setVariableInterface(false);
			submitButton.setEnabled(false);
		});
		
		frameComponent.add(this);
		fpsComponent.add(this);
		offsetComponent.add(this);
		intervalComponent.add(this);
		numBeepsComponent.add(this);
		add(submitButton);
	}
	
	private boolean isVariableDataValid() {
		if(!intervalComponent.getComponent().getText().matches("^0*[1-9]\\d*$")) {
			return false;
		}
		if(!numBeepsComponent.getComponent().getText().matches("^0*[1-9]\\d*$")) {
			return false;
		}
		if(!frameComponent.getComponent().getText().matches("^0*[1-9]\\d*$")) {
			return false;
		}
		if(!offsetComponent.getComponent().getText().matches("^-?\\d+$")) {
			return false;
		}
		if(flowtimer.isTimerRunning()) {
			if(getVariableOffset() - (intervalComponent.getComponent().getValue() * numBeepsComponent.getComponent().getValue()) < flowtimer.getTimerLabelUpdateThread().getTimerLabelCallback().getTime(flowtimer.getTimerStartTime())) {
				return false;
			}
		}
		return true;
	}
	
	private long getVariableOffset() {
		return (long) (Float.parseFloat(frameComponent.getComponent().getText()) / (float) fpsComponent.getComponent().getSelectedItem() * 1000.0f);
	}

	public void onLoad() {
		flowtimer.setTimerLabel(0);
	}

	public void onTimerStart(long startTime) {
	}

	public void onTimerStop() {
		flowtimer.setTimerLabel(0);
		setVariableInterface(true);
	}

	public void onTimerLabelUpdate(long time) {
		submitButton.setEnabled(flowtimer.isTimerRunning() && !flowtimer.areActionsScheduled() && isVariableDataValid());
	}

	public void onKeyEvent(NativeKeyEvent e) {
	}

	public void setInterface(boolean enabled) {
	}
	
	public void setVariableInterface(boolean enabled) {
		fpsComponent.setEnabled(enabled);
		offsetComponent.setEnabled(enabled);
		intervalComponent.setEnabled(enabled);
		numBeepsComponent.setEnabled(enabled);
		frameComponent.setEnabled(enabled);
	}
	
	public ITimerLabelUpdateCallback getTimerLabelUpdateCallback() {
		return (startTime) -> (System.nanoTime() - startTime) / 1_000_000;
	}

	public boolean canStartTimer() {
		return true;
	}
	
	public VariableComponent<IntTextField> getFrameComponent() {
		return frameComponent;
	}

	public VariableComponent<JComboBox<Float>> getFpsComponent() {
		return fpsComponent;
	}

	public VariableComponent<IntTextField> getOffsetComponent() {
		return offsetComponent;
	}

	public VariableComponent<IntTextField> getIntervalComponent() {
		return intervalComponent;
	}

	public VariableComponent<IntTextField> getNumBeepsComponent() {
		return numBeepsComponent;
	}

	public class VariableComponent<E extends JComponent> {
		
		public static final int X_BASE = 150;
		public static final int X_OFFSET = 50;
		public static final int Y_BASE = 20;
		public static final int Y_PADDING = 25;

		private JLabel label;
		private E component;
		
		public VariableComponent(int index, String name, E component, int width, int height) {
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
		
		public void setEnabled(boolean enabled) {
			component.setEnabled(enabled);
		}

		public JLabel getLabel() {
			return label;
		}

		public E getComponent() {
			return component;
		}
	}

	public class VariableElementDocumentListener implements DocumentListener {

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
			submitButton.setEnabled(flowtimer.isTimerRunning() && isVariableDataValid());
		}
	}
}
