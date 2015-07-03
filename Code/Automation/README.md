/**For pom.xml, use mvn exec:java run monitor*/

mvn clean install
mvn exec:java -Dexec.args="-e -DaccessKeyId=xxxx -DsecretAccesssKey=xxxx”





/**For pom.xml.old, it will package the project
 * into a single jar file. Use java -cp in target/
 * to run the project*/

mvn package
cd target/
java -cp ‘cloudMonitor-1.0.jar’ Automation -e -DaccessKeyId=xxxx -DsecretAccesssKey=xxxx