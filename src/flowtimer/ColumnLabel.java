package flowtimer;
import javax.swing.JLabel;

public class ColumnLabel extends JLabel {

	private static final long serialVersionUID = 6275772904824072659L;
	
	public static final int WIDTH = 55;
	public static final int HEIGHT = 20;
	public static final int X_BASE = 146;
	public static final int X_PADDING = 5;
	public static final int Y_BASE = 16;

	public ColumnLabel(String text, int index) {
		super(text);
		setBounds(X_BASE + index * (X_PADDING + WIDTH), Y_BASE, WIDTH, HEIGHT);
	}
}
