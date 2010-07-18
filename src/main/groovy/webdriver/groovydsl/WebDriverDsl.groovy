package webdriver.groovydsl

import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.Sanselan;
import org.codehaus.plexus.util.Base64;
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.OutputType
import org.openqa.selenium.StaleElementReferenceException

import webdriver.groovydsl.ExecutionStepResult

import groovy.xml.MarkupBuilder;

import java.awt.image.BufferedImage;

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
		
		def writer = new StringWriter()
		new MarkupBuilder(writer).html {
			head {
				title("Test results")
			}
			body {
				table {
					tr {
						th("Script")
						th("Commentary")
						th("Screenshot")
					}
					
					instance.executionResults.each { int line, ExecutionStepResult step ->
						
						def imageFilename = "file://" + step.elementScreenshot
						
						tr {
							td(step.getSourceCode().trim())
							td(step.getMessage().trim())
							td {
								img(src:imageFilename)
							}
						}
					}
				}
			}
		}
		File f = File.createTempFile("webdriver-",".html")
		f << writer.toString()
		println f.canonicalPath
		
		
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
	def beforeAction(String message = "", WebElement element=null) {
		logAction(BEFORE, message, element)
	}

	/**
	 * Calls tasks to be run after executing an action:
	 *  1. Logs that the action has been successfully performed.
	 */
	def afterAction(String message = "", WebElement element=null) {
		logAction(AFTER, message, element)
	}

	/**
	 * Log an action (either before or after)
	 */
	private def logAction(def when, String message, WebElement element=null) {
		
		def loc
		if(element) {
			try {
				loc = getLocationOfElement(element)
			} catch (StaleElementReferenceException e) {
				// do nothing!
			}
		}
		
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
		
		def elementScreenshot = ""
		if(loc) {
			elementScreenshot = saveImage(getSubScreenshotData(screenshotData, loc));
		} else {
			elementScreenshot = executionResults[lineNumber]?.elementScreenshot
		}

		def stepResult = new ExecutionStepResult(
				verb: verbLine.getMethodName(),
				lineNumber: lineNumber,
				screenshotData: screenshotData,
				sourceCode: sourceLine,
				message: message,
				when: when,
				timestamp:new Date(),
				element:element,
				elementScreenshot:elementScreenshot)

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
	
	/**
	* Get the location of an element on the page (using javascript).
	* @param element element to locate
	* @return a map containing x, y, width, height in pixels
	*/
   def getLocationOfElement(WebElement element) {
	   def location = this.driver.executeScript("""
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
			   return  findPosX(h)   + ',' +
					   findPosY(h)   + ',' +
					   h.offsetWidth + ',' +
					   h.offsetHeight""", element)
	   def splitForm = location.split(',').collect { new Integer(it) }
	   [x:splitForm[0],y:splitForm[1],width:splitForm[2],height:splitForm[3]]
   }
   
   String getSubScreenshotData(String screenshotData, def loc) {
	   byte[] sourceData = Base64.decodeBase64(screenshotData.getBytes())
	   BufferedImage sourceImage = Sanselan.getBufferedImage(sourceData)
	   
	   BufferedImage region = crop(sourceImage, loc.x, loc.y, loc.width, loc.height)
   	   //File f = File.createTempFile("webdriver-", ".png")
	
	   //Sanselan.writeImage region, f, ImageFormat.IMAGE_FORMAT_PNG, [:]
	   //println f.getCanonicalPath()
	   byte[] croppedBytes = Sanselan.writeImageToBytes(region, ImageFormat.IMAGE_FORMAT_PNG, [:])
	   return new String(Base64.encodeBase64(croppedBytes))
   }
   
   BufferedImage crop(BufferedImage image, int left, int top, int width, int height) {
	   
	   left = Math.max(0, left-50)
	   top = Math.max(0, top-50)
	   width = Math.min(image.getWidth()-left, width+100)
	   height = Math.min(image.getHeight()-top, height+100)
	   
	   image.getSubimage(left, top, width, height)
   }
   
   File saveImage(String base64PngImage) {
	   File f = File.createTempFile("webdriver-ss-", ".png")
	   f << Base64.decodeBase64(base64PngImage.getBytes())
   }
}
