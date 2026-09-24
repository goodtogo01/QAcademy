// qAcademy CI/CD pipeline
//
// What this does, stage by stage:
//   1. Checks out the repo.
//   2. Cleans up any leftover backend process from a previous (aborted/crashed) build,
//      so this run never trips the "Database may be already in use" H2 lock error.
//   3. Compiles the app.
//   4. Runs the chosen TestNG suite. The suite itself starts and stops the Spring Boot
//      backend automatically (BackendLifecycleManager, wired into BaseTest's
//      @BeforeSuite/@AfterSuite) - Jenkins never needs to run `spring-boot:run` itself.
//   5. Publishes test results/reports and archives screenshots + backend console log,
//      always, even on failure - that's the whole point of running tests in CI.
//
// Required Jenkins setup (see the accompanying setup guide for the full walkthrough):
//   - Global Tool Configuration: a JDK named "JDK17" and a Maven named "Maven3".
//   - Plugins: Pipeline, Git, HTML Publisher, JUnit (bundled), TestNG Results (optional
//     but recommended).
//   - A Pipeline job (or Multibranch Pipeline) pointed at this repo with
//     "Script Path" = Jenkinsfile.
//
// Headless Chrome: DriverFactory/ConfigManager already auto-detect Jenkins via the
// JENKINS_URL environment variable that Jenkins itself sets on every build, and switch
// Selenium to --headless=new automatically - the HEADLESS parameter below only needs to
// be touched if you want to override that default for a specific build (e.g. force
// "false" on a self-hosted agent with a real display, to watch a UI run live). The
// Jenkins agent still needs a real Chrome browser installed (Selenium Manager handles
// the matching chromedriver automatically as of Selenium 4.6+).

pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    parameters {
        choice(
            name: 'TEST_SUITE',
            choices: ['regression', 'smoke', 'api', 'ui', 'e2e'],
            description: 'Which TestNG suite to run. "regression" = ui+api+e2e (full run).'
        )
        choice(
            name: 'HEADLESS',
            choices: ['auto', 'true', 'false'],
            description: 'Chrome window mode for UI tests. "auto" (default) already goes headless on this build agent via ConfigManager\'s own JENKINS_URL detection - override to "true"/"false" only to force one way regardless of environment (e.g. "false" on an agent with a real display, to watch a UI run live).'
        )
    }

    options {
        timestamps()
        // Two API/UI/E2E suites can never share port 8085 and the H2 file safely -
        // refuse to start a second build of this job while one is already running,
        // rather than letting them fight over the backend.
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        SUITE_FILE = "src/test/resources/testing/testng-${params.TEST_SUITE}.xml"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Tool versions') {
            steps {
                sh '''
                    echo "== java =="
                    java -version
                    echo "== maven =="
                    mvn -version
                '''
            }
        }

        stage('Pre-build cleanup') {
            steps {
                // Guards against exactly the failure this project hit manually once
                // already: an orphaned `mvn spring-boot:run` from a previous build (e.g.
                // one that was aborted mid-suite, so its JVM shutdown hook never ran)
                // still holding port 8085 and data/qacademy.mv.db. BackendLifecycleManager
                // only ever stops a backend IT started, so a leftover process from a
                // different JVM/build is invisible to it - this stage is what clears that.
                sh '''
                    set +e
                    PID=$(lsof -ti tcp:8085)
                    if [ -n "$PID" ]; then
                        echo "Port 8085 already in use by PID(s) $PID from a previous run - killing before this build starts."
                        kill -9 $PID
                        sleep 2
                    else
                        echo "Port 8085 is free."
                    fi
                    exit 0
                '''
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn -B -ntp clean compile'
            }
        }

        stage('Test') {
            steps {
                // BackendLifecycleManager starts the app for this suite and tears it
                // down again when the suite finishes - see its Javadoc for why. This one
                // Maven call is the entire "deploy + test" step for CI purposes.
                //
                // -Dheadless=${params.HEADLESS} is passed through explicitly (rather than
                // left to ConfigManager's own JENKINS_URL auto-detection) so the HEADLESS
                // build parameter above can actually override it per build.
                sh "mvn -B -ntp test -DsuiteXmlFile=${SUITE_FILE} -Dheadless=${params.HEADLESS}"
            }
        }
    }

    post {
        always {
            // Standard JUnit-format XML that Surefire writes even for a TestNG run -
            // gives Jenkins its pass/fail trend graph regardless of which plugins are
            // installed.
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true

            // Richer HTML reports, published as tabs on the build page. Requires the
            // HTML Publisher plugin - see the setup guide.
            publishHTML(target: [
                reportName : 'TestNG Report',
                reportDir  : 'test-output',
                reportFiles: 'index.html',
                keepAll    : true,
                alwaysLinkToLastBuild: true,
                allowMissing: true
            ])
            publishHTML(target: [
                reportName : 'Extent Report',
                reportDir  : 'test-output',
                reportFiles: 'ExtentReport.html',
                keepAll    : true,
                alwaysLinkToLastBuild: true,
                allowMissing: true
            ])

            // Failure screenshots (UI suite) and the backend's own console output
            // (invaluable when a suite fails because the app itself never came up).
            archiveArtifacts artifacts: 'test-output/screenshot/**, target/automation-logs/**',
                              allowEmptyArchive: true

            // Safety net, mirroring the pre-build cleanup stage: if this build's own
            // suite was aborted (Jenkins timeout/manual abort) before
            // BackendLifecycleManager's @AfterSuite or shutdown hook could run, don't
            // leave the backend process behind for the *next* build to trip over.
            sh '''
                set +e
                PID=$(lsof -ti tcp:8085)
                if [ -n "$PID" ]; then
                    echo "Cleaning up backend process $PID left running after this build."
                    kill -9 $PID
                fi
                exit 0
            '''
        }
    }
}
