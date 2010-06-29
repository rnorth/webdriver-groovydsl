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

		if (args.rightOf) {
			// just use the first found 'rightOf'
			def origin = args.rightOf[0]
			def originLocation = getLocationOfElement(origin)

			listToSearch = listToSearch.findAll {
				def subjectLocation = getLocationOfElement(it)

				println "Checking that $subjectLocation is right of $originLocation"

				subjectLocation.y >= originLocation.y &&
						(subjectLocation.y + subjectLocation.height) <= (originLocation.y + originLocation.height) &&
						subjectLocation.x >= originLocation.x
			}
		}

		if (args.below) {
			// just use the first found
			def origin = args.below[0]
			def originLocation = getLocationOfElement(origin)

			listToSearch = listToSearch.findAll {
				def subjectLocation = getLocationOfElement(it)

				println "Checking that $subjectLocation is below $originLocation"

				return (subjectLocation.x >= originLocation.x &&
						(subjectLocation.x + subjectLocation.width) <= (originLocation.x + originLocation.width) &&
						subjectLocation.y <= originLocation.y)
			}
		}

		return listToSearch
	}

	WebElement[] textField(Map args) {
        println "textField named $args.named"
        return this.driver.findElements(By.name(args.named))
    }

    WebElement[] button(Map args) {
        println "button named $args.named"

        return this.driver.findElements(By.name(args.named)).findAll {
	        it.getAttribute('type') != 'hidden'
        }
    }

    WebElement[] button(String marked) {
        println "button marked $marked"

        return this.driver.findElements(By.xpath('//input')).findAll { WebElement it ->
             it.value.equalsIgnoreCase(marked)
        }
    }

    WebElement[] text(Map narrowTerms=[:], String searchTerm) {

	    // Inefficient - could search for elements likely to contain text first
	    //return findAll(this.driver, '//*[text()]', { WebElement it ->
		return findAll(this.driver, "//*[contains(text(),'$searchTerm')]", { WebElement it ->
		    if (it.text.toLowerCase().contains(searchTerm.toLowerCase())) return it
	    }, narrowingClosure, narrowTerms)

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

			//def parent = it.element?.getParentNode()
			//def parent = it.findElement(By.xpath('..'))
			def parent = getParentNode(it)
			parentNodesToRemove.addAll result.findAll {
					return it == parent 
			}
		}
		result.removeAll(parentNodesToRemove)

		// try and narrow
		result = narrowUsingClosure.call(result, narrowTerms)

		if (result.size() == 0) {
			throw new ElementLocationException("Could not find element")
		} else if (result.size() > 1 ) {


		}

		return result
	}

    WebElement[] link(String linkText) {
        println "link $linkText"

        return this.driver.findElements(By.linkText(linkText))
    }

	WebElement[] combobox(Map args) {
		println "combobox containing $args.with"

		this.driver.findElements(By.xpath('//select')).findAll { WebElement comboBox ->
			def options = comboBox.findElements(By.xpath('./option')).collect {
				it.getText()
			}

			if (options.containsAll(args.with)) return comboBox
		}
	}

	def getParentNode(WebElement element) {
		this.driver.executeScript("""
		   return arguments[0].parentNode;
		""",element)
	}

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

		//[x:element.location.x, y:element.location.y, width:element.size.width, height:element.size.height]
	}
}


