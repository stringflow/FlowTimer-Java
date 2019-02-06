package flowtimer;

import java.io.IOException;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImageLoader {

	private static TreeMap<String, ImageIcon> cache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	
	public static ImageIcon loadImage(String fileName) {
		if(cache.containsKey(fileName)) {
			return cache.get(fileName);
		}
		ImageIcon image = null;
		try {
			image = new ImageIcon(ImageIO.read(ImageLoader.class.getResourceAsStream(fileName)));
		} catch (IOException e) {
			ErrorHandler.handleException(e, false);
		}
		cache.put(fileName, image);
		return image;
	}
}