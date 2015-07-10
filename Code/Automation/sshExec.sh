#!/bin/bash
#param1: aggregator's public hostname
#param2: execute script name
#param3: Mysql IP

ssh -i is.pem -o StrictHostKeyChecking=no ec2-user@$1 'sh $2 $3 &'
if [ $? -eq 0 ];
then
	echo "OK"
else
    echo "NOK"
fi
