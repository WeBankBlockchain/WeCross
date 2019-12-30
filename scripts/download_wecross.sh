#!/bin/bash
set -e

compatibility_version=
enable_build_from_resource=0

LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR()
{
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

help()
{
    echo "$1"
    cat << EOF
Usage:
    -s                              [Optional] Get wecross by: gradle build from github Source Code.
    -h  call for help
e.g
    bash $0 
    bash $0 -s 
EOF
exit 0
}


parse_command()
{
while getopts "sh" option;do
    # shellcheck disable=SC2220
    case ${option} in
    s)
        enable_build_from_resource=1
    ;;
    h)  help;;
    esac
done

}

parallel_download()
{
    #parallel_download WeCross.tar.bz2.md5 https://github.com/WeBankFinTech/WeCross/releases/download/${compatibility_version}
    md5_file_list=${1}
    prefix=${2}
    # shellcheck disable=SC2162
    
    while read line;do
        local part=$(echo ${line}|awk '{print $2}')
        curl -s -C - -LO ${prefix}/${part} &
    done < "${md5_file_list}"   
    wait
    
    if [ ! -z "$(md5sum -c ${md5_file_list}|grep FAILED)" ];then
        LOG_ERROR "Download WeCross package failed! URL: ${prefix}"
        exit 1
    fi
}

download_wecross_pkg()
{
    if [ -d WeCross/apps ];then
        LOG_INFO "./WeCross/ exists"
        exit 0
    fi

    LOG_INFO "Checking latest release"
    if [ -z "${compatibility_version}" ];then
        compatibility_version=$(curl -s https://api.github.com/repos/WeBankFinTech/WeCross/releases/latest | grep "tag_name"|awk -F '\"' '{print $4}')
    fi

    latest_wecross=WeCross.tar.gz
    latest_wecross_checksum_file=WeCross.tar.gz.md5
    LOG_INFO "Latest release: ${compatibility_version}"


    # in case network is broken
    #if [ -z "${compatibility_version}" ];then
    #    compatibility_version="${default_version}"
    #fi
    curl -LO https://github.com/WeBankFinTech/WeCross/releases/download/${compatibility_version}/${latest_wecross_checksum_file}

    if [ ! -e ${latest_wecross} ] || [ -z "$(md5sum -c ${latest_wecross_checksum_file}|grep OK)" ];then
        LOG_INFO "Download from: https://github.com/WeBankFinTech/WeCross/releases/download/${compatibility_version}/${latest_wecross}"
        curl -C - -LO https://github.com/WeBankFinTech/WeCross/releases/download/${compatibility_version}/${latest_wecross}


        if [ -z "$(md5sum -c ${latest_wecross_checksum_file}|grep OK)" ];then
            LOG_ERROR "Download WeCross package failed! URL: https://github.com/WeBankFinTech/WeCross/releases/download/${compatibility_version}/${latest_wecross}"
            rm -f ${latest_wecross}
            exit 1
        fi

    else
        LOG_INFO "Latest release ${latest_wecross} exists."
    fi

    tar -zxf ${latest_wecross}
}

build_from_source()
{
    if [ -d WeCross/apps ];then
        LOG_INFO "./WeCross/ exists"
        exit 0
        return
    fi

    git clone https://github.com/WeBankFinTech/WeCross.git
    cd WeCross
    ./gradlew assemble 2>&1 | tee output.log
    # shellcheck disable=SC2046
    # shellcheck disable=SC2006
    if [ `grep -c "BUILD SUCCESSFUL" output.log` -eq '0' ]; then
        LOG_ERROR "Build Wecross project failed"
        LOG_INFO "See output.log for details"
        mv output.log ../output.log
        cd ..
        exit 1
    fi
    cd ..
    mv WeCross WeCross-Source
    mv WeCross-Source/dist WeCross
    rm -rf WeCross-Source
}

main()
{
    if [ 1 -eq ${enable_build_from_resource} ];then
        build_from_source
    else
        download_wecross_pkg
    fi
}

print_result()
{
LOG_INFO "Download completed. WeCross is in: ./WeCross/"
}

parse_command $@
main
print_result
