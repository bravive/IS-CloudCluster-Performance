#!/bin/bash
#param1: aggregator's public hostname

FILENAME="$(date +%Y%m%d_%H%M%S)"
PUBLIC_HOSTNAME="$(curl http://169.254.169.254/latest/meta-data/public-hostname 2>/dev/null)"
# FILENAME+=$PUBLIC_HOSTNAME

echo "----------------CPU----------------" >> $FILENAME
echo $(date +%Y%m%d%H%M%S) >> $FILENAME
./sysbench-0.4.12/sysbench/sysbench --test=cpu --cpu-max-prime=20000 --num-threads=2 run >> $FILENAME
echo "----------------MEMR----------------" >> $FILENAME
echo $(date +%Y%m%d%H%M%S) >> $FILENAME
./sysbench-0.4.12/sysbench/sysbench --test=memory --num-threads=2 --memory-oper=read run >> $FILENAME
echo "----------------MEMW----------------" >> $FILENAME
echo $(date +%Y%m%d%H%M%S) >> $FILENAME
./sysbench-0.4.12/sysbench/sysbench --test=memory --num-threads=2 --memory-oper=write run >> $FILENAME
echo "----------------DIO----------------" >> $FILENAME
echo $(date +%Y%m%d%H%M%S) >> $FILENAME
./sysbench-0.4.12/sysbench/sysbench --test=fileio --file-total-size=50G --file-test-mode=rndrw --max-time=300 --max-requests=0 run >> $FILENAME

scp -i is.pem $FILENAME ec2-user@$1:/home/ec2-user/data/$PUBLIC_HOSTNAME/.
rm $FILENAME
