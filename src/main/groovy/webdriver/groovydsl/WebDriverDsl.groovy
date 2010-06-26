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
class WebDriverDsl {

    WebDriver driver

    WebDriverDsl() {
        System.setProperty("webdriver.firefox.useExisting", "true");
		//driver = new FirefoxDriver()
        driver = new HtmlUnitDriver()
    }

    def run(String script) {
        Eval.x(this, "x.with { $script }")
    }

    def navigate(Map args) {
        println "Navigating to $args.to"
        driver.get(args.to)
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

    WebElement textField(Map args) {
        println "textField named $args.named"
        return driver.findElement(By.name(args.named))
    }

    WebElement button(Map args) {
        println "button named $args.named"

        return driver.findElement(By.name(args.named))
    }

    WebElement button(String marked) {
        println "button marked $marked"

        return driver.findElements(By.xpath('//input')).find { WebElement it ->
             if (it.value.equalsIgnoreCase(marked)) return it
        }
    }

    WebElement text(String searchTerm) {
        println "text $searchTerm"

        return driver.findElements(By.xpath('//*')).find { WebElement it ->
            if (it.text.toLowerCase().contains(searchTerm.toLowerCase())) return it 
        }
    }

    WebElement link(String linkText) {
        println "link $linkText"

        return driver.findElement(By.linkText(linkText))
    }
}
