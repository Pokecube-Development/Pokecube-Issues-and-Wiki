from pydub import AudioSegment
import os

for filename in os.listdir('.'):
    if filename.endswith('.ogg'):
        AudioSegment.from_ogg(filename).set_channels(1).export(filename, format='ogg')