#!/bin/bash

CONSOLE_JAVA_OPT="-server -Xms128m -Xmx128m"
SERVER_JAVA_OPT="-server -Xms128m -Xmx256m"

if ! java -version; then
  echo "Can not find java environment variable..."
  exit 4
fi

MYOSOTIS_HOME="$(
  cd "$(dirname "$0")"/.. || exit
  pwd
)"
export MYOSOTIS_HOME

startup_console() {
  echo "Myosotis-Console starting..."

  CMD="java -jar ${CONSOLE_JAVA_OPT} -Dmyosotis.home=${MYOSOTIS_HOME} ${MYOSOTIS_HOME}/application/myosotis-console.jar"
  nohup ${CMD} >/dev/null 2>&1 &

  echo "Myosotis-Console started..."
}

startup_server() {
  echo "Myosotis-Server starting..."

  CMD="java -jar ${SERVER_JAVA_OPT} -Dmyosotis.home=${MYOSOTIS_HOME} ${MYOSOTIS_HOME}/application/myosotis-server.jar"
  nohup ${CMD} >/dev/null 2>&1 &

  echo "Myosotis-Server started..."
}

STARTUP_MODE="NONE"
while getopts "acs" opt; do
  case $opt in
  a)
    STARTUP_MODE="ALL"
    ;;
  c)
    STARTUP_MODE="CONSOLE"
    ;;
  s)
    STARTUP_MODE="SERVER"
    ;;
  ?)
    echo "Unknown parameter"
    exit 1
    ;;
  esac
done

if [ "NONE" == "${STARTUP_MODE}" ]; then
  echo "No startup-mode selected, fallback to \"ALL(a)\" mode..."
  startup_server
  startup_console

elif [ "ALL" == "${STARTUP_MODE}" ]; then
  startup_server
  startup_console
elif [ "CONSOLE" == "${STARTUP_MODE}" ]; then
  startup_console
elif [ "SERVER" == "${STARTUP_MODE}" ]; then
  startup_server

else
  echo "Unexpected parameter"
  exit 5
fi
