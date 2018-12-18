package flowtimer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TimerDocumentListener implements DocumentListener {
	
	private Timer parent;
	
	public TimerDocumentListener(Timer parent) {
		this.parent = parent;
	}

	public void insertUpdate(DocumentEvent e) {
		onChange();
	}

	public void removeUpdate(DocumentEvent e) {
		onChange();
	}

	public void changedUpdate(DocumentEvent e) {
		onChange();
	}

	private void onChange() {
		parent.select();
	}
}
