def testType = args[0]
def lastTestName = args[1]

def testRunner = new TestRunner(testType: testType)
//println testRunner.findPollutingTest(lastTestName)
println testRunner.runTestLast(lastTestName)

class TestRunner {
	String testType

	String findPollutingTest(String test) {
		def testNames = getTestNamesExcluding(test)
		findPollutingTest(testNames, test)
	}

	String runTestLast(String test) {
		def testNames = getTestNamesExcluding(test)
		getOutPutFromRunningTests(testNames + [ test ])
	}

	List<String> getTestNamesExcluding(String testToExclude) {
		def testNames = getTestNames()
		testNames - testToExclude
	}

	def getTestNames() {
		File testsFolder = new File("test/$testType")
		getTestNamesFrom(testsFolder)
	}

	def getTestNamesFrom(File testsFolder) {
		def names = []
		testsFolder.eachFileRecurse {
			if(it.isFile()) {
				names << it.name - '.groovy'
			}
		}
		names
	}

	def findPollutingTest(List<String> tests, String pollutedTest) {
		def bisectedTests = returnFailingHalf(tests, pollutedTest)
		if(bisectedTests.size() > 1) {
			findPollutingTest(bisectedTests, pollutedTest)
		} else {
			bisectedTests
		}
	}

	def returnFailingHalf(List<String> tests, String pollutedTest) {
		int middleIndex = tests.size() / 2
		def half = tests.subList(0, middleIndex)
		if(doTestsFail(half + [ pollutedTest ])) {
			half
		} else {
			tests.subList(middleIndex, tests.size())
		}
	}

	boolean doTestsFail(List<String> tests) {
		getOutPutFromRunningTests(tests).contains('FAILED')
	}


	String getOutPutFromRunningTests(List<String> testNames) {
		def command = getGrailsTestCommand() + testNames.join(' ')
		println "Running: $command"
		def proc = command.execute()
		proc.in.text
	}	

	private String getGrailsTestCommand() {
		"grails test-app $testType:spock "
	}
}

