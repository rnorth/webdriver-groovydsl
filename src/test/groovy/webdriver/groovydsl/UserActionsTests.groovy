package webdriver.groovydsl

import org.junit.Test
import groovy.time.TimeCategory
import org.junit.BeforeClass
import org.junit.AfterClass

/**
 * Created by IntelliJ IDEA.
 * User: richardnorth
 * Date: Jun 22, 2010
 * Time: 8:22:26 PM
 * To change this template use File | Settings | File Templates.
 */
class UserActionsTest {

    WebDriverDsl dsl = new WebDriverDsl()

    @Test
    void testBasicDsl() {
        dsl.run """
           navigate to:'http://www.wikipedia.org'
        """
    }

    @Test
    void testTyping() {
	    dsl.run """
           navigate to:'http://www.wikipedia.org'
           type 'hello world', into:textField(named:'search')
        """
    }

    @Test
    void testTypingAndClick() {
	    dsl.run """
           navigate to:'http://www.wikipedia.org'
           type 'hello world', into:textField(named:'search')
           click button(named:'go')
        """
    }

    @Test
    void testTypingAndClickAndPageContains() {
	    dsl.run """
           navigate to:'http://www.wikipedia.org'
           type 'hello world', into:textField(named:'search')
           page contains:button(named:'go')
           click button(named:'go')
           page contains:text('Hello World Program')
        """
    }

    @Test
    void testClickLink() {
	    dsl.run """
           navigate to:'http://www.google.com'

           page contains:link('Advanced Search')
           click link("About Google")              
           page contains:text('Our Company')
        """
    }

    @Test
    void testMarkedButton() {
	    dsl.run """
           navigate to:'http://www.google.com'

           page contains:button('Google Search')
         """
    }

    @Test
    void testAfterPause() {
        long start = new Date().getTime()
        
        dsl.run """
            after 5.seconds    
        """

        long end = new Date().getTime()
        assert (end - start) > 5000
        assert (end - start) < 10000
    }

	@Test
	void testComboBox() {
		dsl.run """
			navigate to:'http://www.wikipedia.org'
			page contains:combobox(with:['English','Nederlands'])

			select 'Nederlands', from:combobox(with:['English','Nederlands'])
			type 'hello world', into:textField(named:'search')
            click button(named:'go')
            page contains:text('Hello world-programma')

			navigate to:'http://www.wikipedia.org'
			select 'English', from:combobox(with:['English','Nederlands'])
		"""
	}

	@Test
	void testGetLineNumbers() {
		dsl.run """
			navigate to:'http://www.wikipedia.org'
			page contains:text('Wikipedia')
		"""

		def result = dsl.getExecutionResults()
		assert result.size() == 2

		assert result[1].verb == 'navigate'

		println result
	}
}
