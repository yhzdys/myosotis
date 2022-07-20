#!/bin/bash

MYOSOTIS_HOME="$(
  cd "$(dirname "$0")"/.. || exit
  pwd
)"
APP_DIR="${MYOSOTIS_HOME}/application"

SERVER_PID="$(pgrep -f -l -a "myosotis-console" | grep java | grep "${APP_DIR}" | awk '{print $1}')"
if [ -z "${SERVER_PID}" ]; then
  echo "No Myosotis-Console is running..."
else
  echo "Myosotis-Console(${SERVER_PID}) is running..."
  kill "${SERVER_PID}"
  echo "Send shutdown request to Myosotis-Console(${SERVER_PID}) OK"
fi

SERVER_PID="$(pgrep -f -l -a "myosotis-server" | grep java | grep "${APP_DIR}" | awk '{print $1}')"
if [ -z "${SERVER_PID}" ]; then
  echo "No Myosotis-Server is running..."
else
  echo "Myosotis-Server(${SERVER_PID}) is running..."
  kill "${SERVER_PID}"
  echo "Send shutdown request to Myosotis-Server(${SERVER_PID}) OK"
fi
