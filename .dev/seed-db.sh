#!/bin/bash
set -Eeuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

users_names='Test Test
Jeff Bezos
Bernard Arnault
Elon Musk
Bill Gates
Mark Elliot Zuckerberg
Warren Buffett
马云
Mǎ Huàténg
Dwayne Johnson
Robert Downey Jr
Will Smith
Jessica Alba
Jennifer Aniston
Julia Louis Dreyfus
Notch'
users_count="$(echo "$users_names" | wc -l)"

# password is "password", encoded with bcrypt
users_password='$2b$10$55EvTUX/nSrzEx2SGkFgruASUN4g35r/LyyFHNdfh9wL./izAW8AW'

currencies=(USD EUR JPY)
currencies_count=${#currencies[@]}

currency_decimals() {
  currency="$1"
  [[ "$currency" == "JPY" ]] && echo 0 || echo 2
}

descriptions='Lorem ipsum dolor sit amet, consectetur adipiscing elit.
Sed do eiusmod.
Tempor incididunt
ut labore et dolore magna aliqua
Faucibus vitae aliquet nec ullamcorper.
Quis eleifend quam adipiscing vitae proin sagittis nisl
BLANDIT TURPIS CURSUS IN HAC HABITASSE PLATEA DICTUMST
Lorem mollis aliquam ut porttitor. Sed odio morbi quis commodo. Congue eu consequat ac felis donec. Aliquet nibh praesent tristique magna sit amet purus.
Gravida arcu
OK
ac tortor dignissim convallis aenean... MORBI tristique senectus.
Et netus et malesuada fames ac.
Risus commodo viverra
Maecenas accumsan lacus!!! Ut morbi tincidunt augue interdum velit euismod in pellentesque. A condimentum vitae sapien pellentesque habitant morbi tristique senectus. In massa tempor nec feugiat nisl pretium fusce id.
Amet nulla facilisi morbi tempus iaculis. Nec feugiat nisl pretium fusce id.
Vulputate
Dignissim
Suspendisse in est ante in.'

sql_query() {
  # echo "$1"
  mariadb -e "$1" -h localhost -P 14373 --protocol=TCP -u pmb -ppmb -sN pmb
}

s_shuf() {
  seed="$1"
  shift
  shuf --random-source=<(openssl enc -aes-256-ctr -pass pass:"$seed" -nosalt </dev/zero 2>/dev/null) "$@"
}

s_rand() {
  seed="$1"
  min_inclusive="$2"
  max_exclusive="$3"
  s_shuf "$seed" -i"${min_inclusive}-$((max_exclusive - 1))" -n1
}

s_rand_amount() {
  seed="$1"
  currency="$2"
  max="$3"

  decimals="$(currency_decimals "$currency")"
  max=$((max * (10 ** decimals)))
  amount="$(s_rand "$seed" 1 "$max" | rev)0000000000"
  amount="${amount:0:$decimals}.${amount:$decimals}"
  echo "$amount" | rev
}

seed_user_count=0
seed_user() {
  seed_user_count=$((seed_user_count + 1))
  id="$seed_user_count"
  name="$1"

  # Create user
  if echo "$name" | LANG=C grep -qE '^[a-zA-Z]+ [a-zA-Z]+'; then
    email="$(echo "$1" | awk '{print tolower($1) "@" tolower($2) ".com"}')"
  else
    email="test${id}@test.fr"
  fi
  currency=${currencies[$(s_rand "currency:$name" 0 "$currencies_count")]}

  sql_query 'INSERT INTO `users`(`id`, `email`, `password`, `name`, `default_currency`)
             VALUES('"$id"', "'"$email"'", "'"$users_password"'", "'"$name"'", "'"$currency"'");'

  # Create balances
  if [[ "$id" == "1" ]]; then
    balances_count="$currencies_count"
  else
    balances_count=$(s_rand "balancecount:$id" 1 "$((currencies_count + 1))")
  fi

  balances_currencies="$(echo "${currencies[*]}" | tr ' ' $'\n' | s_shuf "balance:$id" -n"$balances_count")"
  while IFS= read -r balances_currency; do
    balance_amount=$(s_rand_amount "balance:$balances_currency:$id" "$currency" 1000)
    sql_query 'INSERT INTO `user_balances`(`user_id`, `currency`, `amount`)
               VALUES("'"$id"'", "'"$balances_currency"'", "'"$balance_amount"'");'
  done <<<"$balances_currencies"
}

seed_contacts() {
  user_id="$1"
  if [[ "$user_id" == "1" ]]; then
    contacts_count=$users_count
  else
    contacts_count=$(s_rand "contactcount:$user_id" 0 $((users_count * 2 / 3)))
  fi

  contacts_ids="$(s_shuf "contact:$user_id" -i"1-$users_count" -n"$contacts_count")"
  contacts_ids="$(echo "$contacts_ids" | grep -v "^$user_id$" || true)"
  if [[ -n "$contacts_ids" ]]; then
    while IFS= read -r contact_id; do
      sql_query 'INSERT INTO `user_contacts`(`user_id`, `contact_id`)
                 VALUES("'"$user_id"'", "'"$contact_id"'");'
    done <<<"$contacts_ids"
  fi
}

seed_transaction() {
  seed="$1"
  sender_id="$2"
  recipient_id="$3"
  currency="$4"
  date="$5"

  amount="$(s_rand_amount "$seed:amount" "$currency" 200)"
  decimals="$(currency_decimals "$currency")"
  fee="$(echo "scale=${decimals}; $amount/200" | bc -l)"
  description="$(echo "$descriptions" | s_shuf "$seed:desc" -n1)"

  sql_query 'INSERT INTO `transactions`(`sender_id`, `recipient_id`, `currency`, `amount`, `fee`, `description`, `date`)
             VALUES('"$sender_id"', '"$recipient_id"', "'"$currency"'", "'"$amount"'", "'"$fee"'", "'"$description"'", "'"$date"'");'
}

main() {
  echo 'Tuncate tables...'
  sql_query '
      SET FOREIGN_KEY_CHECKS = 0;
      TRUNCATE `users`;
      TRUNCATE `user_balances`;
      TRUNCATE `user_contacts`;
      TRUNCATE `transactions`;
      SET FOREIGN_KEY_CHECKS = 1;'

  echo 'Seed users (with balances)'
  while IFS= read -r name; do
    seed_user "$name"
  done <<<"$users_names"

  echo 'Seed contacts'
  for ((user_id = 1; user_id <= "$users_count"; user_id = user_id + 1)); do
    seed_contacts "$user_id"
  done

  echo 'Seed transactions'
  for ((i = 0; i < 250; i = i + 1)); do
    sender_id="$(s_rand "tr:$i:sender" 1 $((users_count + 1)))"
    recipient_id="$sender_id"
    j=0
    while [[ "$recipient_id" == "$sender_id" ]]; do
      j=$((j + 1))
      recipient_id="$(s_rand "tr:$i:$j:recipient" 1 $((users_count + 1)))"
    done
    date="$(date -d@$((1602259783 + i * 20874 - $(s_rand "tr:$i:dateoffset" 0 20874))) +'%Y-%m-%d %H:%M:%S')"
    seed_transaction "tr:$i" "$sender_id" "$recipient_id" \
      "${currencies[$(s_rand "tr:$i:currency" 0 "$currencies_count")]}" "$date"
  done

  echo 'Done'
}

main "$@"
exit 0
