package flowtimer;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class IntTextField extends JTextField {

	private static final long serialVersionUID = -5728236606092859465L;

	public IntTextField(boolean allowNegative) {
		setDocument(new TextFieldLimiter(this, 256, allowNegative));
	}
	
	public IntTextField(int maxLength, boolean allowNegative) {
		setDocument(new TextFieldLimiter(this, maxLength, allowNegative));
	}
	
	public IntTextField setValue(int val) {
		setText(String.valueOf(val));
		return this;
	}
	
	public IntTextField setValue(long val) {
		setText(String.valueOf(val));
		return this;
	}
	
	public int getValue() {
		return Integer.valueOf(getText());
	}

	public boolean isEmpty() {
		return getText().equals("");
	}
	
	public boolean isValidInt() {
		return getText().matches("^-?\\d+$");
	}

	private class TextFieldLimiter extends PlainDocument {

		private static final long serialVersionUID = -7576892442371054635L;
		
		private JTextField textField;
		private int maxChar = -1;
		private boolean allowNegative;

		public TextFieldLimiter(JTextField textField, int len, boolean allowNegative) {
			this.textField = textField;
			this.maxChar = len;
			this.allowNegative = allowNegative;
		}

		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			if((str != null) && (this.maxChar > 0) && (getLength() + str.length() > this.maxChar)) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			if(!new StringBuffer(textField.getText()).insert(offs, str).toString().matches("^" + (allowNegative ? "-?" : "") + "\\d*$")) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			super.insertString(offs, str, a);
		}
	}
}
