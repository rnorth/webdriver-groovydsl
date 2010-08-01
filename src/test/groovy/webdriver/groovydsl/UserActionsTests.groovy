package webdriver.groovydsl

import org.junit.Test
import org.junit.BeforeClass
import org.junit.AfterClass
import org.mortbay.jetty.webapp.WebAppContext
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.Server

/**
 * Created by IntelliJ IDEA.
 * User: richardnorth
 * Date: Jun 22, 2010
 * Time: 8:22:26 PM
 * To change this template use File | Settings | File Templates.
 */
class UserActionsTest {

	static Server server = new Server()

	@BeforeClass
	static void startTestHarnessServer() {
		WebAppContext context = new WebAppContext();
		context.setContextPath("");
		context.setWar(UserActionsTest.class.getResource("./wars/harness-1.0.0-SNAPSHOT.war").toString());
		server.addHandler(context);

		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(3001);
		server.addConnector(connector);

		server.start();
		println "Test Harness Server started"
	}

	@AfterClass
	static void stopTestHarnessServer() {
		server.stop()
	}

    @Test
    void testBasicDsl() {
        WebDriverDsl.run """
           navigate to:'http://localhost:3001/app/welcome/view'
        """
    }

    @Test
    void testTyping() {
	    WebDriverDsl.run """
           navigate to:'http://localhost:3001/app/welcome/view'
           type 'hello world', into:textField(named:'firstName')
        """
    }

    @Test
    void testTypingAndClick() {
	    WebDriverDsl.run """
           navigate to:'http://localhost:3001/app/welcome/view'
           type 'hello world', into:textField(named:'firstName')
           click button('Save Changes')
        """
    }

    @Test
    void testTypingAndClickAndPageContains() {
	    WebDriverDsl.run """
           navigate to:'http://localhost:3001/app/welcome/view'
           type 'hello world', into:textField(named:'firstName')
           click button('Save Changes')
           page contains:text('firstName=hello world')
        """
    }

    @Test
    void testClickLink() {
	    WebDriverDsl.run """
           navigate to:'http://localhost:3001/app/welcome/view'

           page contains:link('Click me!')
           click link("Click me!")              
           page contains:text('Link clicked')
        """
    }

    @Test
    void testAfterPause() {
        long start = new Date().getTime()
        
        WebDriverDsl.run """
            after 5.seconds    
        """

        long end = new Date().getTime()
        assert (end - start) > 5000
    }

	@Test
	void testComboBox() {
		WebDriverDsl.run """
			navigate to:'http://localhost:3001/app/welcome/view'
			page contains:combobox(with:['Spring MVC','Struts'])

			select 'Struts', from:combobox(with:['Spring MVC','Struts'], rightOf:text('Skills:'))
			type 'hello world', into:textField(named:'firstName')
            click button('Save Changes')
            page contains:text('firstName=hello world')
            page contains:text('skill=Struts')
		"""
	}

	@Test
	void testGetLineNumbers() {
		def result = WebDriverDsl.run("""
			navigate to:'http://localhost:3001/app/welcome/view'
			page contains:text('Test harness')
		""")
		assert result.size() == 2

		assert result[1].verb == 'navigate'

		println result
	}

	@Test
	void testLocationNarrowingRightOf() {
		WebDriverDsl.run """
		   navigate to:'http://localhost:3001/app/welcome/view'

		   page contains:text('200', rightOf:text('Apples'))
		   page contains:text('60', rightOf:text('Oranges'))
		   page contains:text('48', rightOf:text('Oranges'), below:text('2010'))
		"""

		try {
			WebDriverDsl.run """
			   navigate to:'http://localhost:3001/app/welcome/view'

			   page contains:text('48', rightOf:text('Apples'))
			"""

			assert false, "Should not reach here - false rightOf lookup"
		} catch (ElementLocationException expectedException) {
			// swallow
		}
	}
}
