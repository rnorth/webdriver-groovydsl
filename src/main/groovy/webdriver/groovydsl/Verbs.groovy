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
	    this.beforeAction "Typing $text into $args.into"
        WebElement into = args.into
        into.sendKeys text
		this.afterAction()
    }

    def click(WebElement webElement) {
	    this.beforeAction "Clicking on $webElement"
        webElement.click()
		this.afterAction()
    }

    def page(Map args) {
        if (args.contains) {
            this.beforeAction "Checking if page contains $args.contains"
            assert args.contains instanceof WebElement
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

		this.beforeAction "Selecting $optionToSelect from $args.from"

		WebElement activeComboBox = args.from
		activeComboBox.findElements(By.xpath('./option')).find { WebElement option ->
			if (option.getText()==optionToSelect) option.setSelected()
		}

		this.afterAction()
	}
}
