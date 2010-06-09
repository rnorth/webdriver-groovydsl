package webdriver;

import groovy.lang.GroovyShell;

import org.openqa.selenium.*;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.htmlunit.*;
import org.openqa.selenium.firefox.internal.*;

/**
 * @author richardnorth
 * (c) Richard North 2010
 *
 */
public class SlimWebDriver {

	WebDriver driver

	SlimWebDriver() {
		//driver = new HtmlUnitDriver()
		
		System.setProperty("webdriver.firefox.useExisting", "true");
		driver = new FirefoxDriver()
	}
	
	void user(String command) {
		
		Eval.x(this, "x.${command}")
		
		println driver.getTitle()
	}
	
	def assertThat(String command) {
		return Eval.me(this, "x.${command}") 
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
		clickable(args.on).submit()
	}
	
	boolean contains(Map args) {
		println "checking page contains $args.text (FAKE FOR NOW)"
		return true
	}
	
	def clickable(String searchString) {
		
		def results = []
		
		results = driver.findElements(By.xpath("//input[@id='$searchString']"))
		
		if (!results) throw new Exception("No clickable found!")
		
		highlight(results[0])
		println "Clickable node $results[0] found and highlighted"
		screenshot()
		
		return results[0]
	}
	
	def highlight(WebElement element) {
		driver.executeScript("""
				function findPosX(obj)
				{
				    var left = 0;
				    if(obj.offsetParent)
				    {
				        while(1) 
				        {
				          left += obj.offsetLeft;
				          if(!obj.offsetParent)
				            break;
				          obj = obj.offsetParent;
				        }
				    }
				    else if(obj.x)
				    {
				        left += obj.x;
				    }
				    return left;
				}
				
				function findPosY(obj)
				{
				    var top = 0;
				    if(obj.offsetParent)
				    {
				        while(1)
				        {
				          top += obj.offsetTop;
				          if(!obj.offsetParent)
				            break;
				          obj = obj.offsetParent;
				        }
				    }
				    else if(obj.y)
				    {
				        top += obj.y;
				    }
				    return top;
				}
				
				var h = arguments[0];
				
				var p = document.createElement('p');
				p.style.position = 'absolute';
				p.style.left = findPosX(h) + 'px';
				p.style.top = findPosY(h) + 'px';
				p.style.zIndex = 100000;
				p.style.background = 'red';
				p.innerHTML = '&#x2780;';
				p.style.color = 'white';
				
				document.childNodes[1].appendChild(p);
				
				h.style.border='5px solid red';""", element)
	}
	
	def screenshot() {
		if (driver instanceof FirefoxDriver) {
			File ssFile = File.createTempFile("fitnesse", ".png")
			((FirefoxDriver) driver).saveScreenshot(ssFile)
			println ssFile.getCanonicalPath()
		}
	}
}
