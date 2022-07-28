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

MODE="NONE"
while getopts "acs" opt; do
  case $opt in
  a)
    MODE="ALL"
    ;;
  c)
    MODE="CONSOLE"
    ;;
  s)
    MODE="SERVER"
    ;;
  ?)
    echo "Unknown parameter"
    exit 1
    ;;
  esac
done

if [ "NONE" == "${MODE}" ]; then
  echo "No mode selected, fallback to \"ALL(a)\" mode..."
  startup_server
  startup_console
elif [ "ALL" == "${MODE}" ]; then
  startup_server
  startup_console
elif [ "CONSOLE" == "${MODE}" ]; then
  startup_console
elif [ "SERVER" == "${MODE}" ]; then
  startup_server
else
  echo "Unexpected parameter"
  exit 5
fi
