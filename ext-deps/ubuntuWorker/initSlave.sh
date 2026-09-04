#! /bin/bash
echo $'\nUpdating the system...\n'
# updating the system
sudo apt-get update

echo $'\nInstalling Docker\n'
sudo apt-get remove docker docker-engine docker.io -y
sudo apt-get update -y
sudo apt-get install apt-transport-https ca-certificates curl software-properties-common -y
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt-get update -y
sudo apt-get install docker-ce -y
sudo groupadd docker
sudo usermod -aG docker ubuntu


echo $'\nInstalling Netdata Slave\n'
curl https://my-netdata.io/kickstart-static64.sh > /tmp/kickstart-static64.sh
echo "y" | sh /tmp/kickstart-static64.sh


echo $'\nInstalling Consule Agent\n'
# Downloading the consul
wget https://releases.hashicorp.com/consul/1.0.7/consul_1.0.7_linux_amd64.zip
# Instal unzip and unzip the file
sudo apt-get install unzip -y
unzip consul_1.0.7_linux_amd64.zip
# Remove unnecessary unzip installation and consul zip file
sudo apt-get remove unzip -y
rm consul_1.0.7_linux_amd64.zip
# Move consul to /opt folder and make /etc/consul.d folder
sudo mv consul /opt/
sudo mkdir /etc/consul.d

# register the netdata service with health check to consul config
echo '{
"service":
{
  "ID": "netdataID",
  "Name": "netdata",
  "Tags": [
    "metrics"
  ],
  "Address": "localhost",
  "Port": 19999,
  "EnableTagOverride": false,
  "Check": {
    "DeregisterCriticalServiceAfter": "30m",
    "HTTP": "http://localhost:19999/api/v1/allmetrics",
    "Interval": "5s"
  }
}
}' | sudo tee /etc/consul.d/netdata.json

echo "#!/bin/bash
### BEGIN INIT INFO
# Provides:          consul_master
# Required-Start:    $all
# Required-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:
# Short-Description: Run /etc/init.d/consul_master.sh if it exist
### END INIT INFO
mkdir /tmp/consul
pubIP=$(curl ipinfo.io/ip)
privateIP=$(ip route get 8.8.8.8 | awk '{print \$NF; exit}')
masterIP=$CONSUL_MASTER_IP
sudo /opt/consul agent -join=\$masterIP -data-dir=/tmp/consul -bind=\$privateIP -advertise=\$pubIP -enable-script-checks=true -client=0.0.0.0 -config-dir=/etc/consul.d &" | sudo tee /etc/init.d/init_slave.sh

sudo chmod a+x /etc/init.d/init_slave.sh
sudo chmod 777 /etc/init.d/init_slave.sh
sudo update-rc.d init_slave.sh defaults

# remove uneeded packages
sudo apt-get clean
sudo rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

echo $'\nRebooting the system\n'
sudo reboot
