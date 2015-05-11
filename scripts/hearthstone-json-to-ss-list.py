import re
import json
import sys

f = open(sys.argv[1])
cardset = json.load(f)
cards = cardset['cards']
cards = [ i for i in cards if (
        'Collectible' in i and
        'Name' in i and
        'Type' in i and
        'ManaCost' in i and
        i['Set'] == 'Blackrock Mountain')]

def strcmp(s1, s2, key):
    if key in s1 and key in s2:
        if s1[key] == s2[key]:
            return 0
        elif s1[key] < s2[key]:
            return -1
        else:
            return 1
    elif key in s1 and key not in s2:
        return -1
    elif key not in s1 and key in s2:
        return 1
    else:
        return 0

def sort(c1, c2):
    diff = strcmp(c1, c2, 'Class')
    if diff != 0:
        return diff
    diff = int(c1['ManaCost']) - int(c2['ManaCost'])
    if diff != 0:
        return diff
    diff = -strcmp(c1, c2, 'Type')
    if diff != 0:
        return diff
    diff = strcmp(c1, c2, 'Name')
    return diff

cards.sort(sort)
count = 0
for card in cards:
    count += 1
    cardName = re.sub(r'[\W]+', '', card['Name'])
    cardType = ('L' if card['Rarity'] == 'Legendary' else '') + card['Type']
    line = "card_{0:03d}-{1}-{2}-{3}.png".format(
        count, cardName, card['CardID'], cardType)
    print line
