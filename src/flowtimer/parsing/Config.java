package flowtimer.parsing;

import java.io.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Config {

	private HashMap<String, String> map;

	public Config(String fileName) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		map = new HashMap<String, String>();
		String line;
		int lineNumber = 0;
		while((line = br.readLine()) != null) {
			lineNumber++;
			if(line.isEmpty()) {
				continue;
			}

			char start = line.charAt(0);
			if(start == '[' || start == '#') {
				continue;
			}
			String[] tokens = line.split("=");
			if(tokens.length != 2) {
				br.close();
				throw new ParseException("Line has too many '=' (line " + lineNumber + ")", lineNumber);
			}
			map.put(tokens[0].trim(), tokens[1].trim());
		}
		br.close();
	}

	public Config(HashMap<String, String> map) {
		this.map = map;
	}

	public void write(String fileName) throws Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String, String> pair = it.next();
			String line = pair.getKey() + "=" + pair.getValue() + "\n";
			bw.write(line);
		}
		bw.close();
	}

	public String getString(String entry) {
		String result = map.get(entry);
		if(result != null && result.charAt(0) == '$') {
			return getString(result.substring(1));
		}
		return result;
	}

	public int getInt(String entry) {
		return Integer.parseInt(getString(entry));
	}

	public double getDouble(String entry) {
		return Double.parseDouble(getString(entry));
	}

	public boolean getBoolean(String entry) {
		return Boolean.parseBoolean(getString(entry));
	}

	public String getStringWithDefault(String entry, String defaultValue) {
		String result = getString(entry);
		if(result == null) {
			result = defaultValue;
		}
		return result;
	}

	public int getIntWithDefault(String entry, String defaultValue) {
		return Integer.parseInt(getStringWithDefault(entry, defaultValue));
	}

	public double getDoubleWithDefault(String entry, String defaultValue) {
		return Double.parseDouble(getStringWithDefault(entry, defaultValue));
	}

	public boolean getBooleanWithDefault(String entry, String defaultValue) {
		return Boolean.parseBoolean(getStringWithDefault(entry, defaultValue));
	}
	
	public boolean contains(String key) {
		return map.containsKey(key);
	}
}
