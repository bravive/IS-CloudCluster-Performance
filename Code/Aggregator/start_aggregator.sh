#!/bin/bash
#param1: master's public ip address
#param2: s3 backup path

PUBLIC_HOSTNAME="$(curl http://169.254.169.254/latest/meta-data/public-hostname 2>/dev/null)"
ACTIVE_NODES_FILE="NodesDNS.info"
MASTER_IP=$1
DB_NAME="cloudMonitorDB"
USERNAME=cloudMonitor
PASSWORD=cloudMonitor
S3_PATH=$2
PEM_FILE="is.pem"

# for testing purpose: update scripts
# s3cmd get --force s3://mengyegong/CloudClusterMonitor/src/aggregator/start_aggregator.sh
# s3cmd get --force s3://mengyegong/CloudClusterMonitor/src/aggregator/run_commands.sh
# s3cmd get --force s3://mengyegong/CloudClusterMonitor/src/aggregator/command.sh
# s3cmd get --force s3://mengyegong/CloudClusterMonitor/src/aggregator/get_data.py
# s3cmd get --force s3://mengyegong/CloudClusterMonitor/src/aggregator/wy20.sh

# helper function for creating database tables
# param1: tableName
create_table() {
    sqlCmd="USE "$DB_NAME"; "
    sqlCmd=$sqlCmd"DROP TABLE IF EXISTS \`"$1"\`; "
    mysql -h $MASTER_IP -u $USERNAME -p$PASSWORD -s -N -e "$sqlCmd"

    case $1 in
        *CPU | *MEMR | *MEMW | *DIO)
            sqlCmd="USE "$DB_NAME"; "
            sqlCmd=$sqlCmd"CREATE TABLE \`"$1"\` ("
            sqlCmd=$sqlCmd"\`timestamp\` BIGINT UNSIGNED NOT NULL,"
            sqlCmd=$sqlCmd"\`value\` DOUBLE NOT NULL);"
            mysql -h $MASTER_IP -u $USERNAME -p$PASSWORD -s -N -e "$sqlCmd"
            ;;
        *NET)
            sqlCmd="USE "$DB_NAME"; "
            sqlCmd=$sqlCmd"CREATE TABLE \`"$1"\` ("
            sqlCmd=$sqlCmd"\`timestamp\` BIGINT UNSIGNED NOT NULL,"
            sqlCmd=$sqlCmd"\`reference\` VARCHAR(50) NOT NULL,"
            sqlCmd=$sqlCmd"\`value\` DOUBLE NOT NULL);"
            mysql -h $MASTER_IP -u $USERNAME -p$PASSWORD -s -N -e "$sqlCmd"
            ;;          
    esac
}

# helper function for inserting initial network data into database
# param1: tableName
# other params: columndValues
insert_network_data() {
    sqlCmd="USE "$DB_NAME"; "
    sqlCmd=$sqlCmd"INSERT INTO \`"$1"\` VALUES ("$2", \""$3"\", "$4");"
    mysql -h $MASTER_IP -u $USERNAME -p$PASSWORD -s -N -e "$sqlCmd"
}

# for each instance, mkdir, create DB tables, scp pem file, and command initial run
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
    # scp pem file
    while true
    do
    	scp -i $PEM_FILE -o "StrictHostKeyChecking no" -o "ConnectTimeout 1" $PEM_FILE ec2-user@$line:/home/ec2-user/sysbench-0.4.12/sysbench/$PEM_FILE
    	if [ $? -eq 0 ]
    	then
    		break
    	else
    		sleep 5
    	fi
    done
done < $ACTIVE_NODES_FILE

# command initial run
NET_OUTFILE="$(date +%Y%m%d%H%M%S)"
sh wy20.sh -u ec2-user -k $PEM_FILE -i $ACTIVE_NODES_FILE -o $NET_OUTFILE
while IFS='' read -r line || [[ -n $line ]]
do
    sh command.sh "$line" $PUBLIC_HOSTNAME &
done < $ACTIVE_NODES_FILE
# upload intial network data
s3cmd put $NET_OUTFILE $S3_PATH"data/network/"$NET_OUTFILE
while IFS='' read -r line || [[ -n $line ]]
do
    dataArray=(${line//,/ })
    insert_network_data ${dataArray[1]}"_NET" ${dataArray[0]} ${dataArray[2]} ${dataArray[4]}
    insert_network_data ${dataArray[2]}"_NET" ${dataArray[0]} ${dataArray[1]} ${dataArray[4]}
done < $NET_OUTFILE
rm $NET_OUTFILE

# set up cron job
crontab -l > mycron
echo "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" >> mycron
echo "*/10 * * * * sh run_commands.sh "$1" "$2 >> mycron
crontab mycron
rm mycron
