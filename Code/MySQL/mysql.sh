mysql -u root < schema.sql
exist="$(mysql -u root -s -N -e "SELECT EXISTS(SELECT 1 FROM mysql.user WHERE User = 'cloudMonitor' AND Host = 'localhost')")"
if [ $exist == 0 ]
then
        echo "NOT EXIST cloudMonitor@localhost, Create ONE."
        echo "CREATE USER 'cloudMonitor'@'localhost' IDENTIFIED BY 'cloudMonitor';" | mysql -u root
        echo "GRANT ALL ON *.* TO 'cloudMonitor'@'localhost';" | mysql -u root
elif [ $exist == 1 ]
then
        echo "EXIST cloudMonitor@localhost already."
fi

exist="$(mysql -u root -s -N  -e "SELECT EXISTS(SELECT 1 FROM mysql.user WHERE User = 'cloudMonitor' AND Host = '%')")"
if [ $exist == 0 ]
then
        echo "NOT EXIST cloudMonitor@%, Create ONE."
        echo "CREATE USER 'cloudMonitor'@'%' IDENTIFIED BY 'cloudMonitor';" | mysql -u root
        echo "GRANT ALL ON cloudMonitorDB.* TO 'cloudMonitor'@'%';" | mysql -u root
elif [ $exist == 1 ]
then
        echo "EXIST cloudMonitor@% already."
fi
