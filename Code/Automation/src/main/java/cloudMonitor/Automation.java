import java.util.ArrayList;

public class Automation {
	public static void main(String[] args) {
		Arguments allArguments = ParseArgument.parseArguments(args);
		//Utility.logPrint(allArguments.toString());
		
		/*****************Access EC2*****************/
		//Authorized by specified file.
//		String awsCredentialsPath = "/AwsCredentials.properties";
//		AccessEC2 EC2handler = new AccessEC2(awsCredentialsPath);
		//Authorized by input arguments.
		AccessEC2 EC2handler = new AccessEC2(allArguments.accessKeyId, allArguments.secretAccesssKey);
		/**************Aggregator Initial Variable**************/
		String aggSecurityGroup = allArguments.securityGroup;
		String aggAmiId = allArguments.aggAmiId; //"ami-0e0b1166";
		int aggMaxInstanceNum = allArguments.aggMaxInstanceNum;
		String aggZone = allArguments.aggZone;
		String aggInstanceType = allArguments.aggInstanceType;
		String aggProductDescribe = allArguments.productDescribe;
		String aggOutputPath = allArguments.aggOutputPath;
		/**************Nodes Initial Variable**************/
		String nodSecurityGroup = allArguments.securityGroup;
		String nodAmiId =allArguments.nodAmiId; //"ami-0e0b1166";
		int nodMaxInstanceNum = allArguments.nodMaxInstanceNum;
		String nodZone = allArguments.nodZone;
		String nodInstanceType = allArguments.nodInstanceType;
		String nodProductDescribe = allArguments.productDescribe;
		String nodOutputPath = allArguments.nodOutputPath;
		/**************Begin Two monitoring thread(aggregator && nodes)**************/
		Monitor mAgg = new Monitor();
		Monitor mNod = new Monitor();
		mAgg.withAccessEC2(EC2handler)
			.withAMIId(aggAmiId)
			.withInstanceType(aggInstanceType)
			.withMaxInstanceNum(aggMaxInstanceNum)
			.withProductDescribe(aggProductDescribe)
			.withSecurityGroup(aggSecurityGroup)
			.withZone(aggZone)
			.withTagName("Monitor-Automation")
			.withTagValue("Aggreator")
			.withOutputPath(aggOutputPath);
		mNod.withAccessEC2(EC2handler)
			.withAMIId(nodAmiId)
			.withInstanceType(nodInstanceType)
			.withMaxInstanceNum(nodMaxInstanceNum)
			.withProductDescribe(nodProductDescribe)
			.withSecurityGroup(nodSecurityGroup)
			.withZone(nodZone)
			.withTagName("Monitor-Automation")
			.withTagValue("Slave Nodes")
			.withOutputPath(nodOutputPath);
		Thread aggregatorMonitor = new Thread(mAgg, "Aggregator Monitor");
		Thread nodesMonitor = new Thread(mNod, "Nodes Monitor");
		aggregatorMonitor.start();
		nodesMonitor.start();
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
	private String outputPath = "";
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
	public Monitor withOutputPath(String outputPath) {
		this.outputPath = outputPath;
		return this;
	}
	public void run() {
		if (this.EC2handler == null) {
			Utility.logPrint("[Error]: Must specify an AccessEC2");
			return;
		}
		if (this.securityGroup.equals("")) {
			Utility.logPrint("[Error]: Must specify a securityGroup");
			return;
		}
		if (this.amiId.equals("")) {
			Utility.logPrint("[Error]: Must specify an amiId");
			return;
		}
		if (this.maxInstanceNum == 0) {
			Utility.logPrint("[Error]: Must set maxInstanceNum greater than 0");
			return;
		}
		if (this.zone.equals("")) {
			Utility.logPrint("[Error]: Must specify a zone");
			return;
		}
		if (this.instanceType.equals("")) {
			Utility.logPrint("[Error]: Must specify an instanceType");
			return;
		}
		if (this.outputPath.equals("")) {
			Utility.logPrint("[Error]: Must specify an outputPath for information");
			return;
		}
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
				Utility.writeListToFile(DNSs, this.outputPath);
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
						//if all instances are running, all ids should be added into the a
						//array list. There is an instance terminated, the size of the array
						//will be less than the maximum of the launching number, which will
						//be used when running spot instance.
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
			//when the second while break, that means there is at least one instance is terminated.
			//Only such situation is checked twice by circle variable, the program will go out to 
			//the outer while loop, and then begin a new spot instance progress.
		}
	}
}
