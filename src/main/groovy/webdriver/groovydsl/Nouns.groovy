package webdriver.groovydsl

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.WebDriver

/**
 * Nouns used as mixin for WebDriverDsl.
 * @author Richard North <rich.north@gmail.com>
 */
class Nouns {

	/**
	 * Closure used to narrow in on elements; typically used when finding
	 * any WebElement using the findAll method.
	 * TODO: Refactor common elements out.
	 */
	Closure narrowingClosure = { def listToSearch, Map args ->

		if (args.rightOf) {
			// just use the first found 'rightOf'
			def origin = args.rightOf[0]
			def originLocation = getLocationOfElement(origin)

			listToSearch = listToSearch.findAll {
				def subjectLocation = getLocationOfElement(it)

				//println "Checking that $subjectLocation is right of $originLocation"

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

				//println "Checking that $subjectLocation is below $originLocation"

				return (subjectLocation.x >= originLocation.x &&
						(subjectLocation.x + subjectLocation.width) <= (originLocation.x + originLocation.width) &&
						subjectLocation.y >= originLocation.y)
			}
		}

		return listToSearch
	}

	/**
	 * Find a text input (<INPUT> tag).
	 * @param args where args.named is the name of the input
	 */
	WebElement[] textField(Map args) {
        //println "textField named $args.named"
		
		if (args.labelled) {
			WebElement label = findAll(this.driver,"//label[contains(text(),'${args.labelled}')]", null, narrowingClosure, [:])[0]
			
			String idOfElement = label.getAttribute('for')
			return findAll(this.driver,"//input[@id='$idOfElement']", null, narrowingClosure, args)
			
		} else if (args.named) {
			return findAll(this.driver,"//input[@name='${args.named}']", null, narrowingClosure, args)
		}
		[]
    }

	/**
	 * Find a button (<INPUT> tag).
	 * @param args where args.named is the name of the button
	 */
    WebElement[] button(Map args) {
        //println "button named $args.named"

		return findAll(this.driver,"//input[@name='${args.named}']", {
	        it.getAttribute('type') != 'hidden'
        }, narrowingClosure, args)
    }

	/**
	 * Find a button (<INPUT> tag).
	 * @param marked The text on the button
	 * @param args either empty or a map where narrowing terms (e.g. rightOf, below) are specified
	 */
    WebElement[] button(Map narrowTerms=[:], String marked) {
        //println "button marked $marked"

		return findAll(this.driver,"//input", { WebElement it ->
             it.value.equalsIgnoreCase(marked)
        }, narrowingClosure, narrowTerms)
    }

	/**
	 * Find text on the page (in any single HTML element).
	 * @param marked The text to locate
	 * @param args either empty or a map where narrowing terms (e.g. rightOf, below) are specified
	 */
    WebElement[] text(Map narrowTerms=[:], String searchTerm) {
		
	    // Inefficient - could search for elements likely to contain text first
	    //return findAll(this.driver, '//*[text()]', { WebElement it ->
		return findAll(this.driver, """//*[contains(text(),"$searchTerm")]""", { WebElement it ->
		    if (it.text.toLowerCase().contains(searchTerm.toLowerCase())) return it
	    }, narrowingClosure, narrowTerms)

    }
	
	/**
	 * Find a link on the page
	 * @param marked The link text to locate
	 * TODO: replace findElements(By... with findAll call
	 */
    WebElement[] link(String linkText) {
        //println "link $linkText"

        return this.driver.findElements(By.linkText(linkText))
    }

	/**
	 * Find a combobox/selectable element
	 * @param args where args.with is a list of known selectable values
	 */
	WebElement[] combobox(Map args) {
		//println "combobox containing $args.with"

		return findAll(this.driver, "//select", { WebElement comboBox ->
			def options = comboBox.findElements(By.xpath('./option')).collect {
				it.getText()
			}

			if (options.containsAll(args.with)) return comboBox
		}, narrowingClosure, args)
	}
	
	/**
	 * Generic finder method.
	 * @param context context to search for WebElements; typically a WebDriver or another WebElement
	 * @param xpath XPath expression to search for
	 * @param findUsingClosure closure used to filter search results
	 * @param narrowUsingClosure closure used to further narrow down search results based on relationship with other elements
	 * @param narrowTerms map of arguments to be passed in to the narrowing closure (e.g. rightOf, below)
	 */
	WebElement[] findAll(def context, String xpath, Closure findUsingClosure, Closure narrowUsingClosure, Map narrowTerms) {

		def result = []
		
		if (findUsingClosure  == null) {
			findUsingClosure = { true }
		}

		if (context instanceof WebDriver) {
			result.addAll context.findElements(By.xpath(xpath)).findAll(findUsingClosure)
		}

		// try and narrow
		result = narrowUsingClosure.call(result, narrowTerms)


		return result
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
}


