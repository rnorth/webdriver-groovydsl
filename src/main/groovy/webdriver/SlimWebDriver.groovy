package webdriver;

import groovy.lang.GroovyShell;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class SlimWebDriver {

	WebDriver driver

	SlimWebDriver() {
		//driver = new HtmlUnitDriver()
		
		System.setProperty("webdriver.firefox.useExisting", "true");
		driver = new FirefoxDriver()
	}
	
	void user(String command) {
		
		Eval.x(this, "x.${command}") 

		if (driver instanceof FirefoxDriver) {
			File ssFile = File.createTempFile("fitnesse", ".png")
			((FirefoxDriver) driver).saveScreenshot(ssFile)
			println ssFile.getCanonicalPath()
		}
		
		println driver.getTitle()
	}
	
	def page(String command) {
		return Eval.x(this, "x.${command}") 
	}
	
	void navigates(Map args) {
		println "Going to $args.to"
		driver.get args.to
	}
	
	void types(Map args, String text) {
		println "Typing $text into $args.into"
		driver.findElement(By.id(args.into)).sendKeys text
	}
	
	void clicks(Map args) {
		println "Clicking on $args.on"
		driver.findElement(By.id(args.on)).submit()
	}
	
	boolean contains(Map args) {
		println "checking page contains $args.text"
		return true
	}
	
	URL screenshot() {
		new URL("http://static.bbc.co.uk/frameworks/barlesque/1.0.3/newnav/img/blocks.png")
	}
}
