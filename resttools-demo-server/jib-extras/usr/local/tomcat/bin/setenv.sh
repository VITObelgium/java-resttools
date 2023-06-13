#!/bin/bash
# (c) 2020-2022 Stijn.VanLooy@vito.be

# 1. validation

ERROR=""
echo ""

# default deploy path
if [ -z "$DEMO_DEPLOY_PATH" ]
then 
	DEMO_DEPLOY_PATH="demo"
fi 


if [ -z "$ERROR" ]
then
	: 
else
	echo ""
	echo "Supported environment variables (* = required, - = optional, +/'/. = exactly one required):"
	# variables parsed by this script
	echo "DEMO_DEPLOY_PATH           - path to deploy the service into (default: demo)" 
	echo "DEMO_DEVELOPMENT_MODE      - set to 1 to expose the debug port (8000) and enable the"
	echo "                             allow-everything-from-everywhere CORS filter (default: both disabled)"
	echo ""
	echo "(These are the environment variables for the container configuration."
	echo "The application configuration most likely requires additional environment variables!)"
	exit 1
fi

# 2. enable debugging @ port 8000 and enable allow-everything-from-everywhere CORS filter when in development mode

if [ -z "$DEMO_DEVELOPMENT_MODE" ]
then
	rm /usr/local/tomcat/conf/web.xml.development
	rm /usr/local/tomcat/lib/corsfilter-1.0.2.jar
else
	if [ "$DEMO_DEVELOPMENT_MODE" -eq 1 ]
	then
		JPDA_ADDRESS='*:8000'
		rm /usr/local/tomcat/conf/web.xml
		mv /usr/local/tomcat/conf/web.xml.development /usr/local/tomcat/conf/web.xml
	fi
	
fi
 
# 3. create symlink for deployment path

ln -s /usr/local/tomcat/webapps/ROOT /usr/local/tomcat/webapps/$DEMO_DEPLOY_PATH
