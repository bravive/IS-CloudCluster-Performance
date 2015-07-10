#!/bin/bash
#param1: master's public ip address

PUBLIC_HOSTNAME="$(curl http://169.254.169.254/latest/meta-data/public-hostname 2>/dev/null)"
ACTIVE_NODES_FILE="NodesDNS.info"

# mkdir for each instance and command initial run
while IFS='' read -r line || [[ -n $line ]]
do
    mkdir "/home/ec2-user/data/"$line
    sh command.sh "$line" $PUBLIC_HOSTNAME &
done < $ACTIVE_NODES_FILE

# set up cron job
crontab -l > mycron
echo "*/30 * * * * sh run_commands.sh "$1 >> mycron
crontab mycron
rm mycron
