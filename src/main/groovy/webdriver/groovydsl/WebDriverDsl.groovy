package webdriver.groovydsl

import org.openqa.selenium.WebDriver

import org.openqa.selenium.firefox.FirefoxDriver

/**
 * Created by IntelliJ IDEA.
 * User: richardnorth
 * Date: Jun 22, 2010
 * Time: 8:23:46 PM
 * To change this template use File | Settings | File Templates.
 */
@Mixin([Nouns,Verbs])
class WebDriverDsl implements GroovyInterceptable {

    WebDriver driver
	static final String BEFORE = "BEFORE"
	static final String AFTER = "AFTER"

    WebDriverDsl() {
        System.setProperty("webdriver.firefox.useExisting", "true");
		//driver = new FirefoxDriver()
        //driver = new HtmlUnitDriver()
	    driver = this.getClass().getClassLoader().loadClass("org.openqa.selenium.htmlunit.HtmlUnitDriver").newInstance()
    }

    synchronized def run(String script) {

	    executionResults = [:]

	    this.script = script
        Eval.x(this, """use(org.codehaus.groovy.runtime.TimeCategory) {
                            x.with {
                                $script
                            }
                        }""")
    }

    WebDriver getDriver() {
        return driver
    }

	String script
	Map executionResults = [:]

	def beforeAction(String message = "") {
		logAction(BEFORE, message)
	}

	def afterAction(String message = "") {
		logAction(AFTER, message)
	}

	private def logAction(def when, String message) {
		// Find the line number we were called from
		def stacktrace = new RuntimeException().getStackTrace()
		def verbLine = discoverVerbMethod(stacktrace)
		def scriptLine = discoverScriptLine(stacktrace)

		String screenshotFilename = takeScreenshot()

		def lineNumber = scriptLine.getLineNumber() - 3
		def sourceLine = script.split('\n')[lineNumber]

		if (message == "") {
			// find previous message
			message = executionResults[lineNumber]?.message	
		}

		def stepResult = new ExecutionStepResult(
				verb:verbLine.getMethodName(),
				lineNumber: lineNumber,
				screenshotFilename: screenshotFilename,
				sourceCode: sourceLine,
				message: message,
				when:when,
				timestamp:new Date())

		executionResults.put(lineNumber, stepResult)
	}



	private String takeScreenshot() {
		def screenshot = "none"
		if (driver instanceof FirefoxDriver) {
			File tmpFile = File.createTempFile("webdriver", ".png")
			((FirefoxDriver) driver).saveScreenshot tmpFile
			screenshot = tmpFile.getCanonicalPath()
		}
		return screenshot
	}

	private def discoverScriptLine(StackTraceElement[] stacktrace) {
		def scriptLine = stacktrace.find {
			if (it.getClassName().startsWith("Script1")) {
				return it
			}
		}
		return scriptLine
	}

	private def discoverVerbMethod(StackTraceElement[] stacktrace) {
		def verbLine = stacktrace.find {
			if (it.getClassName().startsWith("webdriver.groovydsl.Verbs") && it.getMethodName() != "invokeMethod") {
				return it
			}
		}
		return verbLine
	}
}
