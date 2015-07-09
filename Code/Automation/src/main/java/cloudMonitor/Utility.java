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
	public static void writeListToFile(ArrayList<String> list, String filePath){
		try {
		    File theDir = new File("watch");
		    if (!theDir.exists()) {
		    	theDir.mkdir();
			}	
		    String path = new File("watch/" + filePath).getAbsolutePath();        
			FileWriter fw = new FileWriter(path,false);
			for (String s: list) {
				logPrint("[Info]: WRITE \"" + s + "\" to " + filePath);
				fw.write(s);
				fw.write("\n");
			}
			fw.close();
		} catch ( Exception e) {
			e.printStackTrace();
		}		
	}
	public static void logPrint(String string) {
		Date dNow = new Date( );
		SimpleDateFormat ft = new SimpleDateFormat ("yyyy/MM/dd HH:mm:ss zzz");
		System.out.println("[" + ft.format(dNow) + "]==" + string);
	}
	public void scpFileByBash(String hostName, String filePath) {
		int tatalDuration = 10;	//minute
		int eachSleep = 30; //second
		int iteration = tatalDuration * 60 / eachSleep;	//times
		String scriptPathName = "scpSend.sh";
		try {
			Runtime r = Runtime.getRuntime();
			while(true) {
				Process p = r.exec("sh " + scriptPathName + " " + hostName + " " + filePath);
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String inputLine;
				if ((inputLine = in.readLine()) == "OK") {
					logPrint(inputLine);
					in.close();
					break;
				}
				in.close();
				if(--iteration < 0) {
					break;
				}
				timerS(eachSleep);
			}
		} catch (Exception e) {
			logPrint(e.toString());
		}
	}	
}
