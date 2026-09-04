#! /bin/bash
#Create folder which will contain the init scripts
sudo mkdir -p /opt/scripts
sudo rm /opt/agent.jar
sudo wget "$AGENT_URL" -P /opt/

#Cjdns configuration
cd /opt/cjdns
sudo ./cjdroute --genconf > /opt/cjdroute.conf

sed -i -e 's/^[[:blank:]]*\/\/.*//' -e '/^[[:blank:]]*$/d' -e '/\/\*/,/\*\//d' /opt/cjdroute.conf

param="{	\"$CJDNS_PEER_HOST:$CJDNS_PEER_PORT\": {
		\"login\": \"$CJDNS_PEER_LOGIN\",
		\"password\":\"$CJDNS_PEER_PASSWORD\",
		\"publicKey\":\"$CJDNS_PEER_PUBLIC_KEY\",
		\"peerName\":\"your-name-goes-here\"
	}
}"
echo $param > /opt/k.json

sudo  jq --argfile c /opt/k.json '.interfaces.UDPInterface[0].connectTo = $c' /opt/cjdroute.conf  > /opt/final.conf
rm /opt/k.json

sudo mv /opt/final.conf /etc/cjdns/cjdroute.conf
sudo /opt/cjdns/cjdroute < /etc/cjdns/cjdroute.conf

sudo wget "$CJDNS_INIT_SCRIPT_URL" -O cjdns.txt
sudo cp cjdns.txt /etc/init.d/cjdns
sudo chmod +x /etc/init.d/cjdns
sudo update-rc.d cjdns defaults
sudo service cjdns start

#Create consul as a service
echo "#!/bin/bash
mkdir -p /tmp/consul
privateIP=$(ip a s | grep -A8 -m1 POINTOPOINT | grep -m1 inet6 | cut -d' ' -f6 | cut -d'/' -f1)
masterIP=$CONSUL_MASTER_IP
sudo /opt/consul agent -join=\$masterIP -data-dir=/tmp/consul -bind=\$privateIP -advertise=\$privateIP -enable-script-checks=true -client=0.0.0.0 -config-dir=/etc/consul.d  -node=lambdaapp-lamba-sumfunc-sumfunc" | sudo tee /opt/scripts/consul-start.sh
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

#Start consul service
sudo systemctl daemon-reload
sudo systemctl enable consul.service
sudo systemctl start consul.service

#Configure netdata properly
sudo systemctl stop netdata.service
#sudo sed -i -e "s/\[backend\]/\[backend\]\nprefix=netdata:@GRAPHID:@GRAPHINSTANCEID:@COMPONENTNODEID/g" /opt/netdata/etc/netdata/netdata.conf
sudo sed -i -e "s/\[backend\]/\[backend\]\nprefix=netdata:lambdaapp:lamba:sumfunc/g" /opt/netdata/etc/netdata/netdata.conf
sudo systemctl start netdata.service

#Create agent as a service
echo -e "#!/bin/bash
java -jar /opt/agent.jar LambdaApp lamba SumFunc SumFunc false false false &>> /home/ubuntu/agent.log " | sudo tee /opt/scripts/agent-start.sh

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
