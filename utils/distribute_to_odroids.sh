#!/bin/bash

if [ -z "$1" ]
then
  echo "No argument provided"
  exit;
fi

echo "Sending $1 to all Odroids, authenticate when requested"

echo "Odroid-1"
scp $1 odroid-1:~/

echo "Odroid-2"
scp $1 odroid-2:~/


