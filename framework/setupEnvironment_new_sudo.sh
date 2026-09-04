#!/usr/bin/env bash

#TODO update the docker daemon for IPv6 enabled
# /etc/docker/daemon.json
{
    "ipv6": true,
    "fixed-cidr-v6": "2001:db8::/122"
}


#echo $'\nInstalling Java\n'
#-----------------------------------------------------------------------------configure java
#sudo apt-get update
#sudo apt-get upgrade -y
#sudo add-apt-repository ppa:webupd8team/java -y
#sudo apt-get update
#sudo apt-get install -y oracle-java8-set-default
#-----------------------------------------------------------------------------configure java


#echo $'\nInstalling Maven\n'
#-----------------------------------------------------------------------------configure maven
#sudo apt-get install -y maven
#-----------------------------------------------------------------------------configure maven


#echo $'\nInstalling git\n'
#-----------------------------------------------------------------------------configure git
#sudo add-apt-repository ppa:git-core/ppa -y
#sudo apt-get update
#sudo apt-get install -y git
#-----------------------------------------------------------------------------configure git


#echo $'\nInstalling cjdns\n'
#-----------------------------------------------------------------------------configure cjdns
sudo apt-get install git build-essential nodejs python
sudo mkdir -p /opt/cjdns
sudo git clone --branch cjdns-v20.6 https://github.com/cjdelisle/cjdns.git /opt/cjdns
cd /opt/cjdns
sudo ./do
sudo mkdir -p /etc/cjdns
sudo chmod 755 /etc/cjdns
./cjdroute --genconf > ~/cjdroute.conf
sudo mv ~/cjdroute.conf /etc/cjdns/cjdroute.conf
sudo /opt/cjdns/cjdroute < /etc/cjdns/cjdroute.conf

sudo wget http://www.jenovarain.com/blog/wp-content/uploads/2016/05/cjdns.txt
sudo cp cjdns.txt /etc/init.d/cjdns
sudo chmod +x /etc/init.d/cjdns
sudo update-rc.d cjdns defaults
sudo service cjdns start
#-----------------------------------------------------------------------------configure cjdns


#echo $'\nInstalling Netdata Slave\n'
#-----------------------------------------------------------------------------configure Netdata
sudo curl https://my-netdata.io/kickstart-static64.sh > /tmp/kickstart-static64.sh
sudo echo "y" | sh /tmp/kickstart-static64.sh
sudo cp ../ext-deps/netdata/orchestratorMetric/netdata.conf /opt/netdata/netdata-configs/
sudo cp ../ext-deps/netdata/orchestratorMetric/orchestratorCollector.conf /opt/netdata/netdata-configs/python.d/
sudo cp ../ext-deps/netdata/orchestratorMetric/orchestratorCollector.chart.py /opt/netdata/netdata-plugins/python.d/
sudo service netdata restart

