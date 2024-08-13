#!/bin/bash

MEM="-Xmx1024m";
if [[ $1 == -Xmx* ]]; then
  # this is for the VM
  MEM=$1
  shift
fi

# change dir
pwd=`pwd`
path=`case $0 in /*) echo $0;; *) echo $pwd/$0;; esac`
dir=`dirname $path`

java -cp $dir/lib/halo.jar:$dir/lib/jfreechart-1.0.13/lib/* $MEM halo.userinterface.cmdline.CommandLine $*
