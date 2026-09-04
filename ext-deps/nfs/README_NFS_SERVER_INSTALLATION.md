## NFS server installation 

On nfs server create the following path (on VM's volume)
```
/mnt/srvXX/data
```

On the server at path `/etc/exports` add the following line 
```
/mnt/srvXX/data  *(rw,nohide,insecure,sync,no_root_squash,no_subtree_check)
```

