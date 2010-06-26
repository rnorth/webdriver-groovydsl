package webdriver.groovydsl

import org.junit.Test

/**
 * Created by IntelliJ IDEA.
 * User: richardnorth
 * Date: Jun 22, 2010
 * Time: 8:22:26 PM
 * To change this template use File | Settings | File Templates.
 */
class UserActionsTests {

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
}
