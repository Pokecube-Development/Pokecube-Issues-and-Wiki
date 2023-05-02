import json
from ignore_list import isIgnored
from legacy_renamer import find_old_name, to_model_form, find_new_name, entry_name
import utils
from utils import get_form, get_pokemon, get_species, default_or_latest, get_pokemon_index, url_to_id
from moves_converter import convert_old_move_name
import os
from glob import glob
import shutil

def _make_root(type):
    root =\
f'''{{
  "display": {{
    "icon": {{
      "item": "pokecube:pokecube"
    }},
    "title": {{
      "translate": "achievement.pokecube.{type}.root"
    }},
    "description": {{
      "translate": "achievement.pokecube.{type}.root.desc"
    }},
    "background": "minecraft:textures/gui/advancements/backgrounds/adventure.png",
    "show_toast": false,
    "announce_to_chat": false
  }},
  "criteria": {{
    "get_first_pokemob": {{
      "trigger": "pokecube:get_first_pokemob"
    }}
  }}
}}
'''
    return root

def _make_advancement(mob, folder, type, parent=None, icon='pokecube'):
    if parent is None:
        parent = 'root'
    root =\
f'''{{
  "display": {{
    "icon": {{
      "item": "pokecube:{icon}"
    }},
    "title": {{
      "translate": "achievement.pokecube.{type}",
      "with": [
        {{
          "translate": "entity.pokecube.{mob}"
        }}
      ]
    }},
    "description": {{
      "translate": "achievement.pokecube.{type}.desc",
      "with": [
        {{
          "translate": "entity.pokecube.{mob}"
        }}
      ]
    }}
  }},
  "criteria": {{
    "{type}_{mob}": {{
      "trigger": "pokecube:{type}",
      "conditions": {{
        "entry": "{mob}"
      }}
    }}
  }},
  "parent": "pokecube_mobs:{folder}/{parent}"
}}'''
    return root


GET_FIRST = \
f'''{{
  "display": {{
    "icon": {{
      "item": "pokecube:pokecube"
    }},
    "title": {{
      "translate": "achievement.pokecube.get1st"
    }},
    "description": {{
      "translate": "achievement.pokecube.get1st.desc"
    }}
  }},
  "parent": "pokecube_mobs:capture/root",
  "criteria": {{
    "get_first_pokemob": {{
      "trigger": "pokecube:get_first_pokemob"
    }}
  }}
}}
'''

def make_advancments(mob, advancements_dir):
    if mob == 'missingno':
      return

    file = f'{advancements_dir}capture/get_first_pokemob.json'
    if not os.path.exists(os.path.dirname(file)):
        os.makedirs(os.path.dirname(file))
    if not os.path.exists(file):
        file = open(file, 'w')
        file.write(GET_FIRST)
        file.close()

    keys = {'capture':'catch', 'hatch':'hatch', 'kill':'kill'}
    parents = {'capture': 'get_first_pokemob'}

    for key, value in keys.items():
        file = f'{advancements_dir}{key}/root.json'
        if not os.path.exists(os.path.dirname(file)):
            os.makedirs(os.path.dirname(file))
        if not os.path.exists(file):
            file = open(file, 'w')
            file.write(_make_root(value))
            file.close()
        file = f'{advancements_dir}{key}/{value}_{mob}.json'
        file = open(file, 'w')
        parent = parents[key] if key in parents else None
        file.write(_make_advancement(mob, key, value, parent))
        file.close()
