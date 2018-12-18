package flowtimer;
import javax.swing.JButton;

public class MenuButton extends JButton {

	private static final long serialVersionUID = -7051612389536498756L;
	
	public static final int WIDTH = 110;
	public static final int HEIGHT = 22;
	public static final int X_BASE = 10;
	public static final int Y_BASE = 60;
	public static final int Y_PADDING = 3;
	
	public MenuButton(String text, int index) {
		super(text);
		setBounds(X_BASE, Y_BASE + index * (Y_PADDING + HEIGHT), WIDTH, HEIGHT);
	}
}
