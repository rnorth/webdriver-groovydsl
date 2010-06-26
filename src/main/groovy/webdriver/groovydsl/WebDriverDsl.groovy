package webdriver.groovydsl

import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxWebElement
import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.By
import org.openqa.selenium.firefox.FirefoxDriver

/**
 * Created by IntelliJ IDEA.
 * User: richardnorth
 * Date: Jun 22, 2010
 * Time: 8:23:46 PM
 * To change this template use File | Settings | File Templates.
 */
@Mixin([Nouns,Verbs])
class WebDriverDsl {

    WebDriver driver

    WebDriverDsl() {
        System.setProperty("webdriver.firefox.useExisting", "true");
		//driver = new FirefoxDriver()
        driver = new HtmlUnitDriver()
    }

    def run(String script) {
        Eval.x(this, """use(org.codehaus.groovy.runtime.TimeCategory) {
                            x.with {
                                $script
                            }
                        }""")
    }

    WebDriver getDriver() {
        return driver
    }

    
}
