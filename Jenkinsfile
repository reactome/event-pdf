// This Jenkinsfile is used by Jenkins to run the EventPDF step of Reactome's release.
// It requires that the DiagramConverter step has been run successfully before it can be run.

import org.reactome.release.jenkins.utilities.Utilities

// Shared library maintained at 'release-jenkins-utils' repository.
def utils = new Utilities()
pipeline{
	agent any

	stages{
		// This stage checks that upstream project DiagramConverter was run successfully.
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
					sh "mvn clean package"
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
						sh "java -Xmx${env.JAVA_MEM_MAX}m -jar target/event-pdf-jar-with-dependencies.jar --user $user --password $pass --diagram ${diagramFolderPath} --ehld ${ehldFolderPath} --summary ${ehldFolderPath}/svgsummary.txt --output TheReactomeBook --verbose"
					}
				}
			}
		}
		stage('Post: Compare TheReactomeBook contents between releases') {
		    steps{
		        script{
		            def releaseVersion = utils.getReleaseVersion()
		            def previousReleaseVersion = utils.getPreviousReleaseVersion()
		            def reactomeBookFolder = "TheReactomeBook"
					
		            sh "mkdir -p ${previousReleaseVersion}"
		            sh "aws s3 --no-progress cp s3://reactome/private/releases/${previousReleaseVersion}/event_pdf/data/${reactomeBookFolder}.pdf.tgz ${previousReleaseVersion}/"
		            dir("${previousReleaseVersion}"){
		                sh "tar -xf ${reactomeBookFolder}.pdf.tgz"
		            }
					
		            echo("EventPDF folder contents for v${releaseVersion}:")
		            sh "ls -lrt ${reactomeBookFolder}/"
		            echo("EventPDF folder contents for v${previousReleaseVersion}:")
		            sh "ls -lrt ${previousReleaseVersion}/${reactomeBookFolder}/"
		            
		            sh "rm -r ${previousReleaseVersion}*"
		            
		        }
		    }
		}
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
		// Archive everything on S3, and move the PDF archive to the download/vXX folder.
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
