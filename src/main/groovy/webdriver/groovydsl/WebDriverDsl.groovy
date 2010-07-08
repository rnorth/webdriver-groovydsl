package webdriver.groovydsl

import org.openqa.selenium.WebDriver

import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.OutputType

/**
 * WebDriver Groovy DSL wrapper class.
 * 
 * @author Richard North <rich.north@gmail.com>
 */
@Mixin([Nouns,Verbs])
class WebDriverDsl {
	
	static final String BEFORE = "BEFORE"
	static final String AFTER = "AFTER"
	private static final Integer SCRIPT_OFFSET = 3

	/**
	 * Variables held by the instance created in the run method.
	 */
    WebDriver driver
	String script
	Map executionResults = [:]
	
	/**
	 * Main entry point.
	 * @param script Script to run, as a String
	 */
    static def run(String script) {
	    
		//driver = new RemoteWebDriver(new URL("http://localhost:3001/wd"), DesiredCapabilities.firefox())

		WebDriver driver = new FirefoxDriver()
		driver.manage().deleteAllCookies()
		WebDriverDsl instance = new WebDriverDsl(driver, script)
		
		/*
		 * If the code snippet below is altered, SCRIPT_OFFSET must be updated to match
		 * the line number that $script appears on.
		 */
		try {
	        Eval.x(instance, """use(org.codehaus.groovy.runtime.TimeCategory) {
		                        	x.with {
		                            	$script
		                        	}
		                    	}""")
		} finally {
	    	driver.close()
		}
		
		return instance.executionResults
    }

	/**
	 * Private constructor, only invoked by static run method.
	 */
	private WebDriverDsl(WebDriver driver, String script) {
		this.driver = driver
		this.script = script
		this.executionResults = [:]
	}

	/**
	 * Used by mixin classes.
	 * @return WebDriver instance
	 */
    WebDriver getDriver() {
        return driver
    }

	/**
	 * Calls tasks to be run before executing an action:
	 *  1. Logs that the action is about to be performed.
	 */
	def beforeAction(String message = "") {
		logAction(BEFORE, message)
	}

	/**
	 * Calls tasks to be run after executing an action:
	 *  1. Logs that the action has been successfully performed.
	 */
	def afterAction(String message = "") {
		logAction(AFTER, message)
	}

	/**
	 * Log an action (either before or after)
	 */
	private def logAction(def when, String message) {
		// Find the line number we were called from. RuntimeException is not thrown!
		def stacktrace = new RuntimeException().getStackTrace()
		def verbLine = discoverVerbMethod(stacktrace)
		def scriptLine = discoverScriptLine(stacktrace)

		// Take a screenshot if the WebDriver is capable.
		String screenshotData = takeScreenshot()

		def lineNumber = scriptLine.getLineNumber() - SCRIPT_OFFSET
		def sourceLine = script.split('\n')[lineNumber]			// Naive grabbing of active line
		
		if (message == "") {
			/* 
			 * we don't typically expect to receive a message for afterAction,
			 * so find out what the message used before was and reuse.
			 */
			message = executionResults[lineNumber]?.message	
		}

		def stepResult = new ExecutionStepResult(
				verb: verbLine.getMethodName(),
				lineNumber: lineNumber,
				screenshotData: screenshotData,
				sourceCode: sourceLine,
				message: message,
				when: when,
				timestamp:new Date())

		executionResults.put(lineNumber, stepResult)
	}

	/**
	 * Take a screenshot, if supported by the WebDriver.
	 * @return Base64 encoded screenshot
	 */
	private String takeScreenshot() {
		def screenshot = "none"
		if (driver instanceof TakesScreenshot) {
			screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.BASE64)
		}
		return screenshot
	}

	/**
	 * Find the line of the script that triggered the current action.
	 * @param stacktrace A stacktrace thrown somewhere inside script execution
	 * @return the specific stack trace element corresponding to the execution of the calling line in the script
	 */
	private StackTraceElement discoverScriptLine(StackTraceElement[] stacktrace) {
		stacktrace.find {
			it.getClassName().startsWith("Script1")
		}
	}

	/**
	 * Find the verb that constitutes the current action.
	 * @param stacktrace A stacktrace thrown somewhere inside execution of a Verb
	 * @return the specific stack trace element corresponding to the execution of the Verb
	 */
	private StackTraceElement discoverVerbMethod(StackTraceElement[] stacktrace) {
		stacktrace.find {
			it.getClassName().startsWith("webdriver.groovydsl.Verbs") && 
				it.getMethodName() != "invokeMethod"
		}
	}
}
