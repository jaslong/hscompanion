X_POS=698
Y_POS=64
X_SIZE=428
Y_SIZE=612
CROP_GEOMETRY="${X_SIZE}x${Y_SIZE}+${X_POS}+${Y_POS}"

ss=$1
mask=$2
out=$3

convert $ss $mask -alpha off -compose copy_opacity -composite -crop $CROP_GEOMETRY $out
