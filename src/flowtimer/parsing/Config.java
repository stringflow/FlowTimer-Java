package flowtimer.parsing;

import java.io.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Config {

	private HashMap<String, String> map;
	private HashMap<String, String> defaultMap;

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
	
	public void put(String key, String value) {
		map.put(key, value);
	}
	
	public void put(String key, int value) {
		map.put(key, String.valueOf(value));
	}
	
	public void put(String key, float value) {
		map.put(key, String.valueOf(value));
	}
	
	public void put(String key, boolean value) {
		map.put(key, String.valueOf(value));
	}

	public String getString(String entry) {
		String result = map.get(entry);
		if(result == null) {
			result = defaultMap.get(entry);
		}
		return result;
	}

	public int getInt(String entry) {
		return Integer.parseInt(getString(entry));
	}

	public float getFloat(String entry) {
		return Float.parseFloat(getString(entry));
	}

	public boolean getBoolean(String entry) {
		return Boolean.parseBoolean(getString(entry));
	}
	
	public boolean contains(String key) {
		return map.containsKey(key);
	}

	public HashMap<String, String> getDefaultMap() {
		return defaultMap;
	}

	public void setDefaultMap(HashMap<String, String> defaultMap) {
		this.defaultMap = defaultMap;
	}
}
