package jiggle;

public class Utils {

	public static double cube (double d) {return d * d * d;}

	public static double square (double d) {return d * d;}

	public static int intSquare (int n) {return n * n;}

	public static int power (int base, int d) {
		if (d == 0) return 1;
		else if (d == 1) return base;
		else if (d % 2 == 0) return intSquare (power (base, d / 2));
		else return base * intSquare (power (base, d / 2));
	}
	
}
