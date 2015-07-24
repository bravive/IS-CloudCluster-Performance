#!/bin/bash
#param1: node's public hostname
#param2: aggregator's public hostname

PEM_FILE="is.pem"

ssh -i $PEM_FILE -o "StrictHostKeyChecking no" ec2-user@$1 'sh ~/sysbench-0.4.12/sysbench/run.sh '$2' &'
exit
