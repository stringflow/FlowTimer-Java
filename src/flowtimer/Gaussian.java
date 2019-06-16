package flowtimer;

public class Gaussian {

	public static double pdf(double x) {
		return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI);
	}

	public static double pdf(double x, double mu, double sigma) {
		return pdf((x - mu) / sigma) / sigma;
	}

	public static double cdf(double z) {
		if(z < -8.0) {
			return 0.0;
		}
		if(z > 8.0) {
			return 1.0;
		}
		double sum = 0.0, term = z;
		for(int i = 3; sum + term != sum; i += 2) {
			sum = sum + term;
			term = term * z * z / i;
		}
		return 0.5 + sum * pdf(z);
	}

	public static double cdf(double z, double mu, double sigma) {
		return cdf((z - mu) / sigma);
	}
}