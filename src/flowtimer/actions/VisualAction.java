package flowtimer.actions;

import java.awt.Color;

import flowtimer.ErrorHandler;
import flowtimer.FlowTimer;

public class VisualAction extends Action {
	
	private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	private Color color;
	private int length;
	
	public VisualAction(FlowTimer flowtimer, Color color, int length) {
		super(flowtimer);
		this.color = color;
		this.length = length;
	}

	public void run() {
		flowtimer.getVisualPanel().setBackColor(color);
		try {
			Thread.sleep(length);
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

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public boolean shouldExecute() {
		return flowtimer.getSettings().getVisualCue().isSelected();
	}
}