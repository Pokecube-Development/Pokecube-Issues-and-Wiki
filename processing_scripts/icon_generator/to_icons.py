from PIL import Image
import PIL
import os
from glob import glob

imgs = [y for x in os.walk(".") for y in glob(os.path.join(x[0], '*.png'))]

size = 32, 32
lastdir = None
for file in imgs:
    img = Image.open(file)
    img = img.resize(size,resample=PIL.Image.HAMMING)
    file = file.replace('img', 'icon', 1)
    dir = os.path.dirname(file)
    try:
        os.makedirs(dir)
    except:
        pass
    if dir!=lastdir:
        print(dir)
    lastdir = dir
    img.save(file, "png")
