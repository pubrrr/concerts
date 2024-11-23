#!/bin/bash

rm -rf /tmp/feierwerk/*
mkdir /tmp/feierwerk

cd /tmp/feierwerk/ || exit

wget -r -l 1  https://www.feierwerk.de/konzert-kulturprogramm/kpp

counter=1

cp /tmp/feierwerk/www.feierwerk.de/konzert-kulturprogramm/kpp "/tmp/feierwerk/result0"

find . -type f -name "*kkp*" -print0 | while IFS= read -r -d '' file; do
  mv "$file" "/tmp/feierwerk/result$counter"
  ((counter++))
done


rm -rf /tmp/feierwerk/www.feierwerk.de/