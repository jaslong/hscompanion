import os
import sys

filenames = open(sys.argv[1]).readlines()

directory = sys.argv[2]
files = os.listdir(directory)
files.sort()
print files[:3]

i = 0
for f in files:
    if f.startswith('.'):
        print 'bad'
        continue
    old = "{}/{}".format(directory, f)
    new = "{}/{}".format(directory, filenames[i][:-1])
    print "{} -> {}".format(old, new)
    os.rename(old, new)
    i += 1
