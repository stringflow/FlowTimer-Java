package flowtimer;

import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImageLoader {

	private static HashMap<String, ImageIcon> loadedImages = new HashMap<>();
	
	public static ImageIcon loadImage(String fileName) {
		if(loadedImages.containsKey(fileName)) {
			return loadedImages.get(fileName);
		}
		ImageIcon image = null;
		try {
			image = new ImageIcon(ImageIO.read(ImageLoader.class.getResourceAsStream(fileName)));
			loadedImages.put(fileName, image);
		} catch (IOException e) {
			ErrorHandler.handleException(e, false);
		}
		return image;
	}
}