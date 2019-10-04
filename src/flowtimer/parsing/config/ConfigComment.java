package flowtimer.parsing.config;

import java.util.HashMap;

public class ConfigComment implements ConfigObject {

	public static final char PREFIX = '#';
	
	public String comment;
	
	public ConfigComment(String comment) {
		this.comment = comment;
	}

	public void load(HashMap<String, String> config) {
	}

	public String getToWrite() {
		return PREFIX + " " + comment;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}