package stringflow.cheatontimer;

import javax.swing.*;
import java.awt.*;

public class TimerPanel extends JPanel {
	
	private static final Color BLACK = new Color(0, false);
	private static final Color BLANK = new Color(0, true);
	
	private boolean visualCue;
	
	public TimerPanel() {
		visualCue = false;
		repaint();
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(visualCue ? BLACK : BLANK);
		g.fillRect(0, 0, 150, 150);
	}
	
	public void setVisualCue(boolean value) {
		visualCue = value;
	}
	
	public void toggleVisualCue() {
		visualCue = !visualCue;
	}
}
