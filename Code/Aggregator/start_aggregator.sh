#!/bin/bash
#param1: master's public ip address
#param2: s3 backup path

PUBLIC_HOSTNAME="$(curl http://169.254.169.254/latest/meta-data/public-hostname 2>/dev/null)"
ACTIVE_NODES_FILE="NodesDNS.info"
MASTER_IP=$1
DB_NAME="cloudMonitorDB"
USERNAME=cloudMonitor
PASSWORD=cloudMonitor
PEM_FILE="is.pem"

# for testing purpose: update scripts
s3cmd get s3://mengyegong/CloudClusterMonitor/src/aggregator/start_aggregator.sh
s3cmd get s3://mengyegong/CloudClusterMonitor/src/aggregator/run_commands.sh
s3cmd get s3://mengyegong/CloudClusterMonitor/src/aggregator/command.sh
s3cmd get s3://mengyegong/CloudClusterMonitor/src/aggregator/get_data.py

# helper function for creating database tables
# param1: tableName
create_table() {
    sqlCmd="USE "$DB_NAME"; "
    sqlCmd=$sqlCmd"DROP TABLE IF EXISTS \`"$1"\`; "
    mysql -h $MASTER_IP -u $USERNAME -p$PASSWORD -s -N -e "$sqlCmd"

    sqlCmd="USE "$DB_NAME"; "
    sqlCmd=$sqlCmd"CREATE TABLE IF NOT EXISTS \`"$1"\` ("
    sqlCmd=$sqlCmd"\`timestamp\` BIGINT UNSIGNED NOT NULL,"
    sqlCmd=$sqlCmd"\`value\` DOUBLE NOT NULL);"
    mysql -h $MASTER_IP -u $USERNAME -p$PASSWORD -s -N -e "$sqlCmd"
}

# for each instance, mkdir, create DB tables, scp files, and command initial run
while IFS='' read -r line || [[ -n $line ]]
do
    # mkdir
    mkdir "/home/ec2-user/data/"$line
    # create tables
    create_table $line"_CPU"
    create_table $line"_MEMR"
    create_table $line"_MEMW"
    create_table $line"_DIO"
    create_table $line"_NET"
    # scp files
    while true
    do
    	scp -i $PEM_FILE -o "StrictHostKeyChecking no" -o "ConnectTimeout 1" $PEM_FILE ec2-user@$line:/home/ec2-user/sysbench/sysbench/.
    	if [ $? -eq 0 ]
    	then
    		break
    	else
    		sleep 5
    	fi
    done
    while true
    do
        scp -i $PEM_FILE -o "StrictHostKeyChecking no" -o "ConnectTimeout 1" $ACTIVE_NODES_FILE ec2-user@$line:/home/ec2-user/sysbench/sysbench/.
        if [ $? -eq 0 ]
        then
            break
        else
            sleep 5
        fi
    done
    # command initial run
    sh command.sh "$line" $PUBLIC_HOSTNAME &
done < $ACTIVE_NODES_FILE

# set up cron job
crontab -l > mycron
echo "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" >> mycron
echo "*/10 * * * * sh run_commands.sh "$1" "$2 >> mycron
crontab mycron
rm mycron
