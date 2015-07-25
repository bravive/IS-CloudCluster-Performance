@Author: Ziyuan Song
Description:
	Automation include two monitor and a coordinator. The two monitors
	takes charge of launching an aggregator instance and lots of cluster
	nodes. They also monitor the status of these instances and update 
	their status on mysql. Once, there is any instance crashed or terminated
	accidentally, the monitor will create extra instances to replace the 
	old ones. On the other hand, the coordinator will send necessarily files
	to specific instances(aggregator or cluster node).

Build && Run
/**For pom.xml, use mvn exec:java run monitor*/

mvn clean install
mvn exec:java -Dexec.args="-e -DaccessKeyId=xxxx -DsecretAccesssKey=xxxx"

/**For pom.xml.1.0, it will package the project
 * into a single jar file. Use java -cp in target/
 * to run the project*/

mvn package
cd target/
java -cp ‘cloudMonitor-1.0.jar’ Automation -e -DaccessKeyId=xxxx -DsecretAccesssKey=xxxx