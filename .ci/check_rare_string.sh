#!/bin/bash



function LOG_ERROR()
{
    local content=${1}
    echo -e "\033[31m"${content}"\033[0m"
}

function LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m"${content}"\033[0m"
}

get_md5sum_cmd() {
  local md5sum_cmd="md5sum"
  if [ "$(uname)" == "Darwin" ]; then
    md5sum_cmd="md5"
  fi
  echo "$md5sum_cmd"
}

function checkConcatenatedRareString() {

   concatenatedString=${1}
   log_file=${2}
   md5sum_cmd=$(get_md5sum_cmd)

   md5_concatenatedString=$(echo -n "$concatenatedString" | $md5sum_cmd | awk '{print $1}')

   # compare rare string and stringFromGet
   get_output=$(cat ${log_file}| grep "result=" | awk -F '[][]' '{print $4}' | awk NF | tail -n 1)
   md5_stringFromGet=$(echo -n "$get_output" | $md5sum_cmd | awk '{print $1}')
   if [ "$md5_concatenatedString" != "$md5_stringFromGet" ]; then
     LOG_ERROR "error: check failed, the md5 values of rareString and stringFromGet are not equal, fail concatenatedString: ${concatenatedString}, get_output: ${get_output}"
     exit 1
   else
     LOG_INFO "check success, concatenatedString: ${concatenatedString}"
   fi
}

main() {
  concatenatedString=${1}
  log_file=${2}
  LOG_INFO "check rare string start, concatenatedString: ${concatenatedString}"

  checkConcatenatedRareString ${concatenatedString} ${log_file}
  LOG_INFO "check rare string finished!"
}

main "$@"

