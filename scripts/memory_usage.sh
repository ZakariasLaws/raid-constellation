#!/bin/bash

trap ctrl_c INT

LOG="./mem.log"
SCRIPT=$(mktemp)
IMAGE=$(mktemp)

echo "Output to LOG=$LOG and SCRIPT=$SCRIPT and IMAGE=$IMAGE"


cat >$SCRIPT <<EOL
set term png small size 800,600
set output "$IMAGE"
set ylabel "MEMORY"
set ytics nomirror
set yrange [0:*]
plot "$LOG" using 2 with lines axes x1y1 title "VSZ", "$LOG" using 3 with lines axes x1y1 title "RSS"
EOL

function ctrl_c {
	gnuplot $SCRIPT
	xdg-open $IMAGE
	exit 0;
}

while true; do
    ps -C $1 -o pid=,vsz=,rss= >> $LOG
#    ps -p $1 -o pid=,vsz=,rss= | tee -a $LOG
    sleep 5
done