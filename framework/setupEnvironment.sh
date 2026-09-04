# configuration from prometheus
sudo mkdir -p /data/maestro/prometheus/config
sudo cp ../ext-deps/prometheus/configs/prometheus.yml /data/maestro/prometheus/config
sudo cp ../ext-deps/prometheus/configs/policy_rules.yml /data/maestro/prometheus/config

# configuration from alertManager
sudo mkdir -p /data/maestro/alertManager/config
sudo cp ../ext-deps/alertManager/configuration.yml /data/maestro/alertManager/config

# configuration from grafana
sudo cp -r ../ext-deps/grafana/ /data/maestro/grafana
sudo chmod -R 777 /data/maestro/grafana

#sudo mkdir -p /data/maestro/grafana/data
#sudo mkdir -p /data/maestro/grafana/logs
#sudo chmod 717 /data/maestro/grafana/data
#sudo chmod 717 /data/maestro/grafana/logs

#configure cjdns
sudo apt-get install git build-essential nodejs python
sudo mkdir -p /opt/cjdns
sudo git clone https://github.com/cjdelisle/cjdns.git /opt/cjdns
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

echo $'\nInstalling Docker-Compose\n'
sudo curl -L "https://github.com/docker/compose/releases/download/1.28.5/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
sudo curl -L https://raw.githubusercontent.com/docker/compose/1.28.5/contrib/completion/bash/docker-compose -o /etc/bash_completion.d/docker-compose


echo $'\nInstalling Java\n'
sudo apt-get update
sudo apt-get upgrade -y
sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update
sudo apt-get install -y oracle-java8-set-default

sudo apt-get install maven

#TODO update the docker daemon for IPv6 enabled
# /etc/docker/daemon.json
{
    "ipv6": true,
    "fixed-cidr-v6": "2001:db8::/122"
}


#TODO IP tables configuration before executing docker-compose up
sudo iptables -t nat -A OUTPUT -p tcp -d <openstack-controller-1> --dport 5000 -o <interface> -j DNAT  --to-destination <nat-host>:<port>

sudo iptables -t nat -A OUTPUT -p tcp -d <openstack-controller-1> --dport 8774 -o <interface> -j DNAT  --to-destination <nat-host>:<port>

sudo iptables -t nat -A OUTPUT -p tcp -d <openstack-controller-1> --dport 9696 -o <interface> -j DNAT  --to-destination <nat-host>:<port>

sudo iptables -t nat -A OUTPUT -p tcp -d <openstack-controller-1> --dport 9292 -o <interface> -j DNAT  --to-destination <nat-host>:<port>

#======================================================================

sudo iptables -t nat -A OUTPUT -p tcp -d <openstack-controller-2> --dport 5000 -o <interface> -j DNAT  --to-destination <nat-host>:<port>

sudo iptables -t nat -A OUTPUT -p tcp -d <openstack-controller-2> --dport 8774 -o <interface> -j DNAT  --to-destination <nat-host>:<port>

sudo iptables -t nat -A OUTPUT -p tcp -d <openstack-controller-2> --dport 9696 -o <interface> -j DNAT  --to-destination <nat-host>:<port>

sudo iptables -t nat -A OUTPUT -p tcp -d <openstack-controller-2> --dport 9292 -o <interface> -j DNAT  --to-destination <nat-host>:<port>


sudo apt-get install iptables-persistent


