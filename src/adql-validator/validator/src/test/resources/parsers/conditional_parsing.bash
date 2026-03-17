#!/usr/bin/env bash

isvalid=$(echo "$3" | sed 's/^.* -- valid:\(.*\)$/\1/')

if [ "$isvalid" = 'true' ]
then
  echo '{"success":"true"}'
else
  echo '{"success":"false", "error": "I failed because I wanted to!"}'
fi