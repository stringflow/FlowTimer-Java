package flowtimer;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GlobalScreenListener implements NativeKeyListener {

	public void nativeKeyPressed(NativeKeyEvent e) {
		if(KeyInput.waitDialog != null) {
			KeyInput.selectedInput.set(NativeKeyEvent.getKeyText(e.getKeyCode()), e.getKeyCode());
			KeyInput.waitDialog.dispose();
			KeyInput.waitDialog = null;
		}
		if(!SettingsWindow.isVisible()) {
			if(FlowTimer.isFocused() || SettingsWindow.getGlobalStartReset().isSelected()) {
				if(e.getKeyCode() == SettingsWindow.getStartInput().getPrimaryInput().getKeyCode() || e.getKeyCode() == SettingsWindow.getStartInput().getSecondaryInput().getKeyCode()) {
					FlowTimer.onStartButtonPress();
				}
				if(e.getKeyCode() == SettingsWindow.getResetInput().getPrimaryInput().getKeyCode() || e.getKeyCode() == SettingsWindow.getResetInput().getSecondaryInput().getKeyCode()) {
					FlowTimer.onResetButtonPress();
				}
			}
			if(FlowTimer.isFocused() || SettingsWindow.getGlobalUpDown().isSelected()) {
				if(e.getKeyCode() == SettingsWindow.getUpInput().getPrimaryInput().getKeyCode() || e.getKeyCode() == SettingsWindow.getUpInput().getSecondaryInput().getKeyCode()) {
					FlowTimer.onUpKeyPress();
				}
				if(e.getKeyCode() == SettingsWindow.getDownInput().getPrimaryInput().getKeyCode() || e.getKeyCode() == SettingsWindow.getDownInput().getSecondaryInput().getKeyCode()) {
					FlowTimer.onDownKeyPress();
				}
			}
		}
	}

	public void nativeKeyReleased(NativeKeyEvent e) {
	}

	public void nativeKeyTyped(NativeKeyEvent e) {
	}
}