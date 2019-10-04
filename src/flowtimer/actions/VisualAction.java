package flowtimer.actions;

import java.awt.Color;

import flowtimer.ErrorHandler;
import flowtimer.FlowTimer;

public class VisualAction extends Action {
	
	private Color color;
	
	public VisualAction(FlowTimer flowtimer, Color color) {
		super(flowtimer);
		this.color = color;
	}

	public void run() {
		flowtimer.setVisualCueColor(color);
		flowtimer.getTimerLabel().repaint();
		try {
			Thread.sleep(flowtimer.getSettings().getVisualCueLength().getValue());
		} catch (InterruptedException e) {
			ErrorHandler.handleException(e, false);
		}
		flowtimer.setVisualCueColor(FlowTimer.TRANSPARENT);
		flowtimer.getTimerLabel().repaint();
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public boolean shouldExecute() {
		return flowtimer.getSettings().getVisualCue().isSelected();
	}
}