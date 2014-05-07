## Usage
## $ dos2unix combine_features_word_letters.sh
## $ ./combine_features_word_letters.sh

path=./../../data/features

if [[ $# != 1 ]]
then
    echo 'usage: ./combine_features_word_letters.sh <input-partial-csv>';
    echo 'example: ./combine_features_word_letters.sh akshay_1234567';
    exit
fi

# remove trailing 
sed -e "s/
sed -e "s/
echo "removing the ^M character"

# line by line concatenation of the files
paste -d"," $path/$1.tmp.csv $path/fft_$1.tmp.csv > $path/$1_combined.tmp.csv
echo "data file and the ffts combined"

# remove trailing 
sed -e "s/
echo "removing the ^M character"

# remove the temporary files created
rm $path/$1.tmp.csv -f
rm $path/fft_$1.tmp.csv -f
rm $path/$1_combined.tmp.csv -f
echo "removing tmp files"

echo "combined features file written to.... $path/$1_combined.csv"