import os
import json

inputs = './inputs'
outputs = './outputs'

def process(data):
    
    preset = data['preset']

    # Remove legacy "options", replace with fields
    if preset == 'type_based':
        old = data['options']
        del data['options']
        threshold = old['threshold']
        poketype = old['type']
        data['poketype'] = poketype
        data['threshold'] = float(threshold)

    if preset == 'entry_based':
        old = data['options']
        del data['options']
        entries = old['entries']
        entries = entries.split(',')
        data['entries'] = entries


for filename in os.listdir(inputs):
    file = open( f"{inputs}/{filename}", 'r')
    data = json.load(file)
    file.close()

    process(data)
    
    file = open( f"{outputs}/{filename}", 'w')
    json.dump(data, file, indent=2)
    file.close()