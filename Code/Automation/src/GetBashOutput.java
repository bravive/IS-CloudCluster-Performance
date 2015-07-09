import java.io.*;

class GetBashOutput {
	public void getBashOutput(String hostname, String filename) {
		try {
			Runtime r = Runtime.getRuntime();
			while(true) {
				Process p = r.exec("sh coordinator.sh " + hostname + " " + filename);
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String inputLine;
				if ((inputLine = in.readLine()) == "OK") {
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
		test.getBashOutput("ec2-52-6-181-148.compute-1.amazonaws.com", "crontab");
	}
}