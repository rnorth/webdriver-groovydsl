package webdriver.groovydsl;

import java.util.Map;
import webdriver.groovydsl.WebDriverDsl;

public interface OutputGenerator {

	void generateOutput(Exception exceptionOccurrence, String failureImageFilename, Map executionResults);
}
