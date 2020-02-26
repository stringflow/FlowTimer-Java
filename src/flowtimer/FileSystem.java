package flowtimer;

import java.io.File;

public class FileSystem {

	private static File programRootFolder;
	private static String fileSeperator;

	public static void init() {
		fileSeperator = System.getProperty("file.separator");
		String os = (System.getProperty("os.name")).toUpperCase();
		if(os.contains("WIN")) {
			programRootFolder = new File(System.getenv("appdata"));
		} else if(os.contains("NIX") || os.contains("NUX") || os.contains("AIX")) {
			programRootFolder = new File(System.getProperty("user.home"));
		} else if(os.contains("MAC")) {
			programRootFolder = new File(System.getProperty("user.home") + fileSeperator + "Library" + fileSeperator + "Application Support");
		} else {
			ErrorHandler.handleException(new IllegalArgumentException("Invalid OS: " + os), false);
		}
	}
	
	public static File getUserHomeFolder() {
		return new File(System.getProperty("user.home"));
	}
	
	public static File getProgramRootFolder() {
		return programRootFolder;
	}
	
	public static String getSystemSeperator() {
		return fileSeperator;
	}

	public static File getFlowtimerRootFolder() {
		return new File(programRootFolder.getPath() + fileSeperator + "flowtimer");
	}
	
	public static File getOldSettingsFile() {
		return new File(programRootFolder.getPath() + fileSeperator + "flowtimer.config");
	}
	
	public static File getSettingsFile() {
		return new File(getFlowtimerRootFolder().getPath() + fileSeperator + "flowtimer.config");
	}
	
	public static File getBeepFolder() {
		return new File(getFlowtimerRootFolder().getPath() + fileSeperator + "beeps");
	}
}