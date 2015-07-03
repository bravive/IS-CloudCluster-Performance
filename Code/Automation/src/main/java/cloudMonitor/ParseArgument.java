import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class ParseArgument {
	@SuppressWarnings("static-access")
	public static Arguments parseArguments(String[] args) {
		/**Load all input arguments into this object, which will be used in Automation******/
		Arguments allArguments = new Arguments(); 
		try {
			Options options = new Options();
			/**Define all options for arguments*******/
			/**boolean Option*/
			Option exec = new Option( "e", "executing monitoring");
			Option help = new Option( "h", "print help information" );
			/**Argument Option*/
			Option aMax = OptionBuilder.withArgName( "aMaxNum" ).hasArg().withDescription("Specify AGGREGATOR maximal number. \n[Default: " + allArguments.aggMaxInstanceNum + "]").create( "aMax");
			Option nMax = OptionBuilder.withArgName( "nMaxNum" ).hasArg().withDescription("Specify NODES maximal number. \n[Default: " + allArguments.nodMaxInstanceNum + "]").create( "nMax");
			Option aAMI = OptionBuilder.withArgName( "aAMIID" ).hasArg().withDescription("Specify AGGREGATOR AMI ID. \n[Default: " + allArguments.aggAmiId + "]").create( "aAMI");
			Option nAMI = OptionBuilder.withArgName( "nAMIID" ).hasArg().withDescription("Specify NODES AMI ID. \n[Default: " + allArguments.nodAmiId + "]").create( "nAMI");
			Option aInstanceType = OptionBuilder.withArgName( "aInstanceType" ).hasArg().withDescription("Specify AGGREGATOR aws instance type. \n[Default: " + allArguments.aggInstanceType + "]").create( "aInstanceType");
			Option nInstanceType = OptionBuilder.withArgName( "nInstanceType" ).hasArg().withDescription("Specify NODES aws instance type. \n[Default: " + allArguments.nodInstanceType + "]").create( "nInstanceType");
			Option aZone = OptionBuilder.withArgName( "aZone" ).hasArg().withDescription("Specify AGGREGATOR avaiable zone. \n[Default: " + allArguments.aggZone + "]").create( "aZone");
			Option nZone = OptionBuilder.withArgName( "nZone" ).hasArg().withDescription("Specify NODES avaiable zone. \n[Default: " + allArguments.nodZone + "]").create( "nZone");
			Option securityGroup = OptionBuilder.withArgName( "securityGroup" ).hasArg().withDescription("Specify security group name. \n[Default: " + allArguments.securityGroup + "]").create( "securityGroup");
			Option productDescribe = OptionBuilder.withArgName( "productDescribe" ).hasArg().withDescription("Specify VM product describe. \n[Default: " + allArguments.productDescribe + "]").create( "productDescribe");
			
			/**Property Option*/
			Option property  = OptionBuilder.withArgName("property=value" ).hasArgs(2).withValueSeparator().withDescription( "Specify access key id BY \"accessKeyId\"; \nSpecify secret accesss key id BY \"secretAccesssKey\";" ).create( "D" );
			
			/**All option to options for parsing*******/
			options.addOption(help);
			options.addOption(exec);
			options.addOption(aMax);
			options.addOption(nMax);
			
			options.addOption(aAMI);
			options.addOption(nAMI);
			options.addOption(aInstanceType);
			options.addOption(nInstanceType);
			options.addOption(aZone);
			options.addOption(nZone);
			options.addOption(securityGroup);
			options.addOption(productDescribe);
			
			options.addOption(property);
			
			/**Lood options into parser for parsing*******/
			CommandLineParser parser = new GnuParser();
			CommandLine cmd = parser.parse(options, args);
			/**Execute corresponding block codes for different arguments******/
			if (cmd.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "cloudMonitor", options );
				System.exit(1);
			}
			
			if (cmd.hasOption("aMax")) {
				allArguments.aggMaxInstanceNum = Integer.parseInt(cmd.getOptionValue("aMax"));
			}
			if (cmd.hasOption("nMax")) {
				allArguments.nodMaxInstanceNum = Integer.parseInt(cmd.getOptionValue("nMax"));
			}
			if (cmd.hasOption("aAMI")) {
				allArguments.aggAmiId = cmd.getOptionValue("aAMI");
			}
			if (cmd.hasOption("nAMI")) {
				allArguments.aggAmiId = cmd.getOptionValue("nAMI");
			}
			if (cmd.hasOption("aInstanceType")) {
				allArguments.aggInstanceType = cmd.getOptionValue("aInstanceType");
			}
			if (cmd.hasOption("nInstanceType")) {
				allArguments.nodInstanceType = cmd.getOptionValue("nInstanceType");
			}
			if (cmd.hasOption("aZone")) {
				allArguments.aggZone = cmd.getOptionValue("aZone");
			}
			if (cmd.hasOption("nZone")) {
				allArguments.nodZone = cmd.getOptionValue("nZone");
			}
			if (cmd.hasOption("securityGroup")) {
				allArguments.securityGroup = cmd.getOptionValue("securityGroup");
			}
			if (cmd.hasOption("productDescribe")) {
				allArguments.productDescribe = cmd.getOptionValue("productDescribe");
			}
			
			if (cmd.hasOption("D")) {
				Properties props = cmd.getOptionProperties("D");
				if (props.getProperty("accessKeyId") == null || props.getProperty("secretAccesssKey") == null) {
					Utility.logPrint("[Error]: Must provide your accessKeyId AND secretAccesssKey.");
					System.exit(1);
				}
				allArguments.accessKeyId = props.getProperty("accessKeyId");
				allArguments.secretAccesssKey = props.getProperty("secretAccesssKey");
			} else {
				Utility.logPrint("[Error]: Must provide your accessKeyId AND secretAccesssKey.");
				System.exit(1);
			}
			
			if (!cmd.hasOption("e")) {
				Utility.logPrint("[Info]: If you want execute monitor, please add -e argument");
				System.exit(1);
			}
			
		} catch (ParseException e) {
			Utility.logPrint(e.toString());
		}
		return allArguments;
	}
}
class Arguments {
	String accessKeyId = "";
	String secretAccesssKey = "";
	String securityGroup = "all-traffic";
	String productDescribe = "Linux/UNIX";
	/**************Aggregator Initial Variable**************/
	String aggAmiId = "ami-dbfc07b0"; //"ami-0e0b1166";
	int aggMaxInstanceNum = 1;
	String aggZone = "us-east-1d";
	String aggInstanceType = "m3.medium";
	String aggOutputPath = "AggregatorDNS.info";
	/**************Nodes Initial Variable**************/
	String nodAmiId = "ami-dbfc07b0"; //"ami-0e0b1166";
	int nodMaxInstanceNum = 2;
	String nodZone = "us-east-1d";
	String nodInstanceType = "m3.medium";
	String nodOutputPath = "NodesDNS.info";
	public String toString() {
		String rs = "";
		rs += "-------All Arguments information------\n";
		rs += "accessKeyId: " + this.accessKeyId + "\n";
		rs += "secretAccesssKey: " + this.secretAccesssKey + "\n";
		rs += "securityGroup: " + this.securityGroup + "\n";
		rs += "productDescribe: " + this.productDescribe + "\n";
		
		rs += "aggSmiId: " + this.aggAmiId + "\n";
		rs += "aggMaxInstanceNum: " + this.aggMaxInstanceNum + "\n";
		rs += "aggZone: " + this.aggZone + "\n";
		rs += "aggInstanceType: " + this.aggInstanceType + "\n";
		rs += "aggOutputPath: " + this.aggOutputPath + "\n";
		
		rs += "nodAmiId: " + this.nodAmiId + "\n";
		rs += "nodMaxInstanceNum: " + this.nodMaxInstanceNum + "\n";
		rs += "nodZone: " + this.nodZone + "\n";
		rs += "nodInstanceType: " + this.nodInstanceType + "\n";
		rs += "nodInstanceType: " + this.nodInstanceType + "\n";
		rs += "-------------All Arguments End----------\n";
		return rs;
	}
}
