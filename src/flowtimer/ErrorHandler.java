package flowtimer;

import java.io.PrintWriter;

import javax.swing.JOptionPane;

public class ErrorHandler {

	public static void handleException(Exception e, boolean deleteSettings) {
		JOptionPane.showMessageDialog(null, "An error has occured, please send the traceback.txt file to the developer for further help.", "Error", JOptionPane.ERROR_MESSAGE);
		try {
			PrintWriter writer = new PrintWriter("traceback.txt");
			e.printStackTrace(writer);
			writer.flush();
			writer.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if(deleteSettings) {
			int result = JOptionPane.showOptionDialog(null, "Do you want to delete the settings file?", "Error", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, 0);
			if(result == 0) {
				FlowTimer.SETTINGS_FILE.delete();
			}
		}
		System.exit(1);
	}
}