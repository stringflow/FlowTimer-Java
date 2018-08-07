package stringflow.flowtimer.audio;

import stringflow.flowtimer.FlowTimer;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class BeepSound {
	
	public static LinkedList<BeepSound> loadedBeepSounds = new LinkedList<>();
	
	public static void registerBeepSounds() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(BeepSound.class.getResourceAsStream("/audio_map.txt")));
			StringBuffer sb = new StringBuffer();
			String l;
			while((l = reader.readLine()) != null) {
				sb.append(l).append("\n");
			}
			String content[] = sb.toString().split("\n");
			for(String line : content) {
				String splitArray[] = line.split(" ");
				loadedBeepSounds.add(new BeepSound(splitArray[0], splitArray[1]));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static BeepSound fromString(String input) {
		for(BeepSound beepSound : loadedBeepSounds) {
			if(beepSound.name.equalsIgnoreCase(input)) {
				return beepSound;
			}
		}
		return null;
	}

	private IAudioFile audioFile;
	private String name;
	
	private BeepSound(String name, String filePath) {
		this.audioFile = FlowTimer.audioEngine.loadAudioData(filePath);
		this.name = name;
	}

	public void play() {
		audioFile.play();
	}
	
	public String getName() {
		return name;
	}
}