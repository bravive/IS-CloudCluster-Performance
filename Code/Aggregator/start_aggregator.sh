#!/bin/bash
#param1: master's public ip address

crontab -l > mycron
echo "*/30 * * * * sh run_commands.sh "$1 >> mycron
crontab mycron
rm mycron
