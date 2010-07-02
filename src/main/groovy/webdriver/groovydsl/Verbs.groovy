package webdriver.groovydsl

import org.openqa.selenium.WebElement
import groovy.time.TimeDuration
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver

/**
 * Created by IntelliJ IDEA.
 * User: richardnorth
 * Date: Jun 26, 2010
 * Time: 7:35:29 AM
 * To change this template use File | Settings | File Templates.
 */
class Verbs implements GroovyInterceptable {
	
	def navigate(Map args) {
        this.beforeAction "Navigating to $args.to"
        this.driver.get(args.to)
		this.afterAction()
    }

    def type(Map args, String text) {
	    WebElement into = justOne(args.into)
	    this.beforeAction "Typing $text into $into"
		into.clear()
	    into.sendKeys text
		this.afterAction()
    }

    def click(WebElement[] webElements) {

	    def element = justOne(webElements)

	    this.beforeAction "Clicking on $element"
        element.click()
		this.afterAction()
    }

    def page(Map args) {
        if (args.contains) {
            this.beforeAction "Checking if page contains $args.contains"
            assert args.contains instanceof WebElement[]
	        assert args.contains.size() > 0
	        this.afterAction()
        } else if (args.containsOne) {
	        this.beforeAction "Checking if page contains one $args.contains"
            assert args.containsOne instanceof WebElement[]
	        assert args.containsOne.size() == 1
	        this.afterAction()
        } else {
	        this.beforeAction "Checking if page contains element - not found"
	        assert false, "Element was not found"
        }
    }

	def after(TimeDuration waitDuration) {
		this.beforeAction "Waiting for ${waitDuration}"
		Thread.sleep waitDuration.toMilliseconds()
		this.afterAction()
	}

	def select(Map args, String optionToSelect) {

		WebElement activeComboBox = justOne(args.from)
		this.beforeAction "Selecting $optionToSelect from $activeComboBox"

		activeComboBox.findElements(By.xpath('./option')).find { WebElement option ->
			if (option.getText()==optionToSelect) option.setSelected()
		}

		this.afterAction()
	}

	WebElement justOne(WebElement[] from) {
		if (from.size() > 1) {
			throw new ElementLocationException("Ambiguous results - ${from.size()} found!")
		}
		return from[0]
	}
}
