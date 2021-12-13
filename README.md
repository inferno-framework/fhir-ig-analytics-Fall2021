# FHIR-IG-Analytics_Group-H

### How to build the application on server
- This is a springboot application which internally uses Jersey Jax-RS features.
- Make sure the build system (your device) is installed with java 8+ version. Please check this by typing (java -version) on the CLI.
- Make sure the build system (your device) is installed with gradle version 7.2. Please check this by typing (gradle -v) on the CLI.
- Checkout the project into a directory. Go to inside the analytics directory and run "gradle build".
- This command will take few seconds to compile the code. After successful compilation, a jar file (./build/libs/analytics-0.0.1.jar)
will be generated. That jar file includes all the dependencies to run the program.

### Setup eclipse
- Check out the project into a directory.
- Download java eclipse 
(Eclipse IDE for Java Developers (includes Incubating components)
	Version: 2021-03 (4.19.0)
	Build id: 20210312-0638
	OS: Linux, v.5.11.0-34-generic, x86_64 / gtk 3.24.20, WebKit 2.32.3
	Java version: 15.0.2
- Open eclipse
- File -> Open Project From File System -> Browse to the checkout directory and then go inside analysis -> Open -> Finish
- Add gradle nature in the project (Right Click on project -> Configure -> Enable Gradle nature)
- Open the resource file application.properties file and change port by updating server.port
- Now you should be able to run the AnalyticsApplication.java file by doing right click -> Run as -> Java Application

### Setup IntelliJ
- Check out the project into a directory.
- Download JetBrains IntelliJ IDE
- Open IntelliJ
- File --> New Project from Existing Sources --> Browse to the repository directory.  Ensure Java version is 8-16, as 17 and later is not supported.
- Gradle plugin comes with IntelliJ and will already be installed.
- Right click AnalyticsApplication.java and Run... as Java Application.

### How to run the application on server
- Checkout go inside analytics dir
- Make sure WGET and TAR binaries/packages are installed.
- Change the application server port other than 8081, update application.properties file server.port variable
- Run command gradle build
	gradle build
- Run the application server
	java -jar ./build/libs/analytics-0.0.1.jar

Now, go to the browser and test getTypes and compare call for two IGs are working
http://localhost:<port>/srvc/fhirAnalytics/getTypes?ig1=<IG1-Download-package-URL>&ig2=<IG2-Download-package-URL>
http://localhost:<port>/srvc/fhirAnalytics/compare?ig1=<IG1-Download-package-URL>&ig2=<IG2-Download-package-URL>

### How to run the UI
	
Read the readme file under fhir-analytics-ui directory
	
### Containerization of the Application
- Checkout the code
- Install docker

### Create Image
Please use the docker files to create docker images after installing docker on an Ubuntu 20.4

### Deploy docker images
- On the cloud backend SSH to the terminal
- Checkout the code
- Run sh start_analytics.sh
	This file would download images from public repositories and deploy the containers.
- The server container would run on 8081 and UI would run on 4200.
	
### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.5.6/gradle-plugin/reference/html/)
* [Jersey](https://docs.spring.io/spring-boot/docs/2.5.6/reference/htmlsingle/#boot-features-jersey)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
* [Install Gradle 7.2](https://gradle.org/install)
