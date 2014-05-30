println 'running tests in order'
def testsFile = new File(args[0])
def pollutedTest = args[1]
if(!testsFile) {
	println 'no gosh darn test file'
	quit()
}
def tests = getTestsFrom(testsFile)
//println getOutPutFromRunningTests(tests + [ pollutedTest ])
findAndPrintPollutingTest(tests, pollutedTest)


def findAndPrintPollutingTest(List<String> tests, String pollutedTest) {
	def pollutingTests = findPollutingTest(tests, pollutedTest)
	println "This is the polluting test"
	println pollutingTests	
}

def quit() {
	System.exit(0)
}

List<String> getTestsFrom(File testsFile) {
	def tests = []
	testsFile.eachLine { line ->
		tests << getTestNameFrom(line)
	}
	tests
}

//example format: Running test com.package.SomeKindOfSpec...PASSED
String getTestNameFrom(String line) {
	line = line - 'Spec...PASSED'
	line.substring(line.lastIndexOf('.')+1, line.size())
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

String getOutPutFromRunningTests(List<String> tests) {
	def command = "$grailsTestCommand ${tests.join(' ')}"
	println "Running: $command"
	def proc = command.execute()
	proc.in.text
}

String getGrailsTestCommand() {
	"grails test-app unit: "
}
