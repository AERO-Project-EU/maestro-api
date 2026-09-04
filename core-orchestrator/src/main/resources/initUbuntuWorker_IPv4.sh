#! /bin/bash

# Add hostname at the /etc/hosts of the VM
varHost=$(hostname)
echo "127.0.0.1 $varHost" | sudo tee -a /etc/hosts

# Optionally set a password for the worker user (worker.password)
@WORKER_PASSWORD

# Create folder which will contain the init scripts
sudo mkdir -p /opt/scripts
sudo rm -r /opt/agent.jar
sudo mkdir -p /opt

# Configure Agent download URL
@AGENT_FETCH_URL

# Configure Network Configuration
@NETWORK_CONFIGURATION

# Add authorized ssh keys
@SSH_KEYS

# Configure IDS if needed
@IDS_CONFIG

# Find what this does
#sudo ethtool -K ens3 rx off tx off

# Cjdns Installation and configuration
cd /opt/cjdns
sudo ./cjdroute --genconf > /opt/cjdroute.conf
sed -i -e 's/^[[:blank:]]*\/\/.*//' -e '/^[[:blank:]]*$/d' -e '/\/\*/,/\*\//d' /opt/cjdroute.conf
param="{	\"@RELAYIP:@RELAYPORT\": {
		\"login\": \"@RELAYLOGIN\",
		\"password\":\"@RELAYPASSWORD\",
		\"publicKey\":\"@RELAYPUBLICKEY\",
		\"peerName\":\"@RELAYPEERNAME\"
	}
}"
echo $param > /opt/k.json
sudo  jq --argfile c /opt/k.json '.interfaces.UDPInterface[0].connectTo = $c' /opt/cjdroute.conf  > /opt/final.conf
rm /opt/k.json
sudo mv /opt/final.conf /etc/cjdns/cjdroute.conf
sudo /opt/cjdns/cjdroute < /etc/cjdns/cjdroute.conf

# Install and enable CJDNS as a service
@CJDNS_SERVICE_INSTALL

# Create consul as a service
echo "#!/bin/bash
mkdir -p /tmp/consul
pubIP=$(curl ipinfo.io/ip)
#privateIP=$(ip route get 8.8.8.8 | awk '{print $NF; exit}')
#privateIP=$(ip a s | grep -A8 -m1 MULTICAST | grep -m1 inet | cut -d' ' -f6 | cut -d'/' -f1)
privateIP=$(ip a s | grep -A8 -m1 POINTOPOINT | grep -m1 inet6 | cut -d' ' -f6 | cut -d'/' -f1)
masterIP=@MASTERIP
sudo /opt/consul agent -join=\$masterIP -datacenter=@CLUSTER_NAME -data-dir=/tmp/consul -bind=\$privateIP -advertise=\$privateIP -enable-script-checks=true -client=0.0.0.0 -config-dir=/etc/consul.d -node=@GRAPHID-@GRAPHINSTANCEID-@COMPONENTNODEID-@COMPONENTNODEINSTANCEID" | sudo tee /opt/scripts/consul-start.sh
sudo chmod +x /opt/scripts/consul-start.sh

echo -e '#!/bin/bash\nps -e | grep consul | awk "{print $1}" | xargs kill -9 ' | sudo tee /opt/scripts/consul-stop.sh
sudo chmod +x /opt/scripts/consul-stop.sh

echo -e '[Unit]
Description=consul
After=syslog.target network.target

[Service]
User=root
ExecStart=/opt/scripts/consul-start.sh
#ExecStop=/opt/scripts/consul-stop.sh
Restart=on-failure
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target' | sudo tee /etc/systemd/system/consul.service

# Start consul service
sudo systemctl daemon-reload
sudo systemctl enable consul.service
sudo systemctl start consul.service

# Configure Netdata properly
sudo systemctl stop netdata.service
sudo echo "@NETDATA_CONFIG" > /opt/netdata.conf

# Create Netdata plugin configuration file
@PLUGIN_CONFIG

sudo mv /opt/netdata.conf /opt/netdata/netdata-configs/
#sudo sed -i -e "s/\[backend\]/\[backend\]\nprefix=netdata:@GRAPHID:@GRAPHINSTANCEID:@COMPONENTNODEID/g" /opt/netdata/etc/netdata/netdata.conf
sudo systemctl start netdata.service

# Create Maestro agent as a service
echo -e "#!/bin/bash
java -jar /opt/agent.jar @GRAPHID @GRAPHINSTANCEID @COMPONENTNODEID @COMPONENTNODEINSTANCEID &>> /home/ubuntu/agent.log " | sudo tee /opt/scripts/agent-start.sh

sudo chmod +x /opt/scripts/agent-start.sh
#echo -e '#!/bin/bash\nps -e | grep agent | awk "{print $1}" | xargs kill -9 ' | sudo tee /opt/scripts/agent-stop.sh
#sudo chown ubuntu:ubuntu /opt/scripts/agent-stop.sh
sudo chmod +x /opt/scripts/agent-stop.sh

echo -e '[Unit]
Description=agent
After=syslog.target network.target

[Service]
User=root
ExecStart=/opt/scripts/agent-start.sh
#ExecStop=/opt/scripts/agent-stop.sh
Restart=on-failure
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target' | sudo tee /etc/systemd/system/agent.service

#Start agent service
sudo systemctl daemon-reload
sudo systemctl enable agent.service
sudo systemctl start agent.service
