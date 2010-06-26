package webdriver.groovydsl

import org.openqa.selenium.WebElement
import groovy.time.TimeDuration

/**
 * Created by IntelliJ IDEA.
 * User: richardnorth
 * Date: Jun 26, 2010
 * Time: 7:35:29 AM
 * To change this template use File | Settings | File Templates.
 */
class Verbs {
     def navigate(Map args) {
        println "Navigating to $args.to"
        this.driver.get(args.to)
    }

    def type(Map args, String text) {
        WebElement into = args.into
        into.sendKeys text
        println "Type $args"
    }

    def click(WebElement webElement) {
        webElement.click()
    }

    def page(Map args) {
        if (args.contains) {
            println "checking if page contains $args.contains"
            return args.contains instanceof WebElement
        }
    }

	def after(TimeDuration waitDuration) {
		Thread.sleep waitDuration.toMilliseconds()
	}
}
