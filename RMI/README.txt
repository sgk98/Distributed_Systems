To run:
bash make.sh
In a new tab
java -Djava.rmi.server.hostname=<server_ip> -Djava.security.policy=server.policy Server

And again in a new tab
java -Djava.rmi.server.hostname=<client_ip> -Djava.security.policy=server.policy Client
