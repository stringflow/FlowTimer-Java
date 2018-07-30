package stringflow.cheatontimer;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GlobalScreenListener implements NativeKeyListener {
	
	public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
	
	}
	
	public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
		if(nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_R && FlowTimer.mainFrame.isFocused()) {
			FixedOffsetTab.instance.onStartButtonPress();
		}
	}
	
	public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
	
	}
}
