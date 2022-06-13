#!/bin/bash

groupadd --system --gid ${CUSTOM_GID:=199} tomcat
useradd --system --uid ${CUSTOM_UID:=199} --gid tomcat tomcat

chown -R tomcat:tomcat .

#command="setpriv --reuid=tomcat --regid=tomcat --init-groups --inh-caps=-all"
# 2021.07.09 Stijn Van Looy:
# --inh-caps=-all results in an error on some systems (setpriv: libcap-ng is too old for "all" caps)
# this workaround lists all capabilities (setpriv --list-caps), puts them in a one line list (tr '\n' ','), 
# adds a - before all of them except for the first one (sed -e s/,/,-/g), adds the first - sign
# and removes the redundant ,- in the end (-${ALL_CAPS::-2})
# resulting capabilities of the tomcat process can be double checked with getpcaps 1
# (1 = pid of the tomcat process in the docker container)
# (resulting capabilities list should be empty)
ALL_CAPS=`setpriv --list-caps | tr '\n' ',' | sed -e s/,/,-/g`
command="setpriv --reuid=tomcat --regid=tomcat --init-groups --inh-caps=-${ALL_CAPS::-2}"

if [ "$DEMO_DEVELOPMENT_MODE" -eq 1 ]; then
  command="${command} catalina.sh jpda run"
else
  command="${command} catalina.sh run"
fi

exec ${command}
