package stringflow.cheatontimer.audio;

import stringflow.cheatontimer.FlowTimer;

public enum BeepSound {
	
	BEEP("/audio/beep.wav"),
	DING("/audio/ding.wav"),
	POP("/audio/pop.wav"),
	TICK("/audio/tick.wav");
	
	public static BeepSound fromString(String input) {
		if(input.equalsIgnoreCase("beep")) {
			return BEEP;
		} else if(input.equalsIgnoreCase("pop")) {
			return POP;
		} else if(input.equalsIgnoreCase("tick")) {
			return TICK;
		} else if(input.equalsIgnoreCase("ding")) {
			return DING;
		} else {
			return null;
		}
	}

	private IAudioFile audioFile;
	
	private BeepSound(String filePath) {
		audioFile = FlowTimer.audioEngine.loadAudioData(filePath);
	}

	public void play() {
		audioFile.play();
	}
}