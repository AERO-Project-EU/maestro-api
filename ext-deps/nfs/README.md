##Development 
1.  Create `/data` on your machine 
```
sudo mkdir /data
```
2. Change permissions
```
sudo chmod -r 777 /data
```
3. Mount nfs path with local path
```
sudo mount <serverIP>:/mnt/srvXX/data  /data
```

##Production 
1.  Create `/home` on your machine 
```
sudo mkdir /data
```
2. Change permissions
```
sudo chmod -R 777 /data
```
3. Mount nfs path with local path
```
sudo mount <serverIP>:/mnt/srvXX/data /data
```
4. Restart NFS service
```
sudo systemctl restart nfs-kernel-server
```
5. Set on backend Service Volume the local path
```
  - /data:/data 
```