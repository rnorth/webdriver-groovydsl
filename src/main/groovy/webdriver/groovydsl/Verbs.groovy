package webdriver.groovydsl

import org.openqa.selenium.WebElement
import groovy.time.TimeDuration
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver

/**
 * Verbs used as mixin for WebDriverDsl.
 * @author Richard North <rich.north@gmail.com>
 */
class Verbs implements GroovyInterceptable {
	
	/**
	 * Navigate to the given URL.
	 * @param args where args.to is the URL to navigate to
	 */
	def navigate(Map args) {
        this.beforeAction "Navigating to $args.to"
        this.driver.get(args.to)
		this.afterAction()
    }

	/**
	 * Type text into an element, overwriting any present content.
	 * @param text the text to be typed
	 * @param args where args.into is the element to have text typed into
	 */
    def type(Map args, String text) {
	    WebElement into = justOne(args.into)
	
	    this.beforeAction "Typing $text", into
		into.clear()
	    into.sendKeys text
		this.afterAction "",into
    }

	/**
	 * Click on an element.
	 * @param webElements an array of elements, expected to have only 1 entry
	 * @throws ElementLocationException if ambiguous elements are passed in
	 */
    def click(WebElement[] webElements) {
	    def element = justOne(webElements)

	    this.beforeAction "Clicking", element
        element.click()
		this.afterAction "", element
    }

	/**
	 * Performs assertions around contents of page.
	 * @param args where args.contains is tested to be one or more elements; 
	 *                   args.containsOne is tested to be just one element
	 */
    def page(Map args) {
        if (args.contains) {
            this.beforeAction "Checking if page contains element", args.contains[0]
            assert args.contains instanceof WebElement[]
	        assert args.contains.size() > 0
	        this.afterAction()
        } else if (args.containsOne) {
	        this.beforeAction "Checking if page contains one element", args.contains[0]
            assert args.containsOne instanceof WebElement[]
	        assert args.containsOne.size() == 1
	        this.afterAction()
        } else {
	        this.beforeAction "Checking if page contains element - not found"
	        assert false, "Element was not found"
        }
    }

	/**
	 * Wait for a defined period.
	 * @param waitDuration amount of time to wait as a Groovy TimeDuration, e.g. 5.seconds
	 */
	def after(TimeDuration waitDuration) {
		this.beforeAction "Waiting for ${waitDuration}"
		Thread.sleep waitDuration.toMilliseconds()
		this.afterAction()
	}

	/**
	 * Select an option from a multi-option element.
	 * @param optionToSelect the text value to be selected
	 * @param args where args.from is the multi-option element to select from
	 */
	def select(Map args, String optionToSelect) {

		WebElement activeSelectionBox = justOne(args.from)
		this.beforeAction "Selecting $optionToSelect", activeSelectionBox

		activeSelectionBox.findElements(By.xpath('./option')).each { WebElement option ->
			if (option.getText()==optionToSelect) option.setSelected()
		}

		this.afterAction "", activeSelectionBox
	}

	/**
	 * Ensure that only a single element is passed in.
	 * @param from an array of WebElements, which is expected to just contain one entry
	 * @throws ElementLocationException if ambiguous elements are passed in
	 */
	private WebElement justOne(WebElement[] from) {
		if (from.size() != 1) {
			throw new ElementLocationException("Ambiguous results - expected 1 but ${from.size()} found!")
		}
		return from[0]
	}
}
