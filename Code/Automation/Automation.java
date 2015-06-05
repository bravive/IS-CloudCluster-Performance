import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Automation {
	private static void timerS(int seconds) {
		try {
			TimeUnit.SECONDS.sleep(seconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		String awsCredentialsPath = "/AwsCredentials.properties";

		String securityGroup = "all-traffic";
		String amiId = "ami-0e0b1166";
		int maxInstanceNum = 3;
		float baseSpotPrice = 0.01f;
		String zone = "us-east-1d";
		//Access EC2//
		AccessEC2 EC2handler = new AccessEC2(awsCredentialsPath);
		ArrayList<String> instanceIds = EC2handler.runSpotInstance(securityGroup, "m3.medium", amiId, maxInstanceNum, Float.toString(baseSpotPrice), zone);
		
		//spot instance
		while (true) {
			while(true) {
				//repeat tag will not cause problems,
				if (instanceIds != null) {
					EC2handler.tagInstancesByIds(instanceIds, "IS-ESE", "Automation");
					for (String s : instanceIds) {
						System.out.println("Instance Id : <" + s + ">");
					}
				}
				if (instanceIds.size() < maxInstanceNum) {
					baseSpotPrice = baseSpotPrice + 0.01f;
					ArrayList<String> newIds = EC2handler.runSpotInstance(securityGroup, "m3.medium", amiId, maxInstanceNum - instanceIds.size(), Float.toString(baseSpotPrice),zone);
					instanceIds.addAll(newIds);
				} else if (instanceIds.size() == maxInstanceNum) {
					break;
				}
			}
			//monitor instance
			//
			int runningCount = 0;
			int terminatedCount = 0;
			int circle = 0;
			while (true) {
				ArrayList<String> temp = new ArrayList<String>();
				for (String instanceId : instanceIds) {
					String state = EC2handler.getStateById(instanceId);
					if (state == null) {
						System.out.println("[Error]: Getting state failing");
						break;
					}
					if (state.equals("running")) {
						System.out.println("[" + instanceId + "]: " +  "is running.");
						runningCount++;
						//if all instances are running, all ids should be added into the a
						//array list. There is an instance terminated, the size of the array
						//will be less than the maximum of the launching number, which will
						//be used when running spot instance.
						temp.add(instanceId);
					} else if (state.equals("pending")){
						System.out.println("[" + instanceId + "]: " +  "is pending.");
					} else if (state.equals("terminated")) {
						System.out.println("[" + instanceId + "]: " +  "is terminated.");
						terminatedCount++;
					} else if (state.equals("shutting-down")) {
						System.out.println("[" + instanceId + "]: " +  "is shutting-down.");
					}	
				}
				//Must check if there are instance in nether running nor terminated. 
				if (runningCount < maxInstanceNum && (runningCount + terminatedCount) == maxInstanceNum && circle == 0) {
					circle++;
				} else if (runningCount < maxInstanceNum && (runningCount + terminatedCount) == maxInstanceNum && circle != 0) {
					// double check if all instance state are stable
					instanceIds = temp;
					break;
				}
				runningCount = 0;
				terminatedCount = 0;
				timerS(60);
			}
			//when the second while break, that means there is at least one instance is terminated.
			//Only such situation is checked twice by circle variable, the program will go out to 
			//the outer while loop, and then begin a new spot instance progress.
		}
	}
}
