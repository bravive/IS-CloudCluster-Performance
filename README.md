# IS-CloudCluster-Performance

This project is aimed to automate the monitoring and performance measuring of cloud cluster based on AWS EC2 instances. There will be a dashboard interface for user to look up their instance status and all benchmark indicators. The web port is 8080 and use the master DNS.

The monitoring system is event driven for communication and uses Amazon S3 to backup data in case master node is crashed accidentally.

|=======================Master Node========================|
||--------------------Manager Console----------------------|
||---Automation---||---DashBoard---||---MySQL Databases---||
||Agg Monitor    |					   |
||Cluster Monitor|					   |
||Coordinator    |					   |
|__________________________________________________________|
	|					/\
	|					|
	|					|
	\/					|
|=====================Aggregator Node======================|
||---Contab Exec--||---S3 Backup--||---MySQL Maintainence--|
|__________________________________________________________|
   	|	/\
	|	|
	|	|
	\/	|
|==============================Cluster Nodes=================================|
||--CPU BenchMark--||--Mem Read--||--Mem Write--||--DISK IO--||--Network IO--|
||(SysBench)	    (SysBench)	  (SysBench)	 (SysBench)   (WY20)	     |
|____________________________________________________________________________|



