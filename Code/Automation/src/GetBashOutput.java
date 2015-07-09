import java.io.*;

class GetBashOutput {
	public void getBashOutput(String hostname, String filename) {
		try {
			Runtime r = Runtime.getRuntime();
			boolean flag = true;
			while(flag) {
				Process p = r.exec("sh coordinator.sh " + hostname + " " + filename);
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String inputLine;
				if ((inputLine = in.readLine()).compareTo("OK") == 0) {
					flag = false;
					System.out.println(inputLine);
					in.close();
					break;
				}
				else {
					System.out.println(inputLine);
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}	
}
class GetBashOutputTest {
	public static void main(String[] args) {
		GetBashOutput test = new GetBashOutput();
		test.getBashOutput("ec2-52-2-41-239.compute-1.amazonaws.com:/home/ec2-user", "crontab");
	}
}