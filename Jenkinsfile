// This Jenkinsfile is used by Jenkins to run the 'EventPDF' step of Reactome's release.
// It requires that the 'DiagramConverter' step has been run successfully before it can be run.

import org.reactome.release.jenkins.utilities.Utilities

// Shared library maintained at 'release-jenkins-utils' repository.
def utils = new Utilities()

pipeline{
	agent any

	stages{
		// This stage checks that upstream project 'DiagramConverter' was run successfully.
		stage('Check DiagramConverter build succeeded'){
			steps{
				script{
					utils.checkUpstreamBuildsSucceeded("File-Generation/job/DiagramConverter/")
				}
			}
		}
		// This stage builds the jar file using maven.
		stage('Setup: Build jar file'){
			steps{
				script{
					sh "mvn clean package -P Reactome-Server"
				}
			}
		}
		// Execute the jar file, producing a folder (TheReactomeBook) of Reactome PDFs.
		stage('Main: Run EventPDF'){
			steps{
				script{
					def releaseVersion = utils.getReleaseVersion()
					def diagramFolderPath = "${env.ABS_DOWNLOAD_PATH}/${releaseVersion}/diagram/"
					def ehldFolderPath = "${env.ABS_DOWNLOAD_PATH}/${releaseVersion}/ehld/"

					withCredentials([usernamePassword(credentialsId: 'neo4jUsernamePassword', passwordVariable: 'pass', usernameVariable: 'user')]){
						sh "java -Xmx${env.JAVA_MEM_MAX}m -jar target/event-pdf-exec.jar --user $user --password $pass --diagram ${diagramFolderPath} --ehld ${ehldFolderPath} --summary ${ehldFolderPath}/svgsummary.txt --output TheReactomeBook --verbose"
					}
				}
			}
		}
		// Execute the verifier jar file checking for the existence and proper file sizes of the TheReactomeBook output
		stage('Post: Verify EventPDF ran correctly') {
			steps {
				script {
					def releaseVersion = utils.getReleaseVersion()
					def outputDirectory = "TheReactomeBook/"
					def dropTolerancePercentage = 2

					sh "java -jar target/event-pdf-verifier.jar --releaseNumber ${releaseVersion} --output ${outputDirectory} --sizeDropTolerance ${dropTolerancePercentage}"
				}
			}
		}
		// Creates a list of files and their sizes to use for comparison baseline during next release
		stage('Post: Create files and sizes list to upload for next release\'s verifier') {
			steps {
				script {
					def fileSizeList = "files_and_sizes.txt"
					def releaseVersion = utils.getReleaseVersion()

					sh "find TheReactomeBook/ -type f -printf \"%s\t%P\n\" > ${fileSizeList}"
					sh "aws s3 --no-progress cp ${fileSizeList} s3://reactome/private/releases/${releaseVersion}/event_pdf/data/"
					sh "rm ${fileSizeList}"
				}
			}
		}
		// This step just lists the contents of the 'TheReactomeBook' folder between releases, allowing for comparison of file sizes.
		stage('Post: Compare TheReactomeBook contents between releases') {
			steps{
				script{
					def releaseVersion = utils.getReleaseVersion()
					def previousReleaseVersion = utils.getPreviousReleaseVersion()
					def reactomeBookFolder = "TheReactomeBook"

					sh "mkdir -p ${previousReleaseVersion}"
					// Download previous 'TheReactomeBook' archive from S3.
					sh "aws s3 --no-progress cp s3://reactome/private/releases/${previousReleaseVersion}/event_pdf/data/${reactomeBookFolder}.pdf.tgz ${previousReleaseVersion}/"
					dir("${previousReleaseVersion}"){
						sh "tar -xf ${reactomeBookFolder}.pdf.tgz"
					}
					// Output folder contents of the current and previous 'TheReactomeBook' folders.
					echo("EventPDF folder contents for v${releaseVersion}:")
					sh "ls -lrt ${reactomeBookFolder}/"
					echo("EventPDF folder contents for v${previousReleaseVersion}:")
					sh "ls -lrt ${previousReleaseVersion}/${reactomeBookFolder}/"

					sh "rm -r ${previousReleaseVersion}*"
				}
			}
		}
		// Create tar archive of the 'TheReactomeBook' folder that was produced by this step, copying it over to the downloads folder.
		stage('Post: Generate ReactomeBook archive and move to downloads folder') {
			steps{
				script{
					def releaseVersion = utils.getReleaseVersion()
					def reactomeBookFolder = "TheReactomeBook"
					sh "tar -zcvf ${reactomeBookFolder}.pdf.tgz ${reactomeBookFolder}/"
					sh "cp ${reactomeBookFolder}.pdf.tgz ${env.ABS_DOWNLOAD_PATH}/${releaseVersion}/"
				}
			}
		}
		// Archive everything on S3, and move the PDF archive to the download/XX folder.
		stage('Post: Archive Outputs'){
			steps{
				script{
					def releaseVersion = utils.getReleaseVersion()
					def dataFiles = ["TheReactomeBook.pdf.tgz"]
					def logFiles = []
					def foldersToDelete = ["TheReactomeBook/"]
					utils.cleanUpAndArchiveBuildFiles("event_pdf", dataFiles, logFiles, foldersToDelete)
				}
			}
		}
	}
}
