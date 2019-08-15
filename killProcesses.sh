#!/bin/bash

kill -9 `ps aux | grep './bin/www' | grep -v 'grep' | awk '{print $2}'`
kill -9 `ps aux | grep 'ibis.ipl.server.Server' | grep -v 'grep' | awk '{print $2}'`
kill -9 `ps aux | grep 'tensorflow_serving/ModelServerConfig.conf' | grep -v 'grep' | awk '{print $2}'`
kill -9 `ps aux | grep 'webpack' | grep -v 'grep' | awk '{print $2}'`
kill -9 `ps aux | grep 'nodemon' | grep -v 'grep' | awk '{print $2}'`
kill -9 `ps aux | grep 'SOURCE' | grep -v 'grep' | awk '{print $2}'`
kill -9 `ps aux | grep 'TARGET' | grep -v 'grep' | awk '{print $2}'`
kill -9 `ps aux | grep 'PREDICTOR' | grep -v 'grep' | awk '{print $2}'`
