package stringflow.flowtimer.audio;

import stringflow.flowtimer.FlowTimer;
import stringflow.resourceloading.TextFile;

import java.util.LinkedList;

public class BeepSound {
	
	public static LinkedList<BeepSound> loadedBeepSounds = new LinkedList<>();
	
	public static void registerBeepSounds() {
		TextFile textFile = new TextFile("/audio_map.txt", "r");
		textFile.visitAll((line, lineNumber) -> {
			String splitArray[] = line.split(" ");
			loadedBeepSounds.add(new BeepSound(splitArray[0], splitArray[1]));
		});
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