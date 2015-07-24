import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class Utility {
	public static void timerS(int seconds) {
		try {
			TimeUnit.SECONDS.sleep(seconds);
		} catch (InterruptedException e) {
			logPrint(e.toString());
		}
	}
	public static String writeListToFile(ArrayList<String> list, String fileName){
		String absPath = "";
		try {
		    File theDir = new File("resource");
		    if (!theDir.exists()) {
		    	theDir.mkdir();
			}	
		    absPath = new File("resource/" + fileName).getAbsolutePath();        
			FileWriter fw = new FileWriter(absPath,false);
			for (String s: list) {
				logPrint("[Info]: WRITE \"" + s + "\" to " + fileName);
				fw.write(s);
				fw.write("\n");
			}
			fw.close();
		} catch ( Exception e) {
			logPrint(e.toString());
		}	
		return absPath;
	}
	public static String writeToFile(String string, String fileName){
		String absPath = "";
		try {
		    File theDir = new File("resource");
		    if (!theDir.exists()) {
		    	theDir.mkdir();
			}	
		    absPath = new File("resource/" + fileName).getAbsolutePath();        
			FileWriter fw = new FileWriter(absPath,false);
			logPrint("[Info]: WRITE \"" + string + "\" to " + fileName);
			fw.write(string);
			fw.write("\n");
			fw.close();
		} catch ( Exception e) {
			logPrint(e.toString());
		}	
		return absPath;
	}
	public static void logPrint(String string) {
		Date dNow = new Date( );
		SimpleDateFormat ft = new SimpleDateFormat ("yyyy/MM/dd HH:mm:ss zzz");
		System.out.println("[" + ft.format(dNow) + "]==" + string);
	}
	/**SSH to exec with parameters
	 * @param: remote host name(DNS) <hostName>
	 * @param: remote executable script <remoteScriptName>
	 * @param: sent information, eg. mySql_ip <mySqlIP>
	 **/
	public static boolean remoteExecWithPara(String hostName, String remoteScriptName, String mySqlIP, String S3Bucket) {
		int tatalDuration = 10;	//minute
		int eachSleep = 30; //second
		int iteration = tatalDuration * 60 / eachSleep;	//times
		String scriptPathName = "sshExec.sh";
		while(true) {
			String cmd = "sh " + scriptPathName + " " + hostName + " " + remoteScriptName + " " + mySqlIP + " " + S3Bucket;
			String inputLine = execBach(cmd);
			if (inputLine.compareTo("OK") == 0) {
				logPrint("[Info]: SSH exec successfully.");
				return true;
			} else {
				logPrint("[Error]: SSH exec failing.");
			}
			if(--iteration < 0) {
				return false;
			}
			timerS(eachSleep);
		}
	} 
	
	public static String getLocalIp() { 
		String cmd = "curl http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null";
		String ip = execBach(cmd);
		if (ip.matches("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$")) {
			logPrint("[Info]: IP: " + ip);
			return ip;
		}
		return null;
	}
	/**SCP to a remote file with parameters
	 * @param: remote host name(DNS) <hostName>
	 * @param: destination <directory>
	 * @param: source <filePath>
	 * */
	public static boolean scpFileByBash(String hostName, String directory, String filePath) {
		int tatalDuration = 10;	//minute
		int eachSleep = 30; //second
		int iteration = tatalDuration * 60 / eachSleep;	//times
		String scriptPathName = "scpSend.sh";
		while(true) {
			String cmd = "sh " + scriptPathName + " " + hostName + " " + directory + " " + filePath;
			String inputLine = execBach(cmd);
			if (inputLine.compareTo("OK") == 0) {
				logPrint("[Info]: SCP send successfully.");
				return true;
			} else {
				logPrint("[Error]: SCP send failing.");
			}
			if(--iteration < 0) {
				return false;
			}
			timerS(eachSleep);
		}
	}	
	public static String execBach(String cmd) {
		String inputLine = "";
		try {
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(cmd);
			logPrint("[Exec]: " + cmd);
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (Exception e) {
			logPrint(e.toString());
		}
		return inputLine;
	}
}
