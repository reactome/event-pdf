FROM maven:3.8.6-openjdk-11-slim

# creating temporary working directory in order to store files
WORKDIR /app

#Install required packages and dependencies
# RUN apt-get update 
# RUN apt-get install -y mysql-client

#Copy the project files into the container
COPY . /app

#Running the commands that existed in the graph-importer Jenkinsfile
#Builds the jar file using Maven
RUN mvn clean package -P Reactome-Server

#Including relative path to the compiled java executable JAR file (created in Jenkinsfile)
CMD ["java", "-jar", "target/event-pdf-exec.jar", "--help"]
