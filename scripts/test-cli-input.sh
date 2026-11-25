#!/bin/sh
set -eu
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
export CAREER_DATA="$tmp/test.db"
cli() { java -jar target/career-agent.jar "$@"; }
cli -input samples/add-job.task </dev/null > "$tmp/result"
cli job show | grep -F 'Build Java services' >/dev/null
for invalid in missing empty multiline unclosed; do
    case "$invalid" in
        empty) touch "$tmp/$invalid" ;;
        multiline) printf 'job show\njob list\n' > "$tmp/$invalid" ;;
        unclosed) printf 'job add "unfinished' > "$tmp/$invalid" ;;
    esac
    if cli -input "$tmp/$invalid" >/dev/null 2>&1; then exit 1; fi
done
if cli -input >/dev/null 2>&1; then exit 1; fi
echo 'PASS: standalone CLI task files'
