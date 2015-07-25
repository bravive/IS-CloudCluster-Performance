#!/bin/bash
#param1: master's public ip address
#param2: s3 backup path

PUBLIC_HOSTNAME="$(curl http://169.254.169.254/latest/meta-data/public-hostname 2>/dev/null)"
ACTIVE_NODES_FILE="NodesDNS.info"
HEALTHY_NODES_FILE="HealthyNodesDNS.info"
MASTER_IP=$1
DB_NAME="cloudMonitorDB"
USERNAME=cloudMonitor
PASSWORD=cloudMonitor
S3_PATH=$2
PEM_FILE="is.pem"

# helper function for inserting data into database
# param1: tableName
# other params: columndValues
insert_data() {
	case $1 in
		*CPU | *MEMR | *MEMW | *DIO)
			sqlCmd="USE "$DB_NAME"; "
			sqlCmd=$sqlCmd"INSERT INTO \`"$1"\` VALUES ("$2", "$3");"
			mysql -h $MASTER_IP -u $USERNAME -p$PASSWORD -s -N -e "$sqlCmd"
			;;
		*NET)
			sqlCmd="USE "$DB_NAME"; "
			sqlCmd=$sqlCmd"UPDATE \`"$1"\` SET timestamp = "$2", value = "$4" WHERE reference = \""$3"\";"
			mysql -h $MASTER_IP -u $USERNAME -p$PASSWORD -s -N -e "$sqlCmd"
			;;			
	esac
}

# upload data and log stuck running instances
no_data[0]=0
no_data_count=$(expr 0)

FOLDERS=/home/ec2-user/data/*
for folder in $FOLDERS
do
	ls -1 $folder/* > /dev/null 2>&1
	if [ "$?" = "0" ]
	then 
		FILES=$folder/*
		for file in $FILES
		do
			# upload cpu, mem, dio benchmark results to mysql and s3, then remove it
			filePathArray=(${file//// })
			filename=${filePathArray[${#filePathArray[@]}-1]}
			instanceDNS=${filePathArray[${#filePathArray[@]}-2]}
			s3cmd put $file $S3_PATH"data/"$instanceDNS"/"$filename

			data=`python get_data.py < $file`
			dataArray=(${data//,/ })
			insert_data $instanceDNS"_CPU" ${dataArray[0]} ${dataArray[1]}
			insert_data $instanceDNS"_MEMR" ${dataArray[2]} ${dataArray[3]}
			insert_data $instanceDNS"_MEMW" ${dataArray[4]} ${dataArray[5]}
			insert_data $instanceDNS"_DIO" ${dataArray[6]} ${dataArray[7]}
			rm $file
		done
	else
		no_data[$no_data_count]=$folder
		no_data_count=$(expr $no_data_count + 1)
	fi
done

# contruct healthy instance file (active - stuck running)
> $HEALTHY_NODES_FILE
while IFS='' read -r line || [[ -n $line ]]
do
	if [[ $(echo ${no_data[*]}) =~ .*$line.* ]]
	then 
		echo "WARNING instances $line did not finish test in time"
	else
		echo $line >> $HEALTHY_NODES_FILE
	fi
done < $ACTIVE_NODES_FILE

# command each healthy instance to run benchmarks again
NET_OUTFILE="$(date +%Y%m%d%H%M%S)"
sh wy20.sh -u ec2-user -k $PEM_FILE -i $HEALTHY_NODES_FILE -o $NET_OUTFILE
while IFS='' read -r line || [[ -n $line ]]
do
	sh command.sh "$line" $PUBLIC_HOSTNAME &
done < $HEALTHY_NODES_FILE

# upload network benchmark results
s3cmd put $NET_OUTFILE $S3_PATH"data/network/"$NET_OUTFILE
while IFS='' read -r line || [[ -n $line ]]
do
	dataArray=(${line//,/ })
	insert_data ${dataArray[1]}"_NET" ${dataArray[0]} ${dataArray[2]} ${dataArray[4]}
	insert_data ${dataArray[2]}"_NET" ${dataArray[0]} ${dataArray[1]} ${dataArray[4]}
done < $NET_OUTFILE
rm $NET_OUTFILE



