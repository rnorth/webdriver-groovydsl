package webdriver.groovydsl

import java.util.Map
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver;

class WebActor {

   /* WebDriver driver = new FirefoxDriver()
	
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
		clickable(args.on).submit()
	}
	
	def clickable(String searchString) {
		
		def results = []
		
		results = driver.findElements(By.xpath("//input[@id='$searchString']"))
		
		if (!results) throw new Exception("No clickable found!")
		
		highlight(results[0])
		println "Clickable node $results[0] found and highlighted"
		screenshot()
		
		return results[0]
	}*/
}
