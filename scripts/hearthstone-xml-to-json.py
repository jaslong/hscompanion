import json
import sys
import time
import traceback
import xml.etree.ElementTree

# VERSION 1
#
# <cardset>:
# {
#   "version": <int>,
#   "timestamp": <int>,
#   "cards": [
#     *<card>
#   ]
# }
#
# <card>:
# {
#   "ArtistName": <string>,
#   "CardId": <string>,
#   "CardName": <string>,
#   "CardSet": (Basic|Expert|Reward|Promo|Naxxramas),
#   "CardTextInHand": <string>,
#   "CardType": (Minion|Spell|Weapon),
#   "Class": <class>,
#   "Collectible": <int>,
#   "Cost": <int>,
#   "FlavorText": <string>,
#   "Rarity": (Basic|Common|Rare|Epic|Legendary),
#   ?"Atk": <int>,
#   ?"Health": <int>,
#   ?"HowToGetThisCard": <string>,
#   ?"HowToGetThisGoldCard": <string>,
#   ?"Race": (Beast|Demon|Dragon|Murloc|Pirate|Totem),
# }

VERSION = 1

CARD_DEFS_LANG = 'enUS'
CARD_DEFS_START = '<CardDefs>'
CARD_DEFS_END = '</CardDefs>'
XML_START = '<Entity'
XML_END = '</Entity>'

compact = True if len(sys.argv) > 2 else False

def main():
    sys.stdout.write('{{"version":{},"timestamp":{},"cards":['.format(VERSION, int(time.time())))
    f = open(sys.argv[1]);
    currentEntity = None
    sep = ''
    foundLang = False
    for line in f:
        if not foundLang:
            if CARD_DEFS_LANG in line and CARD_DEFS_START in line:
                foundLang = True
            continue

        if currentEntity:
            endIndex = line.find(XML_END)
            if endIndex != -1:
                currentEntity += line[endIndex:endIndex+len(XML_END)]
                raw = handleEntity(currentEntity)
                if raw:
                    # Write separator
                    sys.stdout.write(sep)
                    sep = ','

                    # Write JSON object
                    separators = (',', ':') if compact else None
                    indent = None if compact else 2
                    entityJson = json.dumps(
                        raw,
                        indent=indent,
                        separators=separators,
                        sort_keys=True)
                    sys.stdout.write(entityJson)
                currentEntity = None
            else:
                currentEntity += line
        else:
            startIndex = line.find(XML_START)
            if startIndex != -1:
                currentEntity = line[startIndex:]

        if foundLang:
            if CARD_DEFS_END in line:
                break
    sys.stdout.write(']}')

def handleEntity(entityString):
    entity = xml.etree.ElementTree.fromstring(entityString)
    version = entity.attrib[ENTITY_ATTRIB_VERSION]
    try:
        return HANDLE_ENTITY_FUNC[version](entity)
    except KeyError:
        traceback.print_exc()
        sys.stderr.write('Unhandled entity: ' + str(entityString) + '\n')

ENTITY_ATTRIB_VERSION = 'version'
ENTITY_ATTRIB_CARD_ID = 'CardID'
TAG_TAG = 'Tag'
TAG_ATTRIB_ENUM = 'enumID'
TAG_ATTRIB_TYPE = 'type'
TAG_ATTRIB_VALUE = 'value'
UNKNOWN_ENUMS = ['32', '190', '201', '205', '218', '251', '268', '330', '331', '335', '349', '361', '367', '380', '389', '401', '402']
ENUM_TO_NAME = {
    '45': 'Health',
    '47': 'Attack',
    '48': 'ManaCost',
    '114': 'Legendary',
    '183': 'Set',
    '184': 'Text',
    '185': 'Name',
    '187': 'Durability',
    '189': 'Windfury',
    '191': 'Stealth',
    '192': 'SpellDamage',
    '194': 'DivineShield',
    '197': 'Charge',
    '199': 'Class',
    '200': 'Race',
    '202': 'Type',
    '203': 'Rarity',
    '208': 'Freeze',
    '212': 'Enrage',
    '215': 'Overload',
    '217': 'Deathrattle',
    '219': 'Secret',
    '220': 'Combo',
    '252': 'Adjective',
    '293': 'Transformed',
    '321': 'Collectible',
    '325': 'DraggingText',
    '338': 'AttackIncreased',
    '339': 'Silence',
    '342': 'ArtistName',
    '350': 'AdjacentMinionsIncreasedAttack',
    '351': 'FlavorText',
    '362': 'OngoingEffect',
    '363': 'Poisonous',
    '364': 'HowToGetThisCard',
    '365': 'HowToGetThisGoldCard',
    '370': 'VariableDamage',
    '377': 'OnDrawEffect',
}
ENUM_TO_VALUE = {
    'Set': {
        '2': 'Basic',
        '3': 'Expert',
        '4': 'Reward',
        '5': 'Tutorial',
        '8': 'Dev',
        '11': 'Promo',
        '12': 'Naxxramas',
        '13': 'Goblins vs Gnomes',
        '14': 'Blackrock Mountain',
        '16': 'Credits',
        },
    'Type': {
        '3': 'Hero',
        '4': 'Minion',
        '5': 'Spell',
        '6': 'Enchantment',
        '7': 'Weapon',
        '10': 'Hero Power',
        },
    'Class': {
        '2': 'Druid',
        '3': 'Hunter',
        '4': 'Mage',
        '5': 'Paladin',
        '6': 'Priest',
        '7': 'Rogue',
        '8': 'Shaman',
        '9': 'Warlock',
        '10': 'Warrior',
        '11': 'Dream Card',
        },
    'Race': {
        '14': 'Murloc',
        '15': 'Demon',
        '17': 'Mech',
        '20': 'Beast',
        '21': 'Totem',
        '23': 'Pirate',
        '24': 'Dragon',
        },
    'Rarity': {
        '1': 'Common',
        '2': 'Free',
        '3': 'Rare',
        '4': 'Epic',
        '5': 'Legendary',
        },
}

def handleEntityV2(entity):
    raw = {}
    cardId = entity.attrib[ENTITY_ATTRIB_CARD_ID]
    raw[ENTITY_ATTRIB_CARD_ID] = cardId
    for tag in entity.findall(TAG_TAG):
        enum = tag.attrib[TAG_ATTRIB_ENUM]
        try:
            name = ENUM_TO_NAME[enum]
        except KeyError:
            if enum in UNKNOWN_ENUMS:
                continue
            sys.stderr.write('\nUnknown enumID: ' + enum)
            name = enum

        type = tag.attrib[TAG_ATTRIB_TYPE]
        value = None
        if type == 'String':
            value = tag.text
        else:
            value = tag.attrib[TAG_ATTRIB_VALUE]
            if name in ENUM_TO_VALUE:
                try:
                    value = ENUM_TO_VALUE[name][value]
                except KeyError:
                    pass
                    sys.stderr.write(
                        '\nUnknown enum for card ' + cardId +
                        ' with name ' + name + ' and value ' + value)

        if value is None:
            sys.stderr.write(
                'No value found for Tag with name ' + name + '\n' +
                xml.etree.ElementTree.tostring(entity) + '\n\n')

        raw[name] = value

    return raw

HANDLE_ENTITY_FUNC = {
    '1': lambda entityString: None,
    '2': handleEntityV2,
}

main()
