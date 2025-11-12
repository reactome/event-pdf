// This Jenkinsfile is used by Jenkins to run the 'EventPDF' step of Reactome's release.
// It requires that the 'DiagramConverter' step has been run successfully before it can be run.

import org.reactome.release.jenkins.utilities.Utilities

// Shared library maintained at 'release-jenkins-utils' repository.
def utils = new Utilities()

pipeline{
	agent any

        environment {
		ECR_URL = 'public.ecr.aws/reactome/event-pdf'
	        CONT_NAME = 'event_pdf_container'
        }
	
	stages{
		// This stage verifies that the upstream project 'DiagramConverter' was executed successfully.
		stage('Check DiagramConverter build succeeded'){
			steps{
				script{
					utils.checkUpstreamBuildsSucceeded("File-Generation/job/DiagramConverter/")
				}
			}
		}
		stage('Pull event-pdf Docker image') {
			steps{
				script {
                			sh "docker pull ${ECR_URL}:latest"
					sh """
						if docker ps -a --format '{{.Names}}' | grep -Eq '${CONT_NAME}'; then
							docker rm -f ${CONT_NAME}
						fi
					"""
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
					try {
					  sh "sudo service tomcat9 stop"
					  withCredentials([usernamePassword(credentialsId: 'neo4jUsernamePassword', passwordVariable: 'pass', usernameVariable: 'user')]){
					     sh """
                                                 docker run \\
						     -v ${diagramFolderPath}:/data/diagram:ro \\
                                                     -v ${ehldFolderPath}:/data/ehld:ro \\
                                                     -v ${pwd()}/output:/app/output \\
						     --net=host \\
		                                     --name ${CONT_NAME} \\
						     ${ECR_URL}:latest \\
	                                             /bin/bash -c "java -Xmx${env.JAVA_MEM_MAX}m -jar target/event-pdf-exec.jar --user \$user --password \'$pass\' --diagram /data/diagram --ehld /data/ehld --summary /data/ehld/svgsummary.txt --output /app/output/TheReactomeBook --verbose"
						"""
					  }
					} finally {
                                            sh "sudo service tomcat9 start"
                                        }
				}
			}
		}
		// This step just lists the contents of the 'TheReactomeBook' folder between releases, allowing for comparison of file sizes.
		stage('Post: Compare TheReactomeBook contents between releases') {
		    steps{
		        script{
				def releaseVersion = utils.getReleaseVersion()
				def previousReleaseVersion = utils.getPreviousReleaseVersion()
				def reactomeBookFolder = "output/TheReactomeBook"

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
				dir("output"){
				    sh "tar -zcvf ${reactomeBookFolder}.pdf.tgz ${reactomeBookFolder}/" 
				    sh "cp ${reactomeBookFolder}.pdf.tgz ${env.ABS_DOWNLOAD_PATH}/${releaseVersion}/"
				}
		        }
		    }
		}
		// Archive everything on S3, and move the PDF archive to the download/XX folder.
		stage('Post: Archive Outputs'){
			steps{
				script{
					def releaseVersion = utils.getReleaseVersion()
					def dataFiles = ["output/TheReactomeBook.pdf.tgz"]
					def logFiles = []
					def foldersToDelete = ["output"]
					utils.cleanUpAndArchiveBuildFiles("event_pdf", dataFiles, logFiles, foldersToDelete)
				}
			}
		}
	}
}
