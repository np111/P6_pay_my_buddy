#!/bin/bash
# This script define global bash variables to format and color texts.
#
# Version: 2018-11-16
# Author: Nathan Poirier <nathan@poirier.io>

__colors_is_supported() {
  local fd="$1"

  # preconditions
  if ! command -v tput >/dev/null 2>&1; then
    return 1 #false
  fi

  # check FORCE_COLOR override
  local varname="FORCE_COLOR${fd}"
  local force_color="${!varname:-}"
  [ -z "$force_color" ] && force_color="${FORCE_COLOR:-}"
  if [ -n "$force_color" ]; then
    if [[ "$force_color" -ge 1 ]]; then
      return 0 #true
    fi
    return 1 #false
  fi

  # check tty & term capabilities
  if test -t "$fd" && [[ "$(tput colors)" -ge 16 ]]; then
    return 0 #true
  fi
  return 1 #false
}

__colors_init() {
  local fd="$1"
  local pf="$2"
  local sf="$3"

  local reset bold dim italic underline inverse strikethrough black red green yellow blue magenta cyan white \
    blackBright redBright greenBright yellowBright blueBright magentaBright cyanBright whiteBright
  if __colors_is_supported "$fd"; then
    reset="$(tput sgr0)"
    bold="$(tput bold)"
    dim="$(tput dim)"
    italic="$(tput sitm)"
    underline="$(tput smul)"
    inverse="$(tput rev)"
    strikethrough=$'\e[9m'
    black="$(tput setaf 0)"
    red="$(tput setaf 1)"
    green="$(tput setaf 2)"
    yellow="$(tput setaf 3)"
    blue="$(tput setaf 4)"
    magenta="$(tput setaf 5)"
    cyan="$(tput setaf 6)"
    white="$(tput setaf 7)"
    blackBright="$(tput setaf 8)"
    redBright="$(tput setaf 9)"
    greenBright="$(tput setaf 10)"
    yellowBright="$(tput setaf 11)"
    blueBright="$(tput setaf 12)"
    magentaBright="$(tput setaf 13)"
    cyanBright="$(tput setaf 14)"
    whiteBright="$(tput setaf 15)"
  fi

  declare -g "${pf}reset${sf}=${reset:-}"
  declare -g "${pf}bold${sf}=${bold:-}"
  declare -g "${pf}dim${sf}=${dim:-}"
  declare -g "${pf}italic${sf}=${italic:-}"
  declare -g "${pf}underline${sf}=${underline:-}"
  declare -g "${pf}inverse${sf}=${inverse:-}"
  declare -g "${pf}strikethrough${sf}=${strikethrough:-}"
  declare -g "${pf}black${sf}=${black:-}"
  declare -g "${pf}red${sf}=${red:-}"
  declare -g "${pf}green${sf}=${green:-}"
  declare -g "${pf}yellow${sf}=${yellow:-}"
  declare -g "${pf}blue${sf}=${blue:-}"
  declare -g "${pf}magenta${sf}=${magenta:-}"
  declare -g "${pf}cyan${sf}=${cyan:-}"
  declare -g "${pf}white${sf}=${white:-}"
  declare -g "${pf}blackBright${sf}=${blackBright:-}"
  declare -g "${pf}redBright${sf}=${redBright:-}"
  declare -g "${pf}greenBright${sf}=${greenBright:-}"
  declare -g "${pf}yellowBright${sf}=${yellowBright:-}"
  declare -g "${pf}blueBright${sf}=${blueBright:-}"
  declare -g "${pf}magentaBright${sf}=${magentaBright:-}"
  declare -g "${pf}cyanBright${sf}=${cyanBright:-}"
  declare -g "${pf}whiteBright${sf}=${whiteBright:-}"
  declare -g "${pf}grey${sf}=${blackBright:-}" # (alias)
  declare -g "${pf}gray${sf}=${blackBright:-}" # (alias)
}

__colors_init 1 '' ''
__colors_init 2 '' '2'
