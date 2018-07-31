package stringflow.cheatontimer;

import javafx.scene.control.Alert;
import javafx.scene.layout.Region;

public class AlertBox {

	public static void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.setTitle(title);
		alert.showAndWait();
	}
}