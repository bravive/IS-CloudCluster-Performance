import java.util.ArrayList;
import java.util.List;

public class Automation {
	static public VmCluster aggCluster = new VmCluster();
	static public VmCluster nodCluster = new VmCluster();
	static Object lock = new Object();
	public static void main(String[] args) {
		Arguments allArguments = ParseArgument.parseArguments(args);
		
		
		/*****************Access EC2*****************/
		//Authorized by specified file.
//		String awsCredentialsPath = "/AwsCredentials.properties";
//		AccessEC2 EC2handler = new AccessEC2(awsCredentialsPath);
		//Authorized by input arguments.
		AccessEC2 EC2handler = new AccessEC2(allArguments.accessKeyId, allArguments.secretAccesssKey);
		/**************Common**************/
		String s3Bucket = allArguments.s3Bucket;
		/**************Aggregator Initial Variable**************/
		String aggSecurityGroup = allArguments.securityGroup;
		String aggAmiId = allArguments.aggAmiId; //"ami-0e0b1166";
		int aggMaxInstanceNum = allArguments.aggMaxInstanceNum;
		String aggZone = allArguments.aggZone;
		String aggInstanceType = allArguments.aggInstanceType;
		String aggProductDescribe = allArguments.productDescribe;
		String aggFileName = allArguments.aggFileName;
		/**************Nodes Initial Variable**************/
		String nodSecurityGroup = allArguments.securityGroup;
		String nodAmiId =allArguments.nodAmiId; //"ami-0e0b1166";
		int nodMaxInstanceNum = allArguments.nodMaxInstanceNum;
		String nodZone = allArguments.nodZone;
		String nodInstanceType = allArguments.nodInstanceType;
		String nodProductDescribe = allArguments.productDescribe;
		String nodFileName = allArguments.nodFileName;
		/**************Begin Two monitoring thread(aggregator && nodes)**************/
		Monitor mAgg = new Monitor();
		Monitor mNod = new Monitor();
		Coordinator coordinator = new Coordinator();
		mAgg
			.withAccessEC2(EC2handler)
			.withAMIId(aggAmiId)
			.withInstanceType(aggInstanceType)
			.withMaxInstanceNum(aggMaxInstanceNum)
			.withProductDescribe(aggProductDescribe)
			.withSecurityGroup(aggSecurityGroup)
			.withZone(aggZone)
			.withTagName("Monitor-Automation")
			.withTagValue("Aggreator")
			.withVMCluster(aggCluster);
		mNod
			.withAccessEC2(EC2handler)
			.withAMIId(nodAmiId)
			.withInstanceType(nodInstanceType)
			.withMaxInstanceNum(nodMaxInstanceNum)
			.withProductDescribe(nodProductDescribe)
			.withSecurityGroup(nodSecurityGroup)
			.withZone(nodZone)
			.withTagName("Monitor-Automation")
			.withTagValue("Slave Nodes")
			.withVMCluster(nodCluster);
		coordinator
			.withAggOutputPath(aggFileName)
			.withNodOutputPath(nodFileName)
			.withS3Bucket(s3Bucket);
		Thread aggMonitorThread = new Thread(mAgg, "Aggregator Monitor");
		Thread nodMonitorThread = new Thread(mNod, "Nodes Monitor");
		Thread coordinatorThread = new Thread(coordinator, "Coordinator");
		aggMonitorThread.start();
		nodMonitorThread.start();
		coordinatorThread.start();
	}
}
class Coordinator implements Runnable {
	/**
	 * resource/DNS.info		--> host:~/AggregatorDNS.info
	 * resource/NodesDNS.info	--> host:~/NodesDNS.info
	 * resource/MySQL.info		--> host:~/MySQL.info
	 * ../conf/is.pem			--> host:~/is.pem
	 * ../conf/s3cfg			--> host:~/.s3cfg
	 * */
	private String aggFileName = "";
	private String nodFileName = "";
	private String s3Bucket = "";
	private String mySqlIP = Utility.getLocalIp();
	public void run() {
		try {
			synchronized(Automation.lock) {
				while(true){
					Automation.lock.wait();
					if (Automation.aggCluster.isActive() && Automation.nodCluster.isActive() 
							&& (Automation.aggCluster.isUpdated() ||  Automation.nodCluster.isUpdated())) {
						//Write to File
						String absPathOfAggFile = Utility.writeListToFile(Automation.aggCluster.getDNSs(), this.aggFileName);
						String absPathOfNodFile = Utility.writeListToFile(Automation.nodCluster.getDNSs(), this.nodFileName);
						//Send via bash script (For now, there is just ONE aggergator)
						for (String host: Automation.aggCluster.getDNSs()) {
							boolean success = Utility.scpFileByBash(host, "~/", absPathOfNodFile);
							
							/**If Aggregator is updated, beside nodes DNSs, it will also need
							 * mysql ip(the master NOW!) and "conf/" materials(including s3cfg
							 * and is.pem) If Aggregator first SCP success, then send all other 
							 * configure files  as above.
							 **/
							if (success == true && Automation.aggCluster.isUpdated()) {
								String absPathOfMysqlIpFile = Utility.writeToFile(this.mySqlIP, "MySQL.info");
								String absPathOfS3BucketFile = Utility.writeToFile(this.s3Bucket, "S3Bucket.info");
								Utility.scpFileByBash(host, "~/", absPathOfAggFile);
								Utility.scpFileByBash(host, "~/", absPathOfMysqlIpFile);
								Utility.scpFileByBash(host, "~/", absPathOfS3BucketFile);
								Utility.scpFileByBash(host, "~/", "../conf/is.pem");
								Utility.scpFileByBash(host, "~/.s3cfg", "../conf/s3cfg");	
								/**After file sending, should run program on Aggregator remotely as the first time */
								Utility.remoteExecWithPara(host, "start_aggregator.sh", this.mySqlIP, this.s3Bucket);
							}
						}
						//After Send
						Automation.aggCluster.setNotUpdated();
						Automation.nodCluster.setNotUpdated();
					}
				}
			}
		} catch (Exception e) {
			Utility.logPrint(e.toString());
		}
	}
	public Coordinator withAggOutputPath(String aggOutputPath) {
		this.aggFileName = aggOutputPath;
		return this;
	}
	public Coordinator withNodOutputPath(String nodOutputPath) {
		this.nodFileName = nodOutputPath;
		return this;
	}
	public Coordinator withS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
		return this;
	}

}
//Monitor thread implements the real launching, tagging and monitoring process.
class Monitor implements Runnable {
	
	private AccessEC2 EC2handler = null;
	private String securityGroup = "";
	private String amiId = "";
	private int maxInstanceNum = 0;
	private String zone = "";
	private String instanceType = "";
	private String productDescribe = "Linux/UNIX";	//default
	private String tagName = "IS-ESE";
	private String tagValue = "Automation";
	private VmCluster vmCluster = null;

	public void run() {
		
		/**************Get initial price**************/
		String minSpotPrice = EC2handler.getHistoryPrice(this.instanceType, this.zone, this.productDescribe);
		float betSpotPrice = 2 * Float.parseFloat(minSpotPrice);
		/**************Begin to Control & Monitor**************/
		ArrayList<String> instanceIds = EC2handler.runSpotInstance(securityGroup, instanceType, amiId, maxInstanceNum, Float.toString(betSpotPrice), zone);
		
		while (true) {
			//spot and run instance
			while(true) {
				if (instanceIds.size() < maxInstanceNum) {
					minSpotPrice = EC2handler.getHistoryPrice(instanceType,zone, productDescribe);
					betSpotPrice = 2 * Float.parseFloat(minSpotPrice);
					ArrayList<String> newIds = EC2handler.runSpotInstance(securityGroup, instanceType, amiId, maxInstanceNum - instanceIds.size(), Float.toString(betSpotPrice),zone);
					instanceIds.addAll(newIds);
				} else if (instanceIds.size() == maxInstanceNum) {
					//repeat tag will not cause problems,
					if (instanceIds != null) {
						EC2handler.tagInstancesByIds(instanceIds, tagName, tagValue);
						for (String s : instanceIds) {
							Utility.logPrint("[Info]: Instance Id : <" + s + ">");
						}
					}
					break;
				}
			}
			//Only when there is any new instance, update DNS file(overwrite) by writing to file.
			//After getting DNS, all instances should be running. 
			ArrayList<String> DNSs = EC2handler.getDNSById(instanceIds);
			if (DNSs.size() == instanceIds.size()) {
				ArrayList<String> oldDNSs;
				synchronized(Automation.lock) {
					oldDNSs = new ArrayList<String>(this.vmCluster.getDNSs());
					this.vmCluster.updateDNSs(DNSs);
					this.vmCluster.setActive();
					this.vmCluster.setUpdated();
					/**Updata status in Mysql database*/
					Automation.lock.notify();	//Only allow one thread to to use this lock at the same time
				}
				
				/**Update MySQL database status*/
				MysqlConnection conn = new MysqlConnection();
				for (String DNS: oldDNSs) {
					if (!this.vmCluster.getDNSs().contains(DNS)) {
						conn.updateInstanceInfo(DNS, "Terminated");
					}
				}
				for (String DNS: this.vmCluster.getDNSs()) {
					conn.updateInstanceInfo(DNS, "Running");
				}
			}
			
			//Monitor instance
			int runningCount = 0;
			int terminatedCount = 0;
			int circle = 0;
			while (true) {
				ArrayList<String> temp = new ArrayList<String>();
				for (String instanceId : instanceIds) {
					String state = EC2handler.getStateById(instanceId);
					if (state == null) {
						Utility.logPrint("[Error]: Getting state failing");
						break;
					}
					if (state.equals("running")) {
						Utility.logPrint("<" + instanceId + ">: " +  "is running.");
						runningCount++;
						/**if all instances are running, all ids should be added into the a
						 * array list. There is an instance terminated, the size of the array
						 * will be less than the maximum of the launching number, which will
						 * be used when running spot instance.
						 */
						temp.add(instanceId);
					} else if (state.equals("pending")){
						Utility.logPrint("<" + instanceId + ">: " +  "is pending.");
					} else if (state.equals("terminated")) {
						Utility.logPrint("<" + instanceId + ">: " +  "is terminated.");
						terminatedCount++;
					} else if (state.equals("shutting-down")) {
						Utility.logPrint("<" + instanceId + ">: " +  "is shutting-down.");
					}	
				}
				//Must check if there are instance in neither running nor terminated. 
				if (runningCount < maxInstanceNum && (runningCount + terminatedCount) == maxInstanceNum && circle == 0) {
					circle++;
				} else if (runningCount < maxInstanceNum && (runningCount + terminatedCount) == maxInstanceNum && circle != 0) {
					// double check if all instance state are stable
					instanceIds = temp;
					break;
				}
				runningCount = 0;
				terminatedCount = 0;
				Utility.timerS(60);
			}
			/**when the second while break, that means there is at least one instance is terminated.
			 * Only such situation is checked twice by circle variable, the program will go out to 
			 * the outer while loop, and then begin a new spot instance progress.
			 */
			synchronized(Automation.lock) {
				this.vmCluster.setNotActive();
			}
		}
	}
	public Monitor withAccessEC2(AccessEC2 EC2handler) {
		this.EC2handler = EC2handler;
		return this;
	}
	public Monitor withSecurityGroup(String securityGroup) {
		this.securityGroup = securityGroup;
		return this;
	}
	public Monitor withAMIId(String amiId) {
		this.amiId = amiId;
		return this;
	}
	public Monitor withMaxInstanceNum(int maxInstanceNum) {
		this.maxInstanceNum = maxInstanceNum;
		return this;
	}
	public Monitor withZone(String zone) {
		this.zone = zone;
		return this;
	}
	public Monitor withInstanceType(String instanceType) {
		this.instanceType = instanceType;
		return this;
	}
	public Monitor withProductDescribe(String productDescribe) {
		this.productDescribe = productDescribe;
		return this;
	}
	public Monitor withTagName(String tagName) {
		this.tagName = tagName;
		return this;
	}
	public Monitor withTagValue(String tagValue) {
		this.tagValue = tagValue;
		return this;
	}
	public Monitor withVMCluster(VmCluster vmCluster) {
		this.vmCluster = vmCluster;
		return this;
	}
}
class VmCluster {
	private ArrayList<String> DNSs = new ArrayList<String>();
	private boolean isActive = false;
	private boolean isUpdated = false;
	public ArrayList<String> getDNSs() {
		return this.DNSs;
	}
	public boolean isActive() {
		return this.isActive;
	}
	public boolean isUpdated() {
		return this.isUpdated;
	}
	public void updateDNSs(List<String> list) {
		DNSs.clear();
		DNSs.addAll(list);
	}
	public void setActive() {
		this.isActive = true;
	}
	public void setNotActive() {
		this.isActive = false;
	}
	public void setUpdated() {
		this.isUpdated = true;
	}
	public void setNotUpdated() {
		this.isUpdated = false;
	}
}
