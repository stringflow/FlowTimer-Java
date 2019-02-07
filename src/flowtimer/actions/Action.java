package flowtimer.actions;

import flowtimer.FlowTimer;

public abstract class Action implements Runnable {

	protected FlowTimer flowtimer;
	
	public Action(FlowTimer flowtimer) {
		this.flowtimer = flowtimer;
		flowtimer.getActions().add(this);
	}
	
	public boolean shouldExecute() {
		return true;
	}
}