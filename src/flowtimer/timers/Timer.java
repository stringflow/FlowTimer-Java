package flowtimer.timers;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.jnativehook.keyboard.NativeKeyEvent;

import flowtimer.FlowTimer;
import flowtimer.ITimerLabelUpdateCallback;

public abstract class Timer extends JPanel {
	
	private static final long serialVersionUID = 4300353226811341053L;
	
	protected FlowTimer flowtimer;
	
	public Timer(FlowTimer flowtimer) {
		this.flowtimer = flowtimer;
		setLayout(null);
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				flowtimer.getFrame().requestFocus();
			}
		});
	}

	public abstract void onLoad();
	public abstract void onTimerStart(long startTime);
	public abstract void onTimerStop();
	public abstract void onTimerLabelUpdate(long time);
	public abstract void onKeyEvent(NativeKeyEvent e);
	public abstract void setInterface(boolean enabled);
	public abstract ITimerLabelUpdateCallback getTimerLabelUpdateCallback();
	public abstract boolean canStartTimer();
}