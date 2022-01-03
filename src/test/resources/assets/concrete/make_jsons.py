import os
import sys
import json
import re
from glob import glob

def make_template_layers():
    for i in range(16):
        filename = f'./models/block/layered_{i+1}.json'
        f = open(filename, 'w')
        n = i + 1
        layer_template = f'''{{
    "parent": "block/thin_block",
    "textures": {{
        "particle": "#texture",
        "texture": "#texture"
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
        f.write(layer_template)
        f.close()

def make_layer(n, tex):
    layer_template = f'''{{
    "parent": "concrete:block/layered_{n}",
    "textures": {{
        "texture": "{tex}"
    }}
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

def make_block_item(path, name):
    block_template = f'''{{
   "parent": "{path}{name}_block"
}}
'''
    return block_template

def make_item(path, name):
    block_template = f'''{{
   "parent": "{path}{name}"
}}
'''
    return block_template

def make_item_basic(path, tex):
    block_template = f'''{{
    "parent": "item/generated",
	"textures": {{
		"layer0": "{path}{name}"
	}}
}}
'''
    return block_template

def make_layer_item(path, name):
    block_template = f'''{{
   "parent": "{path}{name}_4"
}}
'''
    return block_template

def make_state_block(path, name):
    obj = {}
    obj["variants"] = {}
    layer = {}
    layer["model"] = f'{path}{name}_block'
    obj["variants"][f""] = layer
    state = json.dumps(obj, ensure_ascii=False, indent=2)
    return state

def make_state(path, name):
    obj = {}
    obj["variants"] = {}
    layer = {}
    layer["model"] = f'{path}{name}'
    obj["variants"][f""] = layer
    state = json.dumps(obj, ensure_ascii=False, indent=2)
    return state

def make_multi_layer(path, name, air=True):
    obj = {}
    obj["multipart"] = []

    if air:
        when = {}
        when["falling"] = "true"
        apply = {}
        apply["model"] =  f'{path}{name}_air'    
        condition = {}
        condition["when"] = when
        condition["apply"] = apply
        obj["multipart"].append(condition)

    when = {}
    if air:
        when["falling"] = "false"
    when["layers"] = 16

    apply = {}
    apply["model"] = f'{path}{name}_block'

    condition = {}
    condition["when"] = when
    condition["apply"] = apply

    obj["multipart"].append(condition)

    for i in range(15):
        when = {}
        if air:
            when["falling"] = "false"
        when["layers"] = i + 1

        apply = {}
        apply["model"] = f'{path}{name}_{i+1}'

        condition = {}
        condition["when"] = when
        condition["apply"] = apply

        obj["multipart"].append(condition)

    state = json.dumps(obj, ensure_ascii=False, indent=2)
    return state

def make(name, tex=None, air=True):
    if tex==None:
        tex = f'concrete:block/{name}'
    air_tex = f'concrete:block/{name}_air'
    path = 'concrete:block/'

    filename = f'./blockstates/{name}_layer.json'
    f = open(filename, 'w')
    f.write(make_multi_layer(path, name, air))
    f.close()

    filename = f'./blockstates/{name}_block.json'
    f = open(filename, 'w')
    f.write(make_state_block(path, name))
    f.close()

    filename = f'./models/block/{name}_block.json'
    f = open(filename, 'w')
    f.write(make_block(tex))
    f.close()

    filename = f'./models/item/{name}_block.json'
    f = open(filename, 'w')
    f.write(make_block_item(path,name))
    f.close()

    filename = f'./models/item/{name}_layer.json'
    f = open(filename, 'w')
    f.write(make_layer_item(path,name))
    f.close()

    if air:
        filename = f'./models/block/{name}_air.json'
        f = open(filename, 'w')
        f.write(make_block(air_tex))
        f.close()

    for i in range(16):
        filename = f'./models/block/{name}_{i+1}.json'
        f = open(filename, 'w')
        f.write(make_layer(i+1, tex))
        f.close()


make_template_layers()

make('dust', tex="minecraft:block/tuff")
make('molten')
make('solid', tex="minecraft:block/smooth_basalt", air=False)


colours = {}
colours[0] = "white"
colours[1] = "orange"
colours[2] = "magenta"
colours[3] = "light_blue"
colours[4] = "yellow"
colours[5] = "lime"
colours[6] = "pink"
colours[7] = "gray"
colours[8] = "light_gray"
colours[9] = "cyan"
colours[10] = "purple"
colours[11] = "blue"
colours[12] = "brown"
colours[13] = "green"
colours[14] = "red"
colours[15] = "black"

rots = {}
rots["north"] = {}
rots["east"] = {"y":90}
rots["south"] = {"y":180}
rots["west"] = {"y":270}

true = True

def make_rebar_internal(name,concrete,level,item="rebar_post", state_name=None, value=None):
    path = 'concrete:block/'
    
    obj = {}
    obj["multipart"] = []
    
    # The post in the middle
    apply = {}
    apply["model"] = f"{path}rebar_post"

    condition = {}

    if level == 'level':
        when = {}
        when[level] = "0"
        condition["when"] = when

    condition["apply"] = apply

    obj["multipart"].append(condition)

    # The sides connecting outwards
    for key, rot in rots.items():
        when = {}
        when[key] = "true"

        apply = rot
        apply["model"] = f"{path}rebar_side"
        apply["uvlock"] = true

        condition = {}
        condition["when"] = when
        condition["apply"] = apply

        obj["multipart"].append(condition)

    # The concrete
    for i in range(15):

        model_key = f'{path}{concrete}_layer_{i+1}'
        if value is not None:
            model_key = f'{path}{concrete}_layer_{value}_{i+1}'

        obj["multipart"].append({'when':{level:i+1},"apply":{"model":model_key}})
        obj["multipart"].append({'when':{level:i+1},"apply":{"model":f"{path}rebar_post"}})

    model_key = f'{path}{concrete}_block'
    if value is not None:
        model_key = f'{path}{concrete}_block_{value}'
    obj["multipart"].append({'when':{level:16},"apply":{"model":model_key}})

    state = json.dumps(obj, ensure_ascii=False, indent=2)

    filename = f'./blockstates/{state_name}.json'
    f = open(filename, 'w')
    f.write(state)
    f.close()

    # Item
    filename = f'./models/item/{state_name}.json'
    f = open(filename, 'w')
    f.write(make_item(path,item))
    f.close()

def make_rebar_state(name,concrete,level,item="rebar_post", coloured=True):
    if coloured:
        for key, value in colours.items():
            _name = name + '_' + value
            state_name = _name
            make_rebar_internal(name,concrete,level,item="rebar_post",state_name=state_name,value=value)
    else:
        state_name = name
        make_rebar_internal(name,concrete,level,item="rebar_post",state_name=state_name)


def make_concrete_state(name, override=None, air=False, value=None):

    path = 'concrete:block/'

    if value is not None:
        layer_name = name + '_layer_' + value
        block_name = name + '_block_' + value
        air_name = name + '_air_' + value
        tex_name = name + '_' + value

        if override is not None:
            layer_name = override + '_layer_' + value
            block_name = override + '_block_' + value
            tex_name = override + '_' + value
            air_name = override + '_air_' + value

    else:
        layer_name = name + '_layer' 
        block_name = name + '_block' 
        air_name = name + '_air' 
        tex_name = name + '' 

        if override is not None:
            layer_name = override + '_layer'
            block_name = override + '_block'
            tex_name = override + '' 
            air_name = override + '_air'

    obj = {}
    obj["multipart"] = []

    obj_b = {}

    obj_b["variants"] = {}
    state = json.dumps(obj, ensure_ascii=False, indent=2)

    layer = {}
    layer["texture"] = f'{path}{tex_name}'
    layer["model"] = f'{path}{block_name}'
    obj_b["variants"][f""] = layer

    if air:
        when = {}
        when["falling"] = "true"
        apply = {}
        apply["model"] =  f'{path}{air_name}'    
        condition = {}
        condition["when"] = when
        condition["apply"] = apply
        obj["multipart"].append(condition)

    when = {}
    if air:
        when["falling"] = "false"
    when["layers"] = 16

    apply = {}
    apply["model"] = f'{path}{block_name}'

    condition = {}
    condition["when"] = when
    condition["apply"] = apply

    obj["multipart"].append(condition)

    for i in range(15):
        when = {}
        if air:
            when["falling"] = "false"
        when["layers"] = i + 1

        apply = {}
        apply["model"] = f'{path}{layer_name}_{i+1}'

        condition = {}
        condition["when"] = when
        condition["apply"] = apply

        obj["multipart"].append(condition)

    state = json.dumps(obj, ensure_ascii=False, indent=2)
    if value is not None:
        state_name = name + '_layer_' + value
    else:
        state_name = name + '_layer'
    filename = f'./blockstates/{state_name}.json'
    f = open(filename, 'w')
    f.write(state)
    f.close()

    state = json.dumps(obj_b, ensure_ascii=False, indent=2)
    if value is not None:
        state_name = name + '_block_' + value
    else:
        state_name = name + '_block'
    filename = f'./blockstates/{state_name}.json'
    f = open(filename, 'w')
    f.write(state)
    f.close()

def make_concrete_states(name, override=None, air=False,coloured=True):
    if coloured:
        for key, value in colours.items():
            make_concrete_state(name, override, air, value)
    else:
        make_concrete_state(name, override, air)


def make_concrete_block(name, air=False, value=None):
    path = 'concrete:block/'
    if value is not None:
        _name = name + '_'+value
    else:
        _name = name

    tex = f'{path}{_name}'
    if value is not None:
        air_name = name + '_air_' + value
    else:
        air_name = name + '_air'
    
    # Makes the layered ones
    for i in range(16):

        if value is not None:
            _name = name + '_layer_' + value
        else:
            _name = name + '_layer'
        filename = f'./models/block/{_name}_{i+1}.json'
        f = open(filename, 'w')
        f.write(make_layer(i+1, tex))
        f.close()

        if air:
            filename = f'./models/block/{air_name}.json'
            # TODO make this actually by the block?
            _air = f'{path}dust_air'
            f = open(filename, 'w')
            f.write(make_block(_air))
            f.close()
        
        # Layer Item
        filename = f'./models/item/{_name}.json'
        f = open(filename, 'w')
        f.write(make_layer_item(path,_name))
        f.close()


    # Full Item
    if value is not None:
        _name = name + '_block_' + value
    else:
        _name = name + '_block'
    filename = f'./models/item/{_name}.json'
    f = open(filename, 'w')
    f.write(make_item(path,_name))
    f.close()

    # Makes the full block
    if value is not None:
        _name = name + '_block_' + value
    else:
        _name = name + '_block'
    filename = f'./models/block/{_name}.json'
    f = open(filename, 'w')
    f.write(make_block(f'{tex}'))
    f.close()

def make_concrete_blocks(name, air=False, coloured=True):
    if coloured:
        for key, value in colours.items():
            make_concrete_block(name, air, value)
    else:
        make_concrete_block(name, air)

make_rebar_state('rebar','wet_concrete', 'level', coloured=False)
make_rebar_state('reinforced_concrete_layer','concrete', 'layers')

for key, value in colours.items():
    path = 'concrete:block/'
    name = 'reinforced_concrete_block_' + value
    tex = f'concrete_block_{value}'
    filename = f'./blockstates/{name}.json'
    f = open(filename, 'w')
    f.write(make_state(path, tex))
    f.close()

    filename = f'./models/item/{name}.json'
    f = open(filename, 'w')
    f.write(make_item(path, tex))
    f.close()

    # Paint brushes
    name = f'paint_brush_{value}'
    path = 'concrete:item/'
    filename = f'./models/item/{name}.json'
    f = open(filename, 'w')
    f.write(make_item_basic(path, tex))
    f.close()

name = f'paint_brush'
path = 'concrete:item/'
filename = f'./models/item/{name}.json'
f = open(filename, 'w')
f.write(make_item_basic(path, tex))
f.close()

make_concrete_states('concrete')
make_concrete_states('wet_concrete',air=True,coloured=False)

make_concrete_blocks('concrete')
make_concrete_blocks('wet_concrete',air=True,coloured=False)