package stringflow.flowtimer.timerFile;

import javafx.scene.control.Alert;
import stringflow.flowtimer.AlertBox;
import stringflow.flowtimer.TimerEntry;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class TimerFileUtil {
	
	public static final String HEADER = "FTV1.4";
	
	public static ArrayList<TimerEntry> loadTimers(File file) {
		ArrayList<TimerEntry> result = new ArrayList<>();
		try {
			String lines[] = readFile(file).split("\n");
			Version version = Version.fromHeader(lines[0]);
			if(version == Version.UNKNOWN) {
				AlertBox.showAlert(Alert.AlertType.ERROR, "FlowTimer", "Error: unsupported file format.");
				return null;
			}
			int timerIndex = 0;
			for(int i = version.aboveVersion(1, 4) ? 1 : 2; i < lines.length; i++) {
				String attributes[] = lines[i].split(",");
				result.add(new TimerEntry(timerIndex, attributes[0], Arrays.stream(Arrays.copyOfRange(attributes, 3, attributes.length)).mapToLong(Long::valueOf).toArray(), Long.parseLong(attributes[1]), Integer.parseInt(attributes[2]), timerIndex > 0));
				timerIndex++;
			}
			return result;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void saveTimers(File target, Collection<TimerEntry> timers) {
		try {
			String text = "";
			text += HEADER + "\n";
			for(TimerEntry entry : timers) {
				text += String.format("%s,%d,%d,%s\n", entry.getName(), entry.getInterval(), entry.getNumBeeps(), Arrays.toString(entry.getOffsets()).replaceAll("(\\[|\\])", "").replace(" ", ""));
			}
			writeFile(target, text);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String readFile(File file) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		StringBuilder result = new StringBuilder();
		while((line = reader.readLine()) != null) {
			result.append(line).append("\n");
		}
		reader.close();
		return result.toString();
	}
	
	private static void writeFile(File target, String text) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(target));
		writer.write(text);
		writer.close();
	}
}
