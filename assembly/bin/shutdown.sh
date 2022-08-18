#!/bin/bash

MYOSOTIS_HOME="$(
  cd "$(dirname "$0")"/.. || exit
  pwd
)"

shutdown_console() {
  CONSOLE_PID="$(pgrep -f -l -a "myosotis-console" | grep java | grep "${MYOSOTIS_HOME}" | awk '{print $1}')"
  if [ -z "${CONSOLE_PID}" ]; then
    echo "No Myosotis-Console is running..."
  else
    echo "Myosotis-Console(${CONSOLE_PID}) is running..."
    kill "${CONSOLE_PID}"
    echo "Send shutdown request to Myosotis-Console(${CONSOLE_PID}) OK"
  fi
}

shutdown_server() {
  SERVER_PID="$(pgrep -f -l -a "myosotis-server" | grep java | grep "${MYOSOTIS_HOME}" | awk '{print $1}')"
  if [ -z "${SERVER_PID}" ]; then
    echo "No Myosotis-Server is running..."
  else
    echo "Myosotis-Server(${SERVER_PID}) is running..."
    kill "${SERVER_PID}"
    echo "Send shutdown request to Myosotis-Server(${SERVER_PID}) OK"
  fi
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
  shutdown_console
  shutdown_server
elif [ "ALL" == "${MODE}" ]; then
  shutdown_console
  shutdown_server
elif [ "CONSOLE" == "${MODE}" ]; then
  shutdown_console
elif [ "SERVER" == "${MODE}" ]; then
  shutdown_server
else
  echo "Unexpected parameter"
  exit 5
fi
