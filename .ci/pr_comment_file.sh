#!/bin/bash

set -e

pr_comment_file()
{
    local content="$(cat ${1}|sed ':label;N;s/\n/\\n/g;b label')"
    curl -n -X POST -d "{\"body\": \"${content}\"}" "https://api.github.com/repos/${TRAVIS_REPO_SLUG}/issues/${TRAVIS_PULL_REQUEST}/comments"
}

pr_comment_file_github()
{
    local content="$(cat ${1}|sed ':label;N;s/\n/\\n/g;b label')"
    local pr_number=$(jq --raw-output .pull_request.number "$GITHUB_EVENT_PATH")
    LOG_INFO "PR: ${pr_number}"
    LOG_INFO "GITHUB_REPOSITORY: ${GITHUB_REPOSITORY}"
    curl -H "Authorization: token ${GITHUB_TOKEN}" -X POST -d "{\"body\": \"${content}\"}" "https://api.github.com/repos/${GITHUB_REPOSITORY}/issues/${pr_number}/comments"
}

pr_comment_file_github $@