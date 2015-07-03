#!/bin/bash

PUBLIC_HOSTNAME="$(curl http://169.254.169.254/latest/meta-data/public-hostname 2>/dev/null)"
USERNAME=cloudMonitor
PASSWORD=cloudMonitor

# mkdir for each instance
active_nodes_file="active_nodes"
while IFS='' read -r line || [[ -n $line ]]
do
    mkdir "$line"
done < $active_nodes_file

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
			# echo "TODO: upload it to s3 and mysql, then remove it"
			filePathArray=(${file//// })
			filename=${filePathArray[${#filePathArray[@]}-1]}
			instanceDNS=${filePathArray[${#filePathArray[@]}-2]}
			s3cmd put $filename "s3://CloudMonitor/data/"$instanceDNS"/"filename

			python get_data.py < $file > $file"_clean"
			IFS=";" read -r data < $file"_clean"
			dataArray=(${data//;/ })
			insert_data $instanceDNS"_CPU" ${dataArray[0]} ${dataArray[1]}
			insert_data $instanceDNS"_MEMR" ${dataArray[2]} ${dataArray[3]}
			insert_data $instanceDNS"_MEMW" ${dataArray[4]} ${dataArray[5]}
			insert_data $instanceDNS"_DIO" ${dataArray[6]} ${dataArray[7]}
	else
		no_data[$no_data_count]=$folder
		no_data_count=$(expr $no_data_count + 1)
	fi
done

# command instance to run benchmark again if not stuck running
while IFS='' read -r line || [[ -n $line ]]
do
	if [[ $(echo ${no_data[*]}) =~ .*$line.* ]]
	then 
		echo "WARNING instances $line did not finish test in time"
	else 
		sh command.sh "$line" $PUBLIC_HOSTNAME
	fi
done < $active_nodes_file

# helper function for inserting data into database
insert_data() {
	sqlCmd="USE cloudMonitorDB; "
	sqlCmd=$sqlCmd"CREATE TABLE IF NOT EXISTS \`"$1"\` ("
	sqlCmd=$sqlCmd"\`timestamp\` BIGINT UNSIGNED NOT NULL,"
	sqlCmd=$sqlCmd"\`value\` DOUBLE NOT NULL);"
	mysql -u $USERNAME -p$PASSWORD -s -N -e "$sqlCmd"

	sqlCmd="USE cloudMonitorDB; "
	sqlCmd=$sqlCmd"INSERT INTO \`"$1"\` VALUES ("$2", "$3");"
	mysql -u $USERNAME -p$PASSWORD -s -N -e "$sqlCmd"
}
