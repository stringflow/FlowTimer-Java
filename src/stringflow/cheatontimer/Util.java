package stringflow.cheatontimer;

import java.util.Arrays;

public class Util {
	
	public static int negMod(int a, int b) {
		int r = a % b;
		if(r < 0) {
			r += b;
		}
		return r;
	}
	
	public static boolean arrayContains(String array[], String value) {
		return Arrays.asList(array).contains(value);
	}
	
	public static String convertArrayToString(long data[], String separator) {
		return Arrays.toString(data).replace(",", separator).replace("[", "").replace("]", "").replace(" ", "");
	}
}