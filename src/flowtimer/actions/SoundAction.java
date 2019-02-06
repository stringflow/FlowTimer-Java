package flowtimer.actions;

import flowtimer.FlowTimer;
import flowtimer.OpenAL;

public class SoundAction extends Action {
	
	private int sound;

	public SoundAction(FlowTimer flowtimer, int sound) {
		super(flowtimer);
		this.sound = sound;
	}

	public void run() {
		OpenAL.playSource(sound);
	}

	public int getSound() {
		return sound;
	}

	public void setSound(int sound) {
		this.sound = sound;
	}

	public boolean shouldExecute() {
		return true;
	}
}