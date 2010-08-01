package webdriver.groovydsl

import java.util.Map

import groovy.xml.MarkupBuilder


class BasicOutputGenerator implements OutputGenerator {

	void generateOutput(Exception exceptionOccurrence, String failureImageFilename, Map executionResults) {
		StackTraceElement[] stackTrace = new RuntimeException().getStackTrace()
		StackTraceElement testElement = discoverTestClassName(stackTrace)
		def className = testElement.getClassName()
		def testName = testElement.getMethodName()
		
		def niceClassName = className //StringUtils.splitByCharacterTypeCamelCase(className).join(' ')
		def niceTestName = testName //StringUtils.splitByCharacterTypeCamelCase(testName).join(' ')
		
		def writer = new StringWriter()
		new MarkupBuilder(writer).html {
			head {
				title("$niceClassName - $niceTestName")
				style(type:'text/css', """
					tr.success {
						background-color: #CFC;
					}
					
					tr.failed {
						background-color: #FCC;
					}

					table {
						border: 1px solid black;
					}

					.screenshot {
						max-width: 100%;
					}
				""")
			}
			body {
				h1(niceClassName)
				h2(niceTestName)
				table {
					tr {
						th("Script")
						th("Screenshot")
						th("Duration (ms)")
					}
					
					executionResults.each { int line, ExecutionStepResult step ->
						
						def imageFilename = step.elementScreenshot
						
						if(step.when == "AFTER") {
							tr(class:'success') {
								td { pre(step.getSourceCode().trim()) }
								td {
									img(class:'screenshot', src:imageFilename)
								}
								td(step.duration)
							}
						} else {
							tr(class:'failed') {
								td { pre(step.getSourceCode().trim())
									pre(exceptionOccurrence.toString())
								}
								td {
									img(class:'screenshot', src:failureImageFilename)
								}
								td(step.duration)
							}
						}
					}
				}
			}
		}
		
		
		File outputDir = new File("target/webdriver")
		outputDir.mkdirs()
		File f = new File("target/webdriver/$className-$testName"+".html")
		f << writer.toString()
		
		if (exceptionOccurrence==null) {
			println "Success:         $className $testName"
		} else {
			println "*** FAILED ***   $className $testName"
			println "Please check output files at ${f.getCanonicalPath()}\n"
		}
		
		
		if (exceptionOccurrence!=null) {
			throw exceptionOccurrence
		}
	}
	
	/**
	 * Find the name of the test class.
	 * 
	 * @param stacktrace A stacktrace thrown somewhere inside the test class.
	 * @return the specific stack trace element corresponding to the test class.
	 */
	private StackTraceElement discoverTestClassName(StackTraceElement[] stacktrace) {
		stacktrace.find { 
			it.getClassName().endsWith("Test") && it.getMethodName().startsWith("test")
		}
	}
}
