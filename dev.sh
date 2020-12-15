#!/bin/bash
set -Eeuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

. ./.dev/colors.inc.sh

print_usage() {
  echo "${yellow}Usage:${reset}"
  echo " $0 <command> [options]"
  echo
  echo "${yellow}Available commands:${reset}"
  echo "${green} docker [...]${reset}          Manually use docker-compose commands"
  echo "${green} docker up -d${reset}          Create and start development containers (in background)"
  echo "${green} docker down${reset}           Stop and remove development containers"
  echo "${green} docker logs${reset}           Print development containers logs"
  echo "${green} db-migration <desc>${reset}   Print database migration filename"
  echo "${green} db-seed${reset}               Seed the database with tests data"
  echo "${green} docs generate${reset}         Generate docs"
  echo "${green} docs publish${reset}          Publish docs"
}

docker_compose() {
  command='command'
  if [[ "$USER" != "root" ]] && ! groups "$USER" | grep -q '\bdocker\b'; then
    command='sudo'
  fi
  ${command} docker-compose -p 'pmb' -f '.dev/docker-compose.yml' "$@"
}

db_migration() {
  DATE="$(date -u +%Y.%m.%d@%H*60*60+%M*60+%S)"
  ELAPSED_SECONDS_UTC=$(printf "%05d" $(echo "$DATE" | cut -d@ -f2 | bc))
  DESC=$(echo "${*}" | sed 's/\s/_/g')
  echo "V$(echo "$DATE" | cut -d@ -f1).${ELAPSED_SECONDS_UTC}__${DESC}.sql"
}

db_seed() {
  ./.dev/seed-db.sh "$@"
}

docs() {
  ./.dev/docs.sh "$@"
}

# main
if [ $# -lt 1 ]; then
  print_usage
  exit 0
fi

command="$1"
shift
case "$command" in
docker)
  docker_compose "$@"
  ;;
db-migration)
  db_migration "$@"
  ;;
db-seed)
  db_seed "$@"
  ;;
docs)
  docs "$@"
  ;;
*)
  echo "${red2}Error: '${command}' is not a dev command.${reset2}" >&2
  exit 1
  ;;
esac

exit "$?"
