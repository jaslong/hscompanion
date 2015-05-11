import os
import re
import subprocess
import sys

directory = sys.argv[1]
filenames = os.listdir(directory)

masks = {
    'Weapon': 'masks/mask-weapon.png',
    'Spell': 'masks/mask-spell.png',
    'Minion': 'masks/mask-minion.png',
    'LMinion': 'masks/mask-legendary-minion.png'
}

for filename in filenames:
    match = re.match(r'card_...-\w+-([a-zA-Z0-9_]+)-(\w+)\.png', filename)
    if not match:
        sys.stderr.write('File ' + filename + ' does not match regex.\n')
        continue
    cardId = match.group(1)
    cardType = match.group(2)
    outFile = "card-{}.png".format(cardId)
    maskFile = masks[cardType]

    filename = directory + "/" + filename
    outFile = directory + "-out/" + outFile
    print "In: {}, Out: {}, Mask: {}".format(filename, outFile, maskFile)
    subprocess.call(['bash', 'convert-ss-to-card.sh', filename, maskFile, outFile])
