package Bahwan.TestCoverageDemo;

public class codeCoverage {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("hello BCT");
		start();

	}
	public static String start() {
		System.out.println("start");
		return "start";
	}
	
	public static String stop() {
		System.out.println("stop");
		return "stop";
	}
public static String processing() {
	System.out.println("process...changed code");
	return "process";
}
}
