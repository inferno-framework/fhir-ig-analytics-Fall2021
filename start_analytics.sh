SERVER_PORT=8081
CLIENT_PORT=4200
HOST=$(curl ifconfig.co)

usage()
{
   echo "This script configures the client and server for FHIR analytics application."
   echo
   echo "Syntax: start_application -s <ServerPort>"
   echo "options:"
   echo "-s     Provide a server port number more than 1024 and not used by any other application." 
   echo "       UI connects to the server on this port. Default port is 8081 if not specified."
   echo "       http://$HOST:<ServerPort>/srvc/fhirAnalytics/compare?identifier=123456"
   echo
}

while getopts s: flag
do
    case "${flag}" in
        s) SERVER_PORT=${OPTARG};;
	*)
         usage
         exit;;
    esac
done

echo "INFO: Configuring the application to use IP: ${HOST}, Client_Port: ${CLIENT_PORT}, Server_Port: ${SERVER_PORT}"

if [ "$SERVER_PORT" -le "1024" ]; then
	echo -n "ERROR: The server port must not be less than 1024\n"
	exit
fi

if [ "$CLIENT_PORT" -le "1024" ]; then
        echo -n "ERROR: The client port must not be less than 1024\n"
        exit
fi

if [ ! -e /usr/bin/docker ]; then
        echo "Installing prerequisite docker packages..."
	sudo apt-get --assume-yes update
	sudo apt-get --assume-yes install ca-certificates curl gnupg lsb-release
	curl -fsSL https://download.docker.com/linux/debian/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
	echo   "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
        sudo apt-get --assume-yes update
        sudo apt-get --assume-yes install docker-ce docker-ce-cli containerd.io
        sudo apt-get --assume-yes update
        sudo apt-get --assume-yes install docker
        echo ""
        echo ""
fi

if [ ! -e /usr/bin/npm ]; then
        echo "Installing prerequisite npm packages..."
        sudo apt-get --assume-yes update
        sudo apt-get --assume-yes install npm
        sudo npm cache clean -f 2>/dev/null
        sudo npm install -g n 2>/dev/null
        sudo n stable 
        echo ""
        echo ""
fi

if [[ ! $(groups $USER | grep -i docker) ]]; then
      echo 'Adding $USER to docker group';
      sudo usermod -aG docker $USER > /dev/null 2>&1;
      echo "First time docker user setup, please exit, open new shell, and run again"
      exit
fi

echo "Cleaning up old fhir-ig-analytics angular runtimes"
for pid in $(ps -ef | grep "ng serve" | grep -v grep | awk '{print $2}'); do echo "Killing old running angular server $pid"; kill -9 $pid; done
echo ""
echo "Cleaning up old fhir-ig-analytics containers and images"
for cid in $(docker ps -a | grep fhir-ig-analytics | awk '{print $1}'); do echo 'Stopping running container'; docker stop $cid; echo 'Removing running container'; docker rm $cid; done
for iid in $(docker images | grep fhir-ig-analytics | awk '{print $3}'); do echo 'Removing old images'; docker rmi $iid; done
echo ""
echo "Downloading backend image from docker repo and starting it on port $SERVER_PORT"
docker pull mlee6/fhir-ig-analytics:latest
docker run --name fhir-ig-analytics -p 8081:8081 -d --restart unless-stopped "mlee6/fhir-ig-analytics:latest"
echo ""
echo "Downloading frontend image from docker repo and starting it on port $SERVER_PORT"
docker pull mlee6/fhir-ig-analytics:ui-latest
docker run --name fhir-ig-analytics-ui -p $CLIENT_PORT:4200 -d --restart unless-stopped "mlee6/fhir-ig-analytics:ui-latest"


#cd fhir-analytics-ui

#echo ""
#echo "Updating configuration files"
#sed -i "s/localhost:8081/${HOST}:${SERVER_PORT}/g" ./src/app/api.service.ts
#sed -i "s/--port 4200/--port ${CLIENT_PORT}/g" package.json

#echo "Starting Web Server -- Press Enter when Done"
#npm install && npm start &

echo "Docker process status:"
echo $(docker ps -a | grep fhir-ig-analytics)

