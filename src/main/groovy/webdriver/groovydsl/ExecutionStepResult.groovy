package webdriver.groovydsl

import org.openqa.selenium.WebElement

/**
 * Created by IntelliJ IDEA.
 * User: richardnorth
 * Date: Jun 28, 2010
 * Time: 8:08:40 AM
 * To change this template use File | Settings | File Templates.
 */


class ExecutionStepResult {
	String verb
	int lineNumber
	String screenshotData
	String sourceCode
	String message
	String when
	Date timestamp
	WebElement element
	String elementScreenshot
}
