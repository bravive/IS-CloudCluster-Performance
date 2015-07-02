CREATE DATABASE IF NOT EXISTS cloudMonitorDB;
USE cloudMonitorDB;
DROP TABLE IF EXISTS `instanceInfo`;
CREATE TABLE `instanceInfo`
(
        `instanceDNS` varchar(100) NOT NULL,
        `status` varchar(20)  NOT NULL,
        PRIMARY KEY (`instanceDNS`)
);
