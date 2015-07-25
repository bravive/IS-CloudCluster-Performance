#!/usr/bin/env python 

import sys

cpu_timestamp = ''
cpu_totaltime = ''
memr_timestamp = ''
memr_ops_per_sec = ''
memw_timestamp = ''
memw_ops_per_sec = ''
dio_timestamp = ''
dio_req_per_sec = ''

line_number = 1
for line in sys.stdin:
    line = line.strip()
    if line_number == 2:
        cpu_timestamp = line
    if line_number == 17:
        cpu_totaltime = line.split()[-1][:-1]
    if line_number == 31:
        memr_timestamp = line
    if line_number == 47:
        memr_ops_per_sec = line.split()[3][1:]
    if line_number == 67:
        memw_timestamp = line
    if line_number == 83:
        memw_ops_per_sec = line.split()[3][1:]
    if line_number == 103:
        dio_timestamp = line
    if line_number == 125:
        dio_req_per_sec = line.split()[0]
        sys.stdout.write("%s,%s,%s,%s,%s,%s,%s,%s\n\n" % (cpu_timestamp, cpu_totaltime, memr_timestamp, memr_ops_per_sec, memw_timestamp, memw_ops_per_sec, dio_timestamp, dio_req_per_sec))
    line_number = line_number + 1

