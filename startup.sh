#!/bin/bash

CLASSPATH=$(find "./jars/" -name '*.jar' | xargs echo | tr ' ' ':')

node --jvm --vm.cp $CLASSPATH ./js/serverTwitterSpark.js
