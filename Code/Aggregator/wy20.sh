#iP List
#change this to list the user and IPs that are to be examined.


#Default Params
user="root"
auto=0
ipfile="default"
firewall=0
output_file="wy20results.csv"
output_file2="wy20matrix.txt"

##Setting up SSH to use local bash profile for execution
source="source ~/.bash_profile; "


#EC2 Keyname
key="/home/hadoop/.ssh/id_rsa"


#Usage:
USAGE="Usage: `basename $0` -u <username_of_ec2_instance> -k <path_to_keypair_file> [-i <path_to_file_containing_ips> | -a] [-f] -o <output_file>"
USAGE2="(-a Switch indicates automatically locate all instances and connect with keypair)"

# Parse command line options.
while getopts hu:k:i:afo: OPT; do
    case "$OPT" in
        h)
            echo $USAGE
	    echo $USAGE2
            exit 0
            ;;
        u)
            user=$OPTARG
	    echo Username=$user
	    ;;
        k)
            key=$OPTARG
	    echo Key Pair=$key
            ;;
	i)
	    ipfile=$OPTARG
	    echo IP File=$ipfile
	    ;;

	a)
	    auto=1
	    ;;

	f)
	    firewall=1
	    ;;

	o)
	    output_file=$OPTARG
	    echo Output File=$output_file
	    ;;

        \?)
            # getopts issues an error message
            echo $USAGE >&2
	    echo $USAGE2 >&2
            exit 1
            ;;
    esac
done

# Remove the switches we parsed above.
shift `expr $OPTIND - 1`


#Debug
echo "Starting wy20..."
if [ $auto -eq 1 ]
then
	#Get the IPs from EC2
	host=($(./getips.sh))
	num=${#host[*]}
	echo $num Machines Detected on EC2
else
	#echo $ipfile
	if [ "$ipfile" == "default" ]
	then
		echo "Error! Need IP file, or specify -a" >&2
		echo $USAGE >&2
		echo $USAGE2 >&2
		exit 0
	fi

	host=($(cat $ipfile))
	num=${#host[*]}
	echo $num IPs Read from File
fi


#Local variables
a=0
aLimit=$num

b=0

c=0
cLimit=1

total=0
current=0

#date >> $output_file

#setup the netperf server in each machine
echo Setting up Netperf Servers in each machine... 
while [ "$a" -lt "$aLimit" ]
do
	echo Launching Server on ${host[a]}

	#Disable Firewall, if requested on command line
	if [ $firewall -eq 1 ]
	then
		echo Disabling Firewall on ${host[a]}
		ssh -i $key -o "UserKnownHostsFile /dev/null" -o LogLevel=quiet root@${host[a]} $source 'service iptables stop'
		ssh -i $key -o "UserKnownHostsFile /dev/null" -o StrictHostKeyChecking=no $user@${host[a]} $source './stopfirewall.sh'
	fi

	ssh -i $key -o "UserKnownHostsFile /dev/null" -o StrictHostKeyChecking=no -o LogLevel=quiet $user@${host[a]} $source 'netserver &' >> serversdump.log
	let "a+=1"
done

#reset variable a
let "a=0"
let "total= ( ( $aLimit - 1) * $aLimit) / 2"
echo Running point-to-point network tests...
#run point to point network tests.
while [ "$a" -lt "$aLimit" ]
do

	let "b=a+1"
	
	while [ "$b" -lt "$aLimit" ]
	do 
		
		let "c=0"
		while [ "$c" -lt "$cLimit" ]
			do
			let "current+=1"
			echo \(${current}\\${total}\) Testing link from ${host[a]} to ${host[b]}, Iteration $c
			result=`ssh -i $key -o "UserKnownHostsFile /dev/null" -o StrictHostKeyChecking=no -o LogLevel=quiet $user@${host[a]} $source 'netperf -P 0 -v 0 -H ' ${host[b]}`
			echo $(date +%Y%m%d%H%M%S),${host[a]},${host[b]},$c,$result >> $output_file
			echo $a	$b $result >> $output_file2
			let "c+=1"
		done	

		let "b+=1"
	done
	let "a+=1" 
done

echo Tests Complete, Killing Netperf Servers...
let "a=0"
while [ "$a" -lt "$aLimit" ]
do
	ssh -i $key -o "UserKnownHostsFile /dev/null" -o StrictHostKeyChecking=no -o LogLevel=quiet $user@${host[a]} $source 'killall netserver'

	#Restore Firewall Rules, if any.
	if [ $firewall -eq 1 ]
	then
		echo Killing Firewall on ${host[a]}
		ssh -i $key -o "UserKnownHostsFile /dev/null" -o StrictHostKeyChecking=no$key -o LogLevel=quiet $user@${host[a]} $source 'sudo iptables-restore < ~/firewall.rules'
	fi

	let 'a+=1'
done

echo "  " >> $output_file

#Debug, send email
#echo "wy20 Completed" | mail -s "wy20" suhailrehman@gmail.com

