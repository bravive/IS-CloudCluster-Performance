#!/bin/bash
#param1: aggregator's public hostname
#param2: filename containing all node's public hostname
scp -i is.pem -o StrictHostKeyChecking=no -o ConnectTimeout=1 $2 ec2-user@$1:
if [ $? -eq 0 ];
then
	echo "OK"
else
    echo "NOK"
fi
