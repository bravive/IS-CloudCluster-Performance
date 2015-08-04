#!/usr/bin/env python 

import sys

cpu_timestamp = None
cpu_totaltime = None
memr_timestamp = None
memr_ops_per_sec = None
memw_timestamp = None
memw_ops_per_sec = None
dio_timestamp = None
dio_req_per_sec = None

line_number = 1
while True:
    line = sys.stdin.readline()
    line = line.strip()

    if line_number == 2:
        if line.startswith("20"):
            cpu_timestamp = line
        while cpu_timestamp == None:
            line = sys.stdin.readline()
            line_number = line_number + 1
            if line.startswith("20"):
                cpu_timestamp = line
    if line_number == 17:
        if "total time:" in line:
            cpu_totaltime = line.split()[-1][:-1]
        while cpu_totaltime == None:
            line = sys.stdin.readline()
            line_number = line_number + 1
            if "total time:" in line:
                cpu_totaltime = line.split()[-1][:-1]

    if line_number == 31:
        if line.startswith("20"):
            memr_timestamp = line.split()[-1][:-1]
        while memr_timestamp == None:
            line = sys.stdin.readline()
            line_number = line_number + 1
            if line.startswith("20"):
                memr_timestamp = line.split()[-1][:-1]
    if line_number == 47:
        if "Operations performed:" in line:
            memr_ops_per_sec = line.split()[3][1:]
        while memr_ops_per_sec == None:
            line = sys.stdin.readline()
            line_number = line_number + 1
            if "Operations performed:" in line:
                memr_ops_per_sec = line.split()[3][1:]

    if line_number == 67:
        if line.startswith("20"):
            memw_timestamp = line.split()[-1][:-1]
        while memw_timestamp == None:
            line = sys.stdin.readline()
            line_number = line_number + 1
            if line.startswith("20"):
                memw_timestamp = line.split()[-1][:-1]
    if line_number == 83:
        if "Operations performed:" in line:
            memw_ops_per_sec = line.split()[3][1:]
        while memw_ops_per_sec == None:
            line = sys.stdin.readline()
            line_number = line_number + 1
            if "Operations performed:" in line:
                memw_ops_per_sec = line.split()[3][1:]

    if line_number == 103:
        if line.startswith("20"):
            dio_timestamp = line.split()[-1][:-1]
        while dio_timestamp == None:
            line = sys.stdin.readline()
            line_number = line_number + 1
            if line.startswith("20"):
                dio_timestamp = line.split()[-1][:-1]
    if line_number == 125:
        if "Requests/sec" in line:
            dio_req_per_sec = ine.split()[0]
        while dio_req_per_sec == None:
            line = sys.stdin.readline()
            line_number = line_number + 1
            if "Requests/sec" in line:
                dio_req_per_sec = line.split()[0]

        sys.stdout.write("%s,%s,%s,%s,%s,%s,%s,%s\n\n" % (cpu_timestamp, cpu_totaltime, memr_timestamp, memr_ops_per_sec, memw_timestamp, memw_ops_per_sec, dio_timestamp, dio_req_per_sec))
        break

    line_number = line_number + 1

