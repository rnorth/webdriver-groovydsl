package webdriver.groovydsl

import org.junit.Test
import groovy.time.TimeCategory
import org.junit.BeforeClass
import org.junit.AfterClass
import org.mortbay.jetty.webapp.WebAppContext
import org.openqa.selenium.remote.server.DriverServlet
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

    WebDriverDsl dsl = new WebDriverDsl()
	static Server server = new Server()

	@BeforeClass
	static void startSelServer() {
		WebAppContext context = new WebAppContext();
		context.setContextPath("");
		context.setWar("file:///Users/richardnorth/workspace/webdriver-harness/target/harness-1.0.0-SNAPSHOT.war");
		server.addHandler(context);

		//context.addServlet(DriverServlet.class, "/wd/*");

		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(3001);
		server.addConnector(connector);

		server.start();
		println "Server started"
	}

	@AfterClass
	static void stopSelServer() {
		server.stop()
	}

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
           page contains:text('"Hello World" program')
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
    }

	@Test
	void testComboBox() {
		dsl.run """
			navigate to:'http://www.wikipedia.org'
			page contains:combobox(with:['English','Nederlands'])

			select 'Nederlands', from:combobox(with:['English','Nederlands'])
			type 'hello world', into:textField(named:'search')
            click button(named:'go')
            page contains:text('Hello world in verschillende programmeertalen')

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

	@Test
	void testLocationNarrowingRightOf() {
		dsl.run """
		   navigate to:'http://www.w3schools.com/html/html_tables.asp'

		   page contains:text('13%', rightOf:text('Oranges'))
		   page contains:text('10%', rightOf:text('Other'))
		   page contains:text('row 1, cell 2', rightOf:text('row 1, cell 1'), below:text('Header 2'))
		"""

		try {
			dsl.run """
			   navigate to:'http://www.w3schools.com/html/html_tables.asp'

			   page contains:text('23%', rightOf:text('Oranges'))
			"""

			assert false, "Should not reach here - untrue rightOf lookup"
		} catch (ElementLocationException expectedException) {
			// swallow
		}
	}
}
