import json

import utils
import os
from utils import get_moves_index, get_move, trim, default_or_latest, url_to_id

MANUAL_RENAMES = {
    "lastout": "lash-out",
    "roud": "round"
}

LEGENDS_ARCEUS = [
    "barb-barrage",
    "bitter-malice",
    "bleakwind-storm",
    "ceaseless-edge",
    "chloroblast",
    "dire-claw",
    "esper-wing",
    "headlong-rush",
    "infernal-parade",
    "lunar-blessing",
    "mountain-gale",
    "mystical-power",
    "power-shift",
    "psyshield-bash",
    "raging-fury",
    "sandsear-storm",
    "shelter",
    "springtide-storm",
    "stone-axe",
    "take-heart",
    "triple-arrows",
    "victory-dance",
    "wave-crash",
    "wildbolt-storm",
]

SPECIAL_CONTACT = {
    "petal-dance",
    "trump-card",
    "wring-out",
    "grass-knot",
    "draining-kiss",
    "infestation",
}

PHYSICAL_RANGED = {
    "attack-order",
    "aura-wheel",
    "barrage",
    "beak-blast",
    "beat-up",
    "bone-club",
    "bone-rush",
    "bonemerang",
    "bulldoze",
    "bullet-seed",
    "diamond-storm",
    "dragon-darts",
    "drum-beating",
    "earthquake",
    "egg-bomb",
    "explosion",
    "feint",
    "fissure",
    "fling",
    "freeze-shock",
    "fusion-bolt",
    "grav-apple",
    "gunk-shot",
    "ice-shard",
    "icicle-crash",
    "icicle-spear",
    "land's-wrath",
    "leafage",
    "magnet-bomb",
    "magnitude",
    "metal-burst",
    "meteor-assault",
    "natural-gift",
    "pay-day",
    "petal-blizzard",
    "pin-missile",
    "poison-sting",
    "poltergeist",
    "precipice-blades",
    "present",
    "psycho-cut",
    "pyro-ball",
    "razor-leaf",
    "rock-blast",
    "rock-slide",
    "rock-throw",
    "rock-tomb",
    "rock-wrecker",
    "sacred-fire",
    "sand-tomb",
    "scale-shot",
    "secret-power",
    "seed-bomb",
    "self-destruct",
    "shadow-bone",
    "sky-attack",
    "smack-down",
    "sinister-arrow-raid",
    "spike-cannon",
    "spirit-shackle",
    "splintered-stormshards",
    "stone-edge",
    "thousand-arrows",
    "thousand-waves",
    "twineedle",
}

index_map = get_moves_index()
move_modifications = {}

mod_file = "./data/moves/move_adjustments.json"
arr = json.load(open(mod_file, 'r'))
for var in arr:
    if 'name' in var:
        key = var['name']
        move_modifications[key] = var

OUTPUT_FLAVOUR_TEXT = False

def is_english(details):
    return details.language.name == 'en'

class MoveEntry:
    def __init__(self, move) -> None:
        self.name = move.name
        self.id = move.id
        self.power = move.power
        self.pp = move.pp
        self.priority = move.priority
        self.type = move.type.name
        self.accuracy = move.accuracy
        self.target = move.target.name
        self.damage_class = move.damage_class.name

        flavor_text = default_or_latest(move.flavor_text_entries, is_english)
        if flavor_text is not None:
            self.flavor_text = flavor_text.flavor_text.replace('\n', ' ')

        if move.meta is not None:
            self.move_category = move.meta.category.name
            if move.meta.flinch_chance != 0:
                self.flinch_chance = move.meta.flinch_chance
            if move.meta.crit_rate != 0:
                self.crit_rate = move.meta.crit_rate
            if move.meta.drain != 0:
                self.drain = move.meta.drain
            if move.meta.healing != 0:
                self.healing = move.meta.healing
            if move.meta.stat_chance != 0:
                self.stat_chance = move.meta.stat_chance
            if move.meta.ailment_chance != 0:
                self.ailment_chance = move.meta.ailment_chance
            if move.meta.ailment is not None:
                self.ailment = move.meta.ailment.name

            self.max_hits = move.meta.max_hits
            self.min_hits = move.meta.min_hits
            self.max_turns = move.meta.max_turns
            self.min_turns = move.meta.min_turns
        else:
            print(f'no meta for {move.name}??')

        for entry in move.effect_entries:
            if entry.language.name == 'en':
                self.effect_text_extend = entry.effect
                self.effect_text_simple = entry.short_effect

                if "User foregoes its next turn to recharge." in self.effect_text_simple:
                    self.cooldown = 3.0

        if move.effect_chance is not None:
            self.effect_chance = move.effect_chance

        keys = [x for x in self.__dict__.keys()]
        for x in keys:
            if self.__dict__[x] is None:
                del self.__dict__[x]

def convert_old_move_name(old):
    old = trim(old)

    if old in MANUAL_RENAMES:
        return MANUAL_RENAMES[old]

    for name in index_map.keys():
        if name == old:
            return name
        test = name.replace('-', '')
        oldtest = old.replace('_', '')
        if test == oldtest:
            return name

    for name in LEGENDS_ARCEUS:
        if name == old:
            return name
        test = name.replace('-', '')
        if test == old:
            return name

    print(f'error converting old name for {old}')
    return None

def convert_animation(move_name, old_animation):

    # Start with the preset type.
    preset = old_animation['preset']
    if ':' in preset or not 'preset_values' in old_animation:
        new_animation = {}
        # We need to convert to new format
        args = preset.split(':')
        name = args[0]
        new_animation['preset'] = name

        if 'duration' in old_animation:
            new_animation['duration'] = int(old_animation['duration'])
        if 'starttick' in old_animation:
            new_animation['starttick'] = int(old_animation['starttick'])

        if 'volume' in old_animation:
            new_animation['volume'] = old_animation['volume']
        if 'pitch' in old_animation:
            new_animation['pitch'] = old_animation['pitch']

        if 'sound' in old_animation:
            new_animation['sound'] = old_animation['sound']
        if 'soundSource' in old_animation:
            new_animation['soundSource'] = old_animation['soundSource']
        if 'soundTarget' in old_animation:
            new_animation['soundTarget'] = old_animation['soundTarget']

        if 'applyAfter' in old_animation:
            new_animation['applyAfter'] = old_animation['applyAfter']

        values = {}
        for i in range(1,len(args)):
            arg = args[i]
            # Old system first character was what was going on.
            t = arg[0]
            val = arg[1:len(arg)]

            try:
                # Now to handle the things.
                if t == 'p':
                    values['particle'] = val
                elif t == 'a':
                    values['absolute'] = val == 'true'
                elif t == 'd':
                    values['density'] = float(val)
                elif t == 'w':
                    values['width'] = float(val)
                elif t == 'l':
                    values['lifetime'] = int(val)
                elif t == 'r':
                    values['reverse'] = val == 'true'
                elif t == 'c':
                    values['rgba_string'] = val
                elif t == 'f':
                    # This one depends strongly on the old preset.
                    if name == 'cartFunc':
                        sub_args = val.split(',')
                        values['f_x'] = sub_args[0]
                        values['f_y'] = sub_args[1]
                        values['f_z'] = sub_args[2]
                        pass
                    elif name == 'cylFunc':
                        sub_args = val.split(',')
                        values['f_radial'] = sub_args[0]
                        values['f_phi'] = sub_args[1]
                        pass
                    elif name == 'sphFunc':
                        sub_args = val.split(',')
                        values['f_radial'] = sub_args[0]
                        values['f_theta'] = sub_args[1]
                        values['f_phi'] = sub_args[2]
                        pass
                    elif name == 'flow':
                        values['flat'] = True
                        values['angle'] = float(val)
                    else:
                        print(f'unknown f key for preset {name} {preset} for {move_name}')
                else:
                    print(f'unknown key {t} for preset {name} {preset} for {move_name}')
            except Exception as err:
                print(f'error with key {t} {val} ({arg}) for preset {name} {preset} for {move_name}')
                print(err)
        new_animation['preset_values'] = values
        return new_animation
    return old_animation


def convert_moves():

    old_moves = './old/moves/moves.json'
    file = open(old_moves, 'r')
    data = file.read()
    file.close()
    old_moves = json.loads(data)
    moves_dex = {}
    for var in old_moves["moves"]:
        moves_dex[var["name"]] = var

    anims_dex = {}
    for filename in os.listdir('./data/moves/animations/'):
        file = open(f'./data/moves/animations/{filename}', 'r')
        data = file.read()
        data =  json.loads(data)
        anims_dex[data['name']] = data['animations']

    lang_files = {}
    lang_desc = {}

    contact = []
    ranged = []
    z_moves = []
    d_moves = []

    move_entries = []

    # Dump each move, and collect langs and tags
    for name, index in index_map.items():
        move = get_move(index)
        entry = MoveEntry(move)

        langs = []
        # These will go in langs
        for __name in move.names:
            _name = __name.name
            lang = utils.get('language', url_to_id(__name.language))
            langs.append(url_to_id(__name.language))
            key = f'{lang.iso639}_{lang.iso3166}.json'
            items = {}
            if key in lang_files:
                items = lang_files[key]
            items[f"pokemob.move.{entry.name}"] = _name
            lang_files[key] = items

        # These will go in langs
        for lang in langs:
            def is_lang(details):
                return url_to_id(details.language) == lang
            text = default_or_latest(move.flavor_text_entries, is_lang)
            if text is None:
                continue
            lang = utils.get('language', lang)
            key = f'{lang.iso639}_{lang.iso3166}.json'
            items = {}
            if key in lang_desc:
                items = lang_desc[key]
            text = text.flavor_text.replace('\n', ' ')
            items[f"pokemob.move.{name}.desc"] = text
            lang_desc[key] = items
            if 'Z-Power' in text and not name in z_moves:
                z_moves.append(name)
            if 'attack Dynamax ' in text and not name in d_moves:
                d_moves.append(name)

        # These will go in tags
        if name in SPECIAL_CONTACT:
            contact.append(name)
        elif name in PHYSICAL_RANGED:
            ranged.append(name)
        elif move.damage_class.name == "physical":
            contact.append(name)
        else:
            ranged.append(name)

        move_entries.append(entry)

        values = entry.__dict__
        if name in move_modifications:
            overrides = move_modifications[name]
            for key, value in overrides.items():
                values[key] = value

        # Dump the entry file
        file = f'../../src/generated/resources/data/pokecube_mobs/database/moves/entries/{name}.json'
        if not os.path.exists(os.path.dirname(file)):
            os.makedirs(os.path.dirname(file))
        file = open(file, 'w', encoding='utf-8')
        json.dump(values, file, indent=2, ensure_ascii=False)
        file.close()

    # Post process for the "special" z-moves
    for name, _ in index_map.items():
        if name.endswith('--special') and name.replace('--special', '--physical') in z_moves and not name in z_moves:
            z_moves.append(name)

    if OUTPUT_FLAVOUR_TEXT:
        for key, dict in lang_files.items():
            if key in lang_desc:
                dict2 = lang_desc[key]
                for key2, value in dict2.items():
                    dict[key2] = value

    # Dump the lang files
    for key, dict in lang_files.items():
        file = f'../../src/generated/resources/assets/pokecube_moves/lang/{key}'
        if not os.path.exists(os.path.dirname(file)):
            os.makedirs(os.path.dirname(file))
        try:
            file = open(file, 'w', encoding='utf-8')
            json.dump(dict, file, indent=2, ensure_ascii=False)
            file.close()
        except Exception as err:
            print(f'error saving for {key}')
            print(err)

    # Dump animation files
    for name, value in anims_dex.items():
        new_name = convert_old_move_name(name)
        if new_name is not None:
            file = f'../../src/generated/resources/data/pokecube_mobs/database/moves/animations/{new_name}.json'
            output = {"name":new_name, "animations":value}
            anims = []
            for var in output["animations"]:
                var["preset"] = var["preset"].split(":~")[0]
                anims.append(convert_animation(new_name, var))
            output["animations"] = anims
            if not os.path.exists(os.path.dirname(file)):
                os.makedirs(os.path.dirname(file))
            file = open(file, 'w', encoding='utf-8')
            json.dump(output, file, indent=2, ensure_ascii=False)
            file.close()
        else:
            print(f'unknown animation: {name}')

    # Dump ranged and contact tags
    file = f'../../src/generated/resources/data/pokecube/tags/pokemob_moves/contact-moves.json'
    if not os.path.exists(os.path.dirname(file)):
        os.makedirs(os.path.dirname(file))
    tag = {"replace":False,"values":contact}
    file = open(file, 'w', encoding='utf-8')
    json.dump(tag, file, indent=2, ensure_ascii=False)
    file.close()
    file = f'../../src/generated/resources/data/pokecube/tags/pokemob_moves/ranged-moves.json'
    if not os.path.exists(os.path.dirname(file)):
        os.makedirs(os.path.dirname(file))
    tag = {"replace":False,"values":ranged}
    file = open(file, 'w', encoding='utf-8')
    json.dump(tag, file, indent=2, ensure_ascii=False)
    file.close()

    # Dump Z moves
    file = f'../../src/generated/resources/data/pokecube/tags/pokemob_moves/z-move.json'
    if not os.path.exists(os.path.dirname(file)):
        os.makedirs(os.path.dirname(file))
    tag = {"replace":False,"values":z_moves}
    file = open(file, 'w', encoding='utf-8')
    json.dump(tag, file, indent=2, ensure_ascii=False)
    file.close()

    # Dump D moves
    file = f'../../src/generated/resources/data/pokecube/tags/pokemob_moves/d-move.json'
    if not os.path.exists(os.path.dirname(file)):
        os.makedirs(os.path.dirname(file))
    tag = {"replace":False,"values":d_moves}
    file = open(file, 'w', encoding='utf-8')
    json.dump(tag, file, indent=2, ensure_ascii=False)
    file.close()


    # Print any that errored
    for name, value in moves_dex.items():
        new_name = convert_old_move_name(name)
        if new_name is None:
            print(f'unknown move: {name}')

if __name__ == "__main__":
    convert_moves()