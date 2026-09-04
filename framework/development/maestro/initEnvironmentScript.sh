#!/usr/bin/env bash

echo $'\nInstalling Docker\n'
sudo apt-get remove docker docker-engine docker.io -y
sudo apt-get update -y
sudo apt-get install apt-transport-https ca-certificates curl software-properties-common -y
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt-get update -y
sudo apt-get install docker-ce -y
sudo groupadd docker
sudo usermod -aG docker ${USER}

echo $'\nInstalling Docker-Compose\n'
sudo curl -L "https://github.com/docker/compose/releases/download/1.27.4/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
sudo apt-get install -y bash-completion
sudo curl -L https://raw.githubusercontent.com/docker/compose/1.27.4/contrib/completion/bash/docker-compose -o /etc/bash_completion.d/docker-compose

echo $'\nConfiguring docker daemon json for IPv6\n'
echo "{
    \"ipv6\": true,
    \"fixed-cidr-v6\": \"2001:db8::/122\"
}" | sudo tee /etc/docker/daemon.json

echo $'\nInstalling Java\n'
sudo apt-get install -y openjdk-17-jdk-headless

echo $'\nInstalling Maven\n'
sudo apt-get  install -y maven

echo $'\nInstalling CJDNS\n'
sudo apt-get install -y git build-essential nodejs python
sudo wget https://github.com/cjdelisle/cjdns/archive/cjdns-v20.6.tar.gz  -O /opt/cjdns.tar.gz
sudo tar -xzvf /opt/cjdns.tar.gz -C /opt/
sudo rm /opt/cjdns.tar.gz
sudo mv /opt/cjdns-cjdns-v20.6/ /opt/cjdns
cd /opt/cjdns
sudo ./do
sudo mkdir -p /etc/cjdns
sudo chmod 755 /etc/cjdns
./cjdroute --genconf > ~/cjdroute.conf
sudo mv ~/cjdroute.conf /etc/cjdns/cjdroute.conf
sudo /opt/cjdns/cjdroute < /etc/cjdns/cjdroute.conf

echo $'\Configuring CJDNS as a service\n'
sudo wget http://www.jenovarain.com/blog/wp-content/uploads/2016/05/cjdns.txt
sudo cp cjdns.txt /etc/init.d/cjdns
sudo chmod +x /etc/init.d/cjdns
sudo update-rc.d cjdns defaults
sudo systemctl start cjdns

echo $'\nInstalling Netdata\n'
sudo curl https://my-netdata.io/kickstart-static64.sh > /tmp/kickstart-static64.sh
sudo echo "y" | sh /tmp/kickstart-static64.sh
sudo cp ../../../ext-deps/netdata/orchestratorMetric/netdata.conf /opt/netdata/netdata-configs/
sudo cp ../../../ext-deps/netdata/orchestratorMetric/orchestratorCollector.conf /opt/netdata/netdata-configs/python.d/
sudo cp ../../../ext-deps/netdata/orchestratorMetric/orchestratorCollector.chart.py /opt/netdata/netdata-plugins/python.d/
sudo service netdata restart
