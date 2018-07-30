package stringflow.cheatontimer;

public class Util {

	public static int negMod(int a, int b) {
		int r = a % b;
		if(r < 0) {
			r += b;
		}
		return r;
	}
}