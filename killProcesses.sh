#!/bin/bash

kill -9 `ps aux | grep 'SOURCE' | grep -v 'grep' | awk '{print $2}'`
kill -9 `ps aux | grep 'TARGET' | grep -v 'grep' | awk '{print $2}'`
