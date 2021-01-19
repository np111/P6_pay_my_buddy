#!/bin/bash
set -Eeuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

base_repo_dir="$(pwd)/.."
tmp_dir="$(pwd)/tmp"
docs_dir="$(pwd)/tmp/docs"

generate() {
  rm -rf "$docs_dir"
  mkdir -p "$docs_dir"

  # Copy swagger-ui
  pushd "$tmp_dir"
  npm i swagger-ui-dist@3.38.0
  rsync -av --progress 'node_modules/swagger-ui-dist/' "$docs_dir" \
    --exclude 'package.json' \
    --exclude 'README.md' \
    --exclude 'index.js' \
    --exclude 'swagger-ui.js' \
    --exclude 'absolute-path.js' \
    --exclude '*-es-*' \
    --exclude '*.map'
  sed -i 's#url: "https://petstore.swagger.io/v2/swagger.json",#url: "openapi.json?'"$(git rev-parse HEAD)"'", readOnly: true,#' "${docs_dir}/index.html"
  sed -i 's#layout: "StandaloneLayout"#layout: "BaseLayout"#' "${docs_dir}/index.html"
  popd

  # Copy openapi specification
  curl -f 'http://localhost:8081/api-docs' |
    jq '.info.description = "<a href=\"https://github.com/np111/P6_pay_my_buddy\">View Source on GitHub</a>" |
          .servers = []' >"${docs_dir}/openapi.json"
}

publish() {
  pushd "$docs_dir"
  rm -rf .git
  git init
  cp "${base_repo_dir}/.git/config" '.git/config'
  git checkout --orphan docs
  git add .
  git commit -m 'Publish docs'
  git push -f origin docs
  popd
}

case "${1:-}" in
generate)
  generate "$@"
  ;;
publish)
  publish "$@"
  ;;
*)
  echo "Usage: docs generate|publish" >&2
  exit 1
  ;;
esac

exit "$?"
