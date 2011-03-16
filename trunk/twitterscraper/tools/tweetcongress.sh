#!/bin/bash

TWEETCONGRESS='@tweetCongress'

function scrape {
	COUNTER=1
	while [ $COUNTER -lt 30 ]; do
		wget -O temp.txt http://tweetcongress.org/members/index/page:${COUNTER}/$1 &> /dev/null
		cat temp.txt | grep @ > temp2.txt
		while read LINE
		do   
			CANDIDATE=`echo $LINE | sed 's/\(.*\) \(@[^ ]*\).*/\"\1\",\2/g'`
			if [ "$CANDIDATE" != "$TWEETCONGRESS" ];
		        then
				echo $CANDIDATE >> temp3.txt
				echo $CANDIDATE
		        fi
		done <temp2.txt
		let COUNTER=COUNTER+1 
	done
}

rm -f temp.txt
rm -f temp2.txt
rm -f temp3.txt

scrape "party:D" 
sort temp3.txt | uniq > democrats.csv
rm temp3.txt

scrape "party:R"
sort temp3.txt | uniq > republicans.csv

rm -f temp.txt
rm -f temp2.txt
rm -f temp3.txt
