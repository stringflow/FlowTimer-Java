package flowtimer.actions;

import java.awt.Color;

import flowtimer.ErrorHandler;
import flowtimer.FlowTimer;

public class VisualAction extends Action {
	
	private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	private Color color;
	
	public VisualAction(FlowTimer flowtimer, Color color) {
		super(flowtimer);
		this.color = color;
	}

	public void run() {
		flowtimer.getVisualPanel().setBackColor(color);
		try {
			Thread.sleep(flowtimer.getSettings().getVisualCueLength().getValue());
		} catch (InterruptedException e) {
			ErrorHandler.handleException(e, false);
		}
		flowtimer.getVisualPanel().setBackColor(TRANSPARENT);
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