#!/bin/bash
#param1: node's public hostname
#param2: aggregator's public hostname

ssh -i is.pem -o "StrictHostKeyChecking no" ec2-user@$1 'sh ~/run.sh '$2' &'
exit
