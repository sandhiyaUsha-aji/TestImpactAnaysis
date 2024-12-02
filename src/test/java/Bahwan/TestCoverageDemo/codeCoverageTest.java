package Bahwan.TestCoverageDemo;


import org.testng.annotations.Test;

import junit.framework.Assert;

public class codeCoverageTest {

	@Test
	public void sample1() {

		Assert.assertEquals(codeCoverage.start(), "start");

	}

	@Test
	public void sample2() {
		Assert.assertEquals(codeCoverage.stop(), "stop");
	}
	@Test
	public void sample3() {
		System.out.println("i am not impacted");
	}
}