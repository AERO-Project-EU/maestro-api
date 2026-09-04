#!/bin/bash

# app template id
GRAPHID="mygraph"
# deployed app id
APPID="myapp"
# deployed app component id
COMPONENTID="mycomponent"

#Netdata
if [ ! -f /home/core/start-netdata.sh ]; then
  # Downloading the Netdata
  curl https://my-netdata.io/kickstart-static64.sh > /home/core/start-netdata.sh
fi

# Start Netdata
echo "y" | sh /home/core/start-netdata.sh

sudo sed -i -e "s/\[backend\]/\[backend\]\nprefix=$GRAPHID:$APPID:$COMPONENTID/g" /opt/netdata/etc/netdata/netdata.conf
sudo systemctl restart netdata

#consul
if [ ! -f /home/core/start-consul.sh ]; then
  # Downloading the consul
  wget https://releases.hashicorp.com/consul/1.0.7/consul_1.0.7_linux_amd64.zip
  # Instal unzip and unzip the file
  unzip consul_1.0.7_linux_amd64.zip
  # Remove unnecessary unzip installation and consul zip file
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

  # create start sh file
  echo "#!/bin/bash
  sudo /opt/consul agent -retry-join=$CONSUL_MASTER_IP -data-dir=/tmp/consul -enable-script-checks=true -client=0.0.0.0 -config-dir=/etc/consul.d &" | sudo tee /home/core/start-consul.sh
fi

sudo sh /home/core/start-consul.sh

