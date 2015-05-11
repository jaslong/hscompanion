import os
import subprocess
import sys

indir = sys.argv[1]
outdir = sys.argv[2]
filenames = os.listdir(indir)

for filename in filenames:
    infile = indir + '/' + filename
    outfile = outdir + '/' + filename
    cmd = "pngquant --nofs --speed 1 --quality=50 {} -o {}".format(infile, outfile)
    print cmd
    subprocess.call(cmd, shell=True)
