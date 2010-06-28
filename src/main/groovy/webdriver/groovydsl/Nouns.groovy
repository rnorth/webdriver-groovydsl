package webdriver.groovydsl

import org.openqa.selenium.By
import org.openqa.selenium.WebElement

/**
 * Created by IntelliJ IDEA.
 * User: richardnorth
 * Date: Jun 26, 2010
 * Time: 7:31:30 AM
 * To change this template use File | Settings | File Templates.
 */
class Nouns {
    WebElement textField(Map args) {
        println "textField named $args.named"
        return this.driver.findElement(By.name(args.named))
    }

    WebElement button(Map args) {
        println "button named $args.named"

        return this.driver.findElement(By.name(args.named))
    }

    WebElement button(String marked) {
        println "button marked $marked"

        return this.driver.findElements(By.xpath('//input')).find { WebElement it ->
             if (it.value.equalsIgnoreCase(marked)) return it
        }
    }

    WebElement text(String searchTerm) {
        println "text $searchTerm"

	    // Inefficient - could search for elements likely to contain text first
        return this.driver.findElements(By.xpath('//*')).find { WebElement it ->
            if (it.text.toLowerCase().contains(searchTerm.toLowerCase())) return it
        }
    }

    WebElement link(String linkText) {
        println "link $linkText"

        return this.driver.findElement(By.linkText(linkText))
    }

	WebElement combobox(Map args) {
		println "combobox containing $args.with"

		this.driver.findElements(By.xpath('//select')).find { WebElement comboBox ->
			def options = comboBox.findElements(By.xpath('./option')).collect {
				it.getText()
			}

			if (options.containsAll(args.with)) return comboBox
		}
	}
}
