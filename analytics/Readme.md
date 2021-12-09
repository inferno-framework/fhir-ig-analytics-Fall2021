# FHIR-IG-Analytics_Group-H

### How to build the application on server
- This is a springboot application which internally uses Jersey Jax-RS features.
- Make sure the build system (your device) is installed with java 8+ version. Please check this by typing (java -version) on the CLI.
- Make sure the build system (your device) is installed with gradle version 7.2. Please check this by typing (gradle -v) on the CLI.
- Checkout the project into a directory. Go to inside the analytics directory and run "gradle build".
- This command will take few seconds to compile the code. After successful compilation, a jar file (./build/libs/analytics-0.0.1.jar)
will be generated. That jar file includes all the dependencies to run the program.

### Setup eclipse
- Checkout the project into a directory.
- Download java eclipse 
(Eclipse IDE for Java Developers (includes Incubating components)
	Version: 2021-03 (4.19.0)
	Build id: 20210312-0638
	OS: Linux, v.5.11.0-34-generic, x86_64 / gtk 3.24.20, WebKit 2.32.3
	Java version: 15.0.2
- Open eclipse
- File -> Open Project From File System -> Browse to the checkout directory and then go inside analysis -> Open -> Finish
- Add gradle nature in the project (Right Click on project -> Configure -> Enable Gradle nature)
- Now you should be able to run the AnalyticsApplication.java file by doing right click -> Run as -> Java Application

### How to run the application on server
java -jar ./build/libs/analytics-0.0.1.jar <package_1> <package_2>

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.5.6/gradle-plugin/reference/html/)
* [Jersey](https://docs.spring.io/spring-boot/docs/2.5.6/reference/htmlsingle/#boot-features-jersey)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
* [Install Gradle 7.2](https://gradle.org/install)
