import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CancelSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;
import com.amazonaws.services.ec2.model.SpotPlacement;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;


//this class is used to access EC2 client
//all functions that could be used by ec2 handler will be created in this class
public class AccessEC2 {
	private AmazonEC2Client ec2;
	//constructor: input is the credential path. Then EC2 client access is available
	AccessEC2(String awsCredentialsPath){
		//call hanlerEC2 to configure ec2 client, then return a EC2 client
		this.ec2 = handlerEC2(awsCredentialsPath);
	}
	//get the created ec2.
	public AmazonEC2Client getEC2(){
		return this.ec2;
	}
//	public void checkInstanceStatus(String instanceId) {
//		DescribeInstancesRequest request =new DescribeInstancesRequest();   
//
//        DescribeInstancesResult disresult =ec2.describeInstances(request);
//        List <Reservation> list  = disresult.getReservations();
//        
//        System.out.println("-------------- status of instances -------------");
//        for (Reservation res:list){
//             List <Instance> instancelist = res.getInstances();
//
//             for (Instance instance:instancelist){
//
//                 System.out.println("Instance Status : "+instance.getState().getName());
//                 List <Tag> t1 =instance.getTags();
//                 for (Tag teg:t1){
//                     System.out.println("Instance Name   : "+teg.getValue());
//                 }
//
//             }     
//        System.out.println("------------------------------------------------");
//         } 
//	}
	public String runInstance(String securityGroup, String AMI, String instanceType, int num, String zone) {
		System.out.println("[Info]: Trying to run an instance...");
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();  
		
		runInstancesRequest.withImageId(AMI)
		.withInstanceType(instanceType)
		.withMinCount(num)
		.withMaxCount(num)
		.withKeyName("is") //fixed
		.withPlacement(new Placement().withAvailabilityZone(zone))
		.withSecurityGroups(securityGroup); //fixed
		RunInstancesResult runInstancesResult = this.ec2.runInstances(runInstancesRequest);
		String instanceId=runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
		return instanceId;
	}
	/**
	 * 
	 * @param num
	 * @return instance of ids
	 */
	public ArrayList<String> runSpotInstance(String securityGroupName,String instanceType, String AMI, int num, String price, String zone) {
		// Setup the specifications of the launch. This includes the
		// instance type (e.g. t1.micro) and the latest Amazon Linux
		// AMI id available. Note, you should always use the latest
		// Amazon Linux AMI id or another of your choosing.
		LaunchSpecification launchSpecification = new LaunchSpecification();
		launchSpecification.withImageId(AMI)
			.withInstanceType(instanceType)
			.withKeyName("is") //fixed
			.withSecurityGroups(securityGroupName);
		
		// Initializes a Spot Instance Request
		RequestSpotInstancesRequest requestRequest = new RequestSpotInstancesRequest();
		SpotPlacement placement = new SpotPlacement(zone);
		launchSpecification.setPlacement(placement);
		requestRequest
			.withSpotPrice(price)
			.withInstanceCount(num) // number of spot instance
			.withLaunchSpecification(launchSpecification);
		
		
		

		// Call the RequestSpotInstance API.
		RequestSpotInstancesResult requestResult = this.ec2.requestSpotInstances(requestRequest);
		List<SpotInstanceRequest> requestResponses = requestResult.getSpotInstanceRequests();

		// Setup an arraylist to collect all of the request ids we want to
		// watch hit the running state.
		ArrayList<String> spotInstanceRequestIds = new ArrayList<String>();
		for (SpotInstanceRequest requestResponse : requestResponses) {
		    System.out.println("Created Spot Request: "+requestResponse.getSpotInstanceRequestId());
		    spotInstanceRequestIds.add(requestResponse.getSpotInstanceRequestId());
		}
		//check by spot request id
		return checkInstanceSpotStatus(spotInstanceRequestIds);
	}
	/**
	 * 
	 * @param spotInstanceRequestIds
	 * @return instanceIds: 
	 * 		if success : return an array of ids
	 * 		if failed  : return null
	 */
	private ArrayList<String> checkInstanceSpotStatus(ArrayList<String> spotInstanceRequestIds) {
		ArrayList<String> instanceIds = new ArrayList<String>();
		int open = spotInstanceRequestIds.size();;
		int active = 0;
		boolean failed = false;
		System.out.println("[Info]: Begin to check Instance Spot Status");
		do {
		    // Create the describeRequest object with all of the request ids
		    // to monitor (e.g. that we started).
			
		    DescribeSpotInstanceRequestsRequest describeRequest = new DescribeSpotInstanceRequestsRequest();
		    describeRequest.setSpotInstanceRequestIds(spotInstanceRequestIds);

		    try {
		        // Retrieve all of the requests we want to monitor.
		        DescribeSpotInstanceRequestsResult describeResult = this.ec2.describeSpotInstanceRequests(describeRequest);
		        List<SpotInstanceRequest> describeResponses = describeResult.getSpotInstanceRequests();
		        
		        active = spotInstanceRequestIds.size();
		        // Look through each request and determine if they are all in
		        // the active state.
		        for (SpotInstanceRequest describeResponse : describeResponses) {
		            // If the state is open, it hasn't changed since we attempted
		            // to request it. There is the potential for it to transition
		            // almost immediately to closed or cancelled so we compare
		            // against open instead of active.
			        if (describeResponse.getState().equals("open") && open != 0) {
			        	System.out.println("[Info]: One spot instance is open.");
			        	open--;
			        } else if (describeResponse.getState().equals("failed")){
			        	failed = true;
			        	System.out.println("[Error]: Any spot instances failed.");
			        	break;
			        } else if (describeResponse.getState().equals("active")) {
			        	System.out.println("[Info]: One spot instance is active.");
			        	active--;
			        }
			    }
		        //success
		        if (active == 0) {
		        	for (SpotInstanceRequest describeResponse : describeResponses) {
		        		instanceIds.add(describeResponse.getInstanceId());
		        	}
		        	System.out.println("[Info]: All spot instance are open.");
			    	break;
			    }
		        //fail
		        if (failed == true) {
		        	cancelSpotReq(spotInstanceRequestIds);
			    	return null;
			    }
			} catch (AmazonServiceException e) {
				return null;
			}

		    try {
		        // Sleep for 60 seconds.
		        Thread.sleep(60*1000);
		    } catch (Exception e) {
		        // Do nothing because it woke up early.
		    }
		    
		} while (true); 
		
		return instanceIds;
	}
	private void cancelSpotReq(ArrayList<String> spotInstanceRequestIds) {
		System.out.println("[Info]: Begin to CANCEL spot.");
		 try {
		      // Cancel requests.
		      CancelSpotInstanceRequestsRequest cancelRequest = new CancelSpotInstanceRequestsRequest(spotInstanceRequestIds);
		      this.ec2.cancelSpotInstanceRequests(cancelRequest);
		    } catch (AmazonServiceException e) {
		      // Write out any exceptions that may have occurred.
		      System.out.println("Error canceling instances");
		      System.out.println("Caught Exception: " + e.getMessage());
		      System.out.println("Reponse Status Code: " + e.getStatusCode());
		      System.out.println("Error Code: " + e.getErrorCode());
		      System.out.println("Request ID: " + e.getRequestId());
		    }
	}
	public void terminateInstance(ArrayList<String> instanceIds) {	
		try {
		    // Terminate instances.
		    TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest(instanceIds);
		    ec2.terminateInstances(terminateRequest);
		} catch (AmazonServiceException e) {
		    // Write out any exceptions that may have occurred.
		    System.out.println("Error terminating instances");
		    System.out.println("Caught Exception: " + e.getMessage());
		    System.out.println("Reponse Status Code: " + e.getStatusCode());
		    System.out.println("Error Code: " + e.getErrorCode());
		    System.out.println("Request ID: " + e.getRequestId());
		}
	}
	public void tagInstancesByIds(ArrayList<String> instanceIds, String name, String value) {
		System.out.println("[Info]: Tagging instance by <" + name +", " + value + ">.");
		// Create the list of tags we want to create
	    ArrayList<Tag> instanceTags = new ArrayList<Tag>();
	    instanceTags.add(new Tag("IS-ESE","Automation"));

	    // Create a tag request for instances.
	    CreateTagsRequest createTagsRequest_instances = new CreateTagsRequest();
	    createTagsRequest_instances.setResources(instanceIds);
	    createTagsRequest_instances.setTags(instanceTags);

	    // Try to tag the Spot instance started.
	    try {
	      ec2.createTags(createTagsRequest_instances);
	    } catch (AmazonServiceException e) {
	      // Write out any exceptions that may have occurred.
	      System.out.println("Error terminating instances");
	      System.out.println("Caught Exception: " + e.getMessage());
	      System.out.println("Reponse Status Code: " + e.getStatusCode());
	      System.out.println("Error Code: " + e.getErrorCode());
	      System.out.println("Request ID: " + e.getRequestId());
	    }

	}

	//use EC2client to create a security group
	public String createSecurityGroup(String groupName, String description) {
		String newGroupId = null;
		try {
			//create a security group
			CreateSecurityGroupRequest csgr = new CreateSecurityGroupRequest();
			//configure name and description
			csgr.withGroupName(groupName).withDescription(description);
			//link ec2 and security group
			CreateSecurityGroupResult createSecurityGroupResult = this.ec2.createSecurityGroup(csgr);
			newGroupId = createSecurityGroupResult.getGroupId();
            System.out.println(String.format("Security group created: [%s]",createSecurityGroupResult.getGroupId()));
        } catch (AmazonServiceException ase) {
            // Likely this means that the group is already created, so ignore.
        	//get id by group Name, then return 
            System.out.println(ase.getMessage());
            try{
            	DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
                DescribeSecurityGroupsResult dsgResult =  this.ec2.describeSecurityGroups(describeSecurityGroupsRequest.withGroupNames(groupName));
                newGroupId = dsgResult.getSecurityGroups().get(0).getGroupId();
                return newGroupId;
            } catch(AmazonServiceException ase1) {
            	System.out.println(ase1.getMessage());
            	System.exit(0);
            }
            
        }
		//configure ip, protocl,port information.
		String ipAddr = "0.0.0.0/0";
		List<String> ipRanges = Collections.singletonList(ipAddr);
		IpPermission ipPermission =  new IpPermission();
		ipPermission.withIpRanges(ipRanges)
		            .withIpProtocol("tcp")
		            .withFromPort(80)
		            .withToPort(80);
		//authorize this security group used by the ec2.
		AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest();
		authorizeSecurityGroupIngressRequest.withGroupName(groupName).withIpPermissions(ipPermission);
		this.ec2.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
		System.out.println("Security Group Id: " + newGroupId);
		return newGroupId;
	}

	//get state by id
	public String getStateById(String instancId){
		if (instancId == null) {
			System.out.println("[Error]: Getting Instance Id failing.");
			return null;
		}
		//Obtain a list of Reservations
		List<Reservation> reservations = this.ec2.describeInstances().getReservations();
		for (Reservation reservation:reservations) {
			for (Instance instance: reservation.getInstances()) {
				if (instance.getInstanceId().equals(instancId)) {
					//only when state is running, dns is available.
					if(instance.getState().getName().equals("running")) {
						return "running";
					//pending is a normal state before running
					} else if (instance.getState().getName().equals("pending")) {
						return "pending";
					//other state will make DNS invalid forever.
					}else if (instance.getState().getName().equals("terminated")) {
						return "terminated";
					//other state will make DNS invalid forever.
					} else if (instance.getState().getName().equals("shutting-down")){
						return "shutting-down";
					} else {
						return null;
					}
				}
			}
		}
		return null;
	}
	//configure ec2 by aws credential path
	private AmazonEC2Client handlerEC2 (String awsCredentialsPath) {
		Properties properties = new Properties();
		try {
			//get credentials path under this class path.
			properties.load(AccessEC2.class.getResourceAsStream(awsCredentialsPath));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		//get info. by accessKey and secretKey
		BasicAWSCredentials bawsc = new BasicAWSCredentials(properties.getProperty("accessKey"), properties.getProperty("secretKey"));
		//Create an Amazon EC2 Client by basic aws credentials
		AmazonEC2Client ec2 = new AmazonEC2Client(bawsc);
		//set region of ec2 to Virginia
		Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        ec2.setRegion(usEast1);
		return ec2;
	}
}
