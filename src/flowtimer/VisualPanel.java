package flowtimer;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class VisualPanel extends JPanel {

	private static final long serialVersionUID = 1555922902282601483L;

	public static final int WIDTH = 125;
	public static final int HEIGHT = 60;

	private JFrame parent;
	private Color color;

	public VisualPanel(JFrame parent) {
		super();
		this.parent = parent;
		this.color = new Color(0, 0, 0, 0);
		setBounds(0, 0, WIDTH, HEIGHT);
		repaint();
	}

	public void setBackColor(Color color) {
		this.color = color;
		repaint();
		parent.repaint();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(color);
		g.fillRect(0, 0, WIDTH, HEIGHT);
	}
}
