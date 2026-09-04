#! /bin/bash

boolean=false
interfaces=()
for i in {3..10}
do
    ethName="ens$i"
    state=""
    state=$(ip a s $ethName | grep "state DOWN")
    if [ ! -z "$state" ]
    then
        boolean=true
        interfaces=("${interfaces[@]}" "$ethName")
        mac=$(ifconfig $ethName | grep -o -E '([[:xdigit:]]{1,2}:){5}[[:xdigit:]]{1,2}')

sudo echo "        $ethName:\n" +
"            dhcp4: true\n" +
"            match:\n" +
"                macaddress: $mac\n" +
"            set-name: $ethName" | sudo tee -a /etc/netplan/50-cloud-init.yaml
    fi
done
if $boolean
then
    sudo netplan generate
    sudo netplan apply
    for ethName in "${interfaces[@]}"
    do
        privateIP=""
        while true
        do
            # privateIP=\\$(ip a s ens3| grep -A8 -m1 MULTICAST | grep -m1 inet | cut -d' ' -f6 | cut -d'/' -f1 | cut -d'.' --fields=1,2,3)\n" +

            check=$(ip a s $ethName| grep -A8 -m1 MULTICAST | grep -m1 inet | cut -d' ' -f6 | cut -d'/' -f1 | cut -d'.' --fields=1)
            size=${#check}
            echo $size
            if ([ $size -gt 3 ] || [ $size -eq 0 ])
            then
                continue;
            fi
            privateIP=$(ip a s $ethName| grep -A8 -m1 MULTICAST | grep -m1 inet | cut -d' ' -f6 | cut -d'/' -f1 | cut -d'.' --fields=1,2,3)
            if [ ! -z "$privateIP" ]
            then
                break;
            fi
        done
#        privateIP="$privateIP.1"
        echo $privateIP
        sudo ip route del default via $privateIP.1
        sudo ip route del default via $privateIP.254
        fileName="10-netplan-$ethName.network"

sudo echo "[Match]\n" +
"Name=$ethName\n" +
"\n" +
"[Network]\n" +
"DHCP=ipv4\n" +
"\n" +
"[DHCP]\n" +
"UseMTU=true\n" +
"RouteMetric=200" | sudo tee /etc/systemd/network/$fileName\n

    done
fi