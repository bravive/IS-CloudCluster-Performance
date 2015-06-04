import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Project22 {
	private static void timerS(int seconds) {
		try {
			TimeUnit.SECONDS.sleep(seconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		String awsCredentialsPath = "/AwsCredentials.properties";
		//String securityGroup = "Project2Group";
		String securityGroup = "all-traffic";
		String amiId = "ami-0e0b1166";
//		String asConfigName = "AS-Config-Program";
//		String elbName = "ELB-Project22-Program";
//		String asgName = "ASG-Program";
//		String tagValue = "2.2";
		
		/*You should change this LG_DNS to an available LG DNS*/
		//String LG_DNS = "ec2-52-1-99-245.compute-1.amazonaws.com";
		
		
		//Access EC2//
		AccessEC2 EC2handler = new AccessEC2(awsCredentialsPath);
		String instanceId = EC2handler.runInstance(securityGroup, amiId, "t2.micro", 1, "us-east-1a");
		//String instanceId = "i-fe58fd57";
		
		while (true) {
			String state = EC2handler.getStateById(instanceId);
			if (state == null) {
				System.out.println("[Error]: Getting state failing");
				break;
			}
			
			if (state.equals("running")) {
				System.out.println("[" + instanceId + "]: " +  "is running.");
				ArrayList<String> array = new ArrayList<String>();
				array.add(instanceId);
				EC2handler.tagInstancesByIds(array,"IS-ESE", "Automation");
			} else if (state.equals("pending")){
				System.out.println("[" + instanceId + "]: " +  "is pending.");
			} else if (state.equals("terminated")) {
				System.out.println("[" + instanceId + "]: " +  "is terminated.");
				break;
			} else if (state.equals("shutting-down")) {
				System.out.println("[" + instanceId + "]: " +  "is shutting-down.");
			}
			timerS(60);
		}
		System.out.println("Done.");
//		ArrayList<String> instanceIds = EC2handler.runSpotInstance(securityGroup, amiId, 1);
//		if (instanceIds != null) {
//			EC2handler.tagInstanceById(instanceIds, "IS-ESE", "Automation");
//			for (String s : instanceIds) {
//				System.out.println("Instance Id : <" + s + ">");
//			}
//		}
//		System.out.println("Done.");
		
		//create a new security group
		//String securityGroupId = EC2handler.createSecurityGroup( securityGroup, "Created for Project2 Programming");
		//System.out.println("securityGroupId:" + securityGroupId);
		
		//Access Auto Scaling//
			//AccessAutoScaling AShandler = new AccessAutoScaling(awsCredentialsPath);
			//AShandler.launchConfiguration(asConfigName, securityGroup);
		
		//Access Elastic Load Balancer//
			//AccessElasticLoadBalancer ELBhandler = new AccessElasticLoadBalancer(awsCredentialsPath);
			//String ELB_DNS = ELBhandler.createLoadBalancer(elbName, securityGroupId, tagValue);
			//System.out.println("ELB DNS : " + ELB_DNS);
		
		//Create a ASG//
			//AShandler.createASG(asConfigName, elbName, asgName, tagValue);
		
		//Begin to Work.(Put together)
		//wait ELB available, keep checking /tracker/devcie
		//only if it is available, the program continues.
			//myUtility.waitTrackerDevice(ELB_DNS);
			//boolean isLargerThan2500 = false;
		//Keep warmup test, until rps has larger than 2500
//		while(!isLargerThan2500) {
//			System.out.println("Warm Up..");
//			//create a new warmup test
//			String testId = myUtility.warmUp(ELB_DNS, LG_DNS);
//			//read INI log file and check if there is rps over 2500
//			isLargerThan2500 = myUtility.checkRPS2500(myUtility.logUrl(LG_DNS, testId));
//		}
//		System.out.println("Warm Up End!");
//		
//		System.out.println("Wait two minutes to begin Test");
//		
//		//create a new junior test
//		String testId = myUtility.junior(ELB_DNS, LG_DNS);
//		System.out.println("Junior log test Id:" + testId);
//		//wait for junior test over. It will take 48 minutes.
//		myUtility.waitJuniorOver(myUtility.logUrl(LG_DNS, testId));
//		System.out.println("Junior Test Over");
		
		
		//To terminate resources
		
		//terminate ASG(policy and alarm included)
			//AShandler.temianteASG(asgName);
		//terminate ELB
			//ELBhandler.temianteELB(elbName);
		//terminate asConfigure
			//AShandler.temianteASGConfigure(asConfigName);
		//terminate Security Group
			//EC2handler.deleteSecurityGroup(securityGroup);
	}
}
