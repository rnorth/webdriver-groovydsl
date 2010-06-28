package webdriver.groovydsl

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.WebDriver

/**
 * Created by IntelliJ IDEA.
 * User: richardnorth
 * Date: Jun 26, 2010
 * Time: 7:31:30 AM
 * To change this template use File | Settings | File Templates.
 */
class Nouns {
	Closure narrowingClosure = { def listToSearch, Map args ->
		return [listToSearch[0]]
	}

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

    WebElement text(Map narrowTerms=[:], String searchTerm) {

	    // Inefficient - could search for elements likely to contain text first
	    return findOne(this.driver, '//*', { WebElement it ->
		    if (it.text.toLowerCase().contains(searchTerm.toLowerCase())) return it
	    }, narrowingClosure, narrowTerms)
	    /*println "text $searchTerm"


        return this.driver.findElements(By.xpath('/*//*')).find { WebElement it ->
            if (it.text.toLowerCase().contains(searchTerm.toLowerCase())) return it
        }*/
    }

	WebElement[] findAll(def context, String xpath, Closure findUsingClosure, Closure narrowUsingClosure, Map narrowTerms) {

		def result = []

		if (context instanceof WebDriver) {
			result.addAll context.findElements(By.xpath(xpath)).findAll(findUsingClosure)
		}

		// The list initially may contain the full depth of each branch to hits
		//  so narrow by removing parents
		def parentNodesToRemove = []
		result.collect {

			def parent = it.element?.getParentNode()
			parentNodesToRemove.addAll result.findAll { it.element == parent }
		}
		result.removeAll(parentNodesToRemove)

		if (result.size() == 0) {
			throw new ElementLocationException("Could not find element")
		} else if (result.size() > 1 ) {

			// try and narrow
			result = narrowUsingClosure.call(result, narrowTerms)
		}

		return result
	}

	WebElement findOne(def context, String xpath, Closure findUsingClosure, Closure narrowUsingClosure, Map narrowTerms) {

		def result = findAll(context, xpath, findUsingClosure, narrowUsingClosure, narrowTerms)
		if (result.size() > 1) {
			throw new ElementLocationException("Ambiguous results")
		}
		result[0]
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
