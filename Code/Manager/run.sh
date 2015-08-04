#!/bin/bash
# Reset all variables that might be set
accessKeyId=
secretAccessKey=
securityGroup="all-traffic"
productDescribe="Linux/UNIX"

aAMI=
aMax=1
aZone="us-east-1d"
aInstanceType="m3.medium"

nAMI=
nMax=2
nZone="us-east-1d"
nInstanceType="m3.medium"

aSpot="true"
nSpot="true"

verbose=0 

show_help () {
	echo "Usage: sh run.sh [--option] [args]"
	echo "where options include:"
	echo "	--accessKeyId 			Specify access key id."
	echo "	--secretAccessKey 		Specify secret accesss key id."
	echo " 	--securityGroup 		Specify security group name. [Default: $securityGroup]"
	echo "	--productDescribe 		Specify VM product describe. [Default: $productDescribe]"
	echo "	--aAMI 				Specify AGGREGATOR AMI ID."
	echo "	--aMax 				Specify AGGREGATOR maximal number. [Default: $aMax]"
	echo "	--aZone 			Specify AGGREGATOR avaiable zone. [Default: $aZone]"
	echo "	--aInstanceType 		Specify AGGREGATOR aws instance type. [Default: $aInstanceType]"
	echo "	--nAMI    			Specify NODES AMI ID."
	echo "	--nMax 			 	Specify NODES maximal number. [Default: $nMax]"
	echo "	--nZone 			Specify NODES avaiable zone. [Default: $nZone]"
	echo "	--nInstanceType 		Specify NODES aws instance type. [Default: $nInstanceType]"
	echo "  --aSpot             Specify whether use spot instance for aggregator. [Default: $aSpot]"
	echo "  --nSpot             Specify whether use spot instance for nodes [Default: $nSpot]."
}

while :; do
    case $1 in
        -h|-\?|--help)   # Call a "show_help" function to display a synopsis, then exit.
            show_help
            exit
            ;;
        --accessKeyId)   
			if [ -n "$2" ]; then
				accessKeyId=$2
				shift 2
				continue
			else 
				printf 'ERROR: "--accessKeyId" requires a non-empty option argument.\n' >&2
                exit 1
            fi
            ;;
		--secretAccessKey)
			if [ -n "$2" ]; then
				secretAccessKey=$2
				shift 2
				continue
			else 
				printf 'ERROR: "--secretAccessKey" requires a non-empty option argument.\n' >&2
                exit 1
            fi
            ;;
        --securityGroup)
			if [ -n "$2" ]; then
				securityGroup=$2
				shift 2
				continue
			fi
			;;
		--productDescribe)
			if [ -n "$2" ]; then
				productDescribe=$2
				shift 2
				continue
			fi
			;;
		--aAMI)
			if [ -n "$2" ]; then
				aAMI=$2
				shift 2
				continue
			else 
				printf 'ERROR: "--aAMI" requires a non-empty option argument.\n' >&2
                exit 1
			fi
			;;
		--aMax)
			if [ -n "$2" ]; then
				aMax=$2
				shift 2
				continue
			fi
			;;
		--aZone)
			if [ -n "$2" ]; then
				aZone=$2
				shift 2
				continue
			fi
			;;
		--aSpot)
			if [ -n "$2" ]; then
				aSpot=$2
				shift 2
				continue
			fi
			;;
		--nSpot)
			if [ -n "$2" ]; then
				nSpot=$2
				shift 2
				continue
			fi
			;;
		--aInstanceType)
			if [ -n "$2" ]; then
				aInstanceType=$2
				shift 2
				continue
			fi
			;;
		--nAMI)
			if [ -n "$2" ]; then
				nAMI=$2
				shift 2
				continue
			else 
				printf 'ERROR: "--nAMI" requires a non-empty option argument.\n' >&2
                exit 1
			fi
			;;
		--nMax)
			if [ -n "$2" ]; then
				nMax=$2
				shift 2
				continue
			fi
			;;
		--nZone)
			if [ -n "$2" ]; then
				nZone=$2
				shift 2
				continue
			fi
			;;
		--nInstanceType)
			if [ -n "$2" ]; then
				nInstanceType=$2
				shift 2
				continue
			fi
			;;
        -v|--verbose)
            verbose=$((verbose + 1)) # Each -v argument adds 1 to verbosity.
            ;;
        --)              # End of all options.
            shift
            break
            ;;
        -?*)
            printf 'WARN: Unknown option (ignored): %s\n' "$1" >&2
            ;;
        *)               # Default case: If no more options then break out of the loop.
            break
    esac

    shift
done

if [ -z "$accessKeyId" ]; then
    printf 'ERROR: option "--accessKeyId KEYID" not given. See --help.\n' >&2
    exit 1
fi
if [ -z "$secretAccessKey" ]; then
    printf 'ERROR: option "--secretAccessKey KEY" not given. See --help.\n' >&2
    exit 1
fi

echo "securityGroup=$securityGroup"
echo "productDescribe=$productDescribe"

echo "aAMI=$aAMI"
echo "aMax=$aMax"
echo "aZone=$aZone"
echo "aInstanceType=$aInstanceType"
echo "aSpot=$aSpot"

echo "nAMI=$nAMI"
echo "nMax=$nMax"
echo "nZone=$nZone"
echo "nInstanceType=$nInstanceType"
echo "nSpot=$nSpot"

awk -v "a=$accessKeyId" -v "b=$secretAccessKey" '{ 
        if ($1~/^access_key*/) 
                print "access_key = " a; 
        else if($1~/^secret_key*/)      
                print "secret_key = " b;
        else print $0
}' ../conf/s3cfg > s3cfg1
mv s3cfg1 ../conf/s3cfg

cd ../..
java -cp 'DashBoard.jar' DashBoard &
cd ziyuans/monitor
mvn exec:java -Dexec.args="-e -DaccessKeyId=$accessKeyId -DsecretAccesssKey=$secretAccessKey -securityGroup=$securityGroup -productDescribe=$productDescribe -aAMI=$aAMI -aMax=$aMax -aZone=$aZone -aInstanceType=$aInstanceType -nAMI=$nAMI -nMax=$nMax -nZone=$nZone -nInstanceType=$nInstanceType -aSpot=$aSpot -nSpot=$nSpot"


