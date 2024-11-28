package Bahwan.TestCoverageDemo;
import org.openqa.selenium.WebDriver;

import org.openqa.selenium.chrome.ChromeDriver;

import org.testng.annotations.Test;

public class sample {
	
	
	@Test
	public void sample1() {
		 
		WebDriver driver=new  ChromeDriver();
		driver.get("https://bct3.my.salesforce.com");
	}

}
