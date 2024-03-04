#!/bin/bash
rare_str_range_names=("CJKUnifiedIdeographs" "CJKCompatibilityIdeographs" "CJKCompatibilityIdeographsSupplement" "KangxiRadicals" "CJKRadicalsSupplement" "IdeographicDescriptionCharacters" "Bopomofo" "BopomofoExtended" "CJKStrokes" "CJKSymbolsandPunctuation" "CJKCompatibilityForms" "CJKCompatibility" "EnclosedCJKLettersandMonths" "CJKUnifiedIdeographsExtensionA" "CJKUnifiedIdeographsExtensionB" "CJKUnifiedIdeographsExtensionC" "CJKUnifiedIdeographsExtensionD" "CJKUnifiedIdeographsExtensionE" "CJKUnifiedIdeographsExtensionF")
rare_str_range_values=("19968,40959" "63744,64255" "194560,195103" "12032,12255" "11904,12031" "12272,12287" "12544,12591" "12704,12735" "12736,12783" "12288,12351" "65072,65103" "13056,13311" "12800,13055" "13312,19903" "131072,173791" "173824,177977" "177984,178205" "178208,183969" "183984,191456")
final_rare_input=""

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

download_rare_string_jar() {
  curl -LOs "https://github.com/FISCO-BCOS/LargeFiles/raw/master/binaries/jar/get-rare-string-with-unicode.jar"
}

getRangeValues() {
  local rangeValue=$1
  IFS=',' read -r startValue endValue <<<"$rangeValue"

  echo "$startValue $endValue"
}

getConcatenatedRareStringWithRange() {
  local startUnicode=${1}
  local endUnicode=${2}

  # concatenate strings with begin middle end
  local concatenatedString
  concatenatedString=$(java -cp './get-rare-string-with-unicode.jar' org.example.Main ${startUnicode})
  local midUnicode=$((($startUnicode + $endUnicode) / 2))
  for ((i = midUnicode; i <= midUnicode + 5; i++)); do
    local currentRareString
    currentRareString=$(java -cp './get-rare-string-with-unicode.jar' org.example.Main ${i})
    concatenatedString+="$currentRareString"
  done
  local endRareString
  endRareString=$(java -cp './get-rare-string-with-unicode.jar' org.example.Main ${endUnicode})
  concatenatedString+="$endRareString"
  echo "$concatenatedString"
}

get_rare_string() {
  download_rare_string_jar
  export LC_ALL=en_US.UTF-8
  export LANG=en_US.UTF-8
  export LANGUAGE=en_US.UTF-8

  for ((i = 0; i < ${#rare_str_range_names[@]}; i++)); do
    range_name="${rare_str_range_names[$i]}"
    range_value="${rare_str_range_values[$i]}"

    read -r start_value end_value <<<$(getRangeValues "${range_value}")
    concatenated=$(getConcatenatedRareStringWithRange $start_value $end_value)
    final_rare_input+="${concatenated}"
  done
}

main() {
  get_rare_string
  echo "${final_rare_input}"
}

main