package flowtimer.parsing.config;

import java.util.HashMap;

public interface ConfigObject {

	public void load(HashMap<String, String> config);
	public String getToWrite();
}