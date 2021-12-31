import os
import sys
import json
import re
from glob import glob


def make_layer(n, tex):
    layer_template = f'''{{
    "parent": "block/thin_block",
    "textures": {{
        "particle": "{tex}",
        "texture": "{tex}"
    }},
    "elements": [
        {{   "from": [ 0, 0, 0 ],
            "to": [ 16, {n}, 16 ],
            "faces": {{
                "down":  {{ "uv": [ 0, 0, 16, 16 ], "texture": "#texture", "cullface": "down" }},
                "up":    {{ "uv": [ 0, 0, 16, 16 ], "texture": "#texture" }},
                "north": {{ "uv": [ 0, {16-n}, 16, 16 ], "texture": "#texture", "cullface": "north" }},
                "south": {{ "uv": [ 0, {16-n}, 16, 16 ], "texture": "#texture", "cullface": "south" }},
                "west":  {{ "uv": [ 0, {16-n}, 16, 16 ], "texture": "#texture", "cullface": "west" }},
                "east":  {{ "uv": [ 0, {16-n}, 16, 16 ], "texture": "#texture", "cullface": "east" }}
            }}
        }}
    ]
}}
'''
    return layer_template

def make_block(tex):
    block_template = f'''{{
  "parent": "minecraft:block/cube_all",
  "textures": {{
    "all": "{tex}"
  }}
}}
'''
    return block_template

def make_state_block(path, name):
    obj = {}
    obj["variants"] = {}
    falling = {}
    falling["model"] = f'{path}{name}_air'
    layer = {}
    layer["model"] = f'{path}{name}_block'
    obj["variants"][f""] = layer
    state = json.dumps(obj, ensure_ascii=False, indent=2)
    return state

def make_state_layer(path, name):
    obj = {}
    obj["variants"] = {}

    falling = {}
    falling["model"] = f'{path}{name}_air'


    for i in range(15):
        layer = {}
        layer["model"] = f'{path}{name}_{i+1}'
        obj["variants"][f"layers={i+1},falling=false"] = layer
        obj["variants"][f"layers={i+1},falling=true"] = falling

    layer = {}
    layer["model"] = f'{path}{name}_block'

    obj["variants"][f"layers={16},falling=false"] = layer
    obj["variants"][f"layers={16},falling=true"] = falling

    state = json.dumps(obj, ensure_ascii=False, indent=2)
    return state

def make(name, tex=None):
    if tex==None:
        tex = f'concrete:block/{name}'
    air = f'concrete:block/{name}_air'
    path = 'concrete:block/'

    filename = f'./blockstates/{name}_layer.json'
    f = open(filename, 'w')
    f.write(make_state_layer(path, name))
    f.close()

    filename = f'./blockstates/{name}_block.json'
    f = open(filename, 'w')
    f.write(make_state_block(path, name))
    f.close()

    filename = f'./models/block/{name}_block.json'
    f = open(filename, 'w')
    f.write(make_block(tex))
    f.close()

    filename = f'./models/block/{name}_air.json'
    f = open(filename, 'w')
    f.write(make_block(air))
    f.close()

    for i in range(16):
        filename = f'./models/block/{name}_{i+1}.json'
        f = open(filename, 'w')
        f.write(make_layer(i+1, tex))
        f.close()

make('dust')
make('molten')
make('solid', tex="minecraft:block/smooth_basalt")

