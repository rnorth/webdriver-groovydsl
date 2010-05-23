package webdriver;

import org.junit.Test;


public class UserActionsTests {

	@Test
	public void testUserCanNavigate() {
		SlimWebDriver driver = new SlimWebDriver();
		
		driver.user("navigates to:'http://www.bbc.co.uk'");
		driver.user("types 'hello world', into:'blq-search'");
		driver.user("clicks on:'blq-search-btn'");
	}
}
