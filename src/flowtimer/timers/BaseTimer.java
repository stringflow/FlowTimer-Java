package flowtimer.timers;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.jnativehook.keyboard.NativeKeyEvent;

import flowtimer.FlowTimer;

public abstract class BaseTimer extends JPanel {
	
	private static final long serialVersionUID = 4300353226811341053L;
	
	protected FlowTimer flowtimer;
	
	public BaseTimer(FlowTimer flowtimer) {
		this.flowtimer = flowtimer;
		setLayout(null);
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				flowtimer.getFrame().requestFocus();
			}
		});
	}
	
	// Gets called when the tab of the timer is loaded
	public abstract void onLoad();
	
	// Gets called when the timer starts while on this tab
	public abstract void onTimerStart(long startTime);
	
	// Gets called when the timer stops while on this tab
	public abstract void onTimerStop();
	
	// Gets called on key strokes while on this tab
	public abstract void onKeyEvent(NativeKeyEvent e);
	
	// Returns whether or not the timer can be started
	public abstract boolean canStartTimer();
}