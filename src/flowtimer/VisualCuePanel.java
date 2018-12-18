package flowtimer;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class VisualCuePanel extends JPanel {

	private static final long serialVersionUID = 1555922902282601483L;

	public static final int WIDTH = 125;
	public static final int HEIGHT = 60;

	public static final Color BLACK = Color.BLACK;
	public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	private JFrame parent;
	private Color color;

	public VisualCuePanel(JFrame parent) {
		super();
		this.parent = parent;
		setBounds(0, 0, WIDTH, HEIGHT);
		color = TRANSPARENT;
		repaint();
	}

	public void toggleVisualCue() {
		color = color == BLACK ? TRANSPARENT : BLACK;
		repaint();
		parent.repaint();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(color);
		g.fillRect(0, 0, WIDTH, HEIGHT);
	}
}
