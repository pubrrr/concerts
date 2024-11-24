#!/bin/bash

TMP_DIR=/tmp/feierwerk

rm -rf $TMP_DIR
mkdir $TMP_DIR

cd $TMP_DIR || exit

wget -r -l 1  https://www.feierwerk.de/konzert-kulturprogramm/kpp

counter=1

cp $TMP_DIR/www.feierwerk.de/konzert-kulturprogramm/kpp $TMP_DIR"/result0"

find . -type f -name "*kkp*" -print0 | while IFS= read -r -d '' file; do
  mv "$file" $TMP_DIR"/result$counter"
  ((counter++))
done


rm -rf /tmp/feierwerk/www.feierwerk.de/

echo "done downloading feierwerk website"