package flowtimer.parsing.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

public class Config {

	private ArrayList<ConfigObject> config;

	public Config() {
		config = new ArrayList<>();
	}

	public void put(ConfigObject object) {
		config.add(object);
	}

	public void load(File file) throws Exception {
		HashMap<String, String> processedMap = new HashMap<>();
		
		if(file.exists()) {
			BufferedReader br = new BufferedReader(new FileReader(file));
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
				processedMap.put(tokens[0].trim(), tokens[1].trim());
			}
			br.close();
		}

		config.stream().forEach(entry -> entry.load(processedMap));
	}

	public void save(File file) throws Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		for(ConfigObject object : config) {
			bw.write(object.getToWrite() + "\n");
		}
		bw.close();
	}
}
