#!/bin/bash

echo "Start nginx"
nginx
sleep 1
echo "Start streaming"
ffmpeg -stream_loop -1 -re -i horse_bear.mp4 -c:v libx264 -c:a aac -f flv rtmp://rtmp_server:1935/live
