#!/bin/sh

DATE=`date`

# dump some simple data to a file to be sure it was triggered
echo "GLUECE RP was detected in the output at $DATE" >> /tmp/glue_detected

cat <<FIN
<?xml version="1.0" encoding="UTF-8"?>
<exampleGLUETriggerActionScriptOutput>
  <glueDataDetected>true</glueDataDetected>
</exampleGLUETriggerActionScriptOutput>
FIN