import utils
import json

TO_MODEL = {
    "basculin-blue-striped" : "basculin-red-striped",
}

RENAMES = {
    # Minior meteors are all the same externally, so the internals are handled in code
    "minior-red-meteor": "minior-meteor",
    # basculin red and blue stripes are essentially cosmetic, so merged to basculin
    "basculin-red-striped": "basculin",
}

LEGACY_REV_MAP = {
    "missingno": "missingno",
    "Sirfetch'd" : "sirfetchd",
    "Mr. Mime Galar" : "mr-mime-galar",
    "Mr. Mime" : "mr-mime",
    "Mr. Rime" : "mr-rime",
    "Wormadam" : "wormadam-plant",
    "Darmanitan" : "darmanitan-standard",
    "Darmanitan Galar" : "darmanitan-galar-standard",
    "Meowstic" : "meowstic-male",
    "meowstic" : "meowstic-male",
    "Gourgeist" : "gourgeist-average",
    "Lycanroc" : "lycanroc-midday",
    "Urshifu Single" : "urshifu-single-strike",
    "Urshifu Rapid" : "urshifu-rapid-strike",
    "orange_core_minior" : "minior",
    "blue_core_minior" : "minior",
    "green_core_minior" : "minior",
    "indigo_core_minior" : "minior",
    "red_core_minior" : "minior",
    "violet_core_minior" : "minior",
    "yellow_core_minior" : "minior",
    "genesectbdrive": "genesect",
    "genesectcdrive": "genesect",
    "genesectddrive": "genesect",
    "genesectsdrive": "genesect",
    "googra_hisui": "goodra-hisui",
    "cherrim_sunny": "cherrim",
    "basculegion": "basculegion-make",
    "Solgaleo Dusk": "necrozma-dusk",
    "Lunala Dawn": "necrozma-dawn",
    "Calyrex Ice Rider": "calyrex-ice",
    "Calyrex Shadow Rider": "calyrex-shadow",
    "Greninja Ash": "greninja-battle-bond",
}

LEGACY_MAP = {
    "missingno": "missingno",
    "castform-sunny" : "castform_sun",
    "castform-rainy" : "castform_rain",
    "castform-snowy" : "castform_snow",
    "deoxys-normal" : "deoxys",
    "wormadam-plant" : "wormadam",
    "giratina-altered" : "giratina",
    "basculin-red-striped" : "basculin",
    "basculin-white-striped" : "basculin_hisui",
    "darmanitan-standard" : "darmanitan",
    "darmanitan-galar-standard" : "darmanitan_galar",
    "darmanitan-galar-zen" : "darmanitan_zen_galar",
    "keldeo-ordinary" : "keldeo",
    "greninja-battle-bond" : "greninja_ash",
    "meowstic-male" : "meowstic",
    # I guess we need to add one of these separate form
    # "meowstic-female" : "",
    "pumpkaboo-average" : "pumpkaboo",
    "gourgeist-average" : "gourgeist",
    "zygarde-complete" : "zygarde_100",
    "hoopa" : "hoopa_confined",
    "oricorio-pom-pom" : "oricorio_pom-pom",
    "lycanroc-midday" : "lycanroc",
    "wishiwashi-solo" : "wishiwashi",
    "minior-red-meteor" : "minior",
    "minior-orange-meteor" : "minior",
    "minior-yellow-meteor" : "minior",
    "minior-green-meteor" : "minior",
    "minior-blue-meteor" : "minior",
    "minior-indigo-meteor" : "minior",
    "minior-violet-meteor" : "minior",
    "minior-red" : "red_core_minior",
    "minior-orange" : "orange_core_minior",
    "minior-yellow" : "yellow_core_minior",
    "minior-green" : "green_core_minior",
    "minior-blue" : "blue_core_minior",
    "minior-indigo" : "indigo_core_minior",
    "minior-violet" : "violet_core_minior",
    "mimikyu-disguised" : "mimikyu",
    "mimikyu-busted" : "mimikyu",
    "necrozma-dusk" : "solgaleo_dusk",
    "necrozma-dawn" : "lunala_dawn",
    "toxtricity-low-key" : "toxtricity_lowkey",
    "toxtricity-amped-gmax" : "toxtricity_amped_gigantamax",
    "toxtricity-low-key-gmax" : "toxtricity_lowkey_gigantamax",
    "eiscue-ice" : "eiscue",
    "indeedee-male" : "indeedee",
    "morpeko-full-belly" : "morpeko",
    "eternatus-eternamax" : "eternatus_gigantamax",
    "urshifu-single-strike" : "urshifu_single",
    "urshifu-rapid-strike" : "urshifu_rapid",
    "urshifu-single-strike-gmax" : "urshifu_single_gigantamax",
    "urshifu-rapid-strike-gmax" : "urshifu_rapid_gigantamax",
    "calyrex-ice" : "calyrex_ice_rider",
    "calyrex-shadow" : "calyrex_shadow_rider",
}

IGNORED_FORMS = [
    # We handle scatterbug and spewpa with just vivilon.
    "scatterbug-icy-snow",
    "scatterbug-polar",
    "scatterbug-tundra",
    "scatterbug-continental",
    "scatterbug-garden",
    "scatterbug-elegant",
    "scatterbug-meadow",
    "scatterbug-modern",
    "scatterbug-marine",
    "scatterbug-archipelago",
    "scatterbug-high-plains",
    "scatterbug-sandstorm",
    "scatterbug-river",
    "scatterbug-monsoon",
    "scatterbug-savanna",
    "scatterbug-sun",
    "scatterbug-ocean",
    "scatterbug-jungle",
    "scatterbug-fancy",
    "scatterbug-poke-ball",            
    "spewpa-icy-snow",
    "spewpa-polar",
    "spewpa-tundra",
    "spewpa-continental",
    "spewpa-garden",
    "spewpa-elegant",
    "spewpa-meadow",
    "spewpa-modern",
    "spewpa-marine",
    "spewpa-archipelago",
    "spewpa-high-plains",
    "spewpa-sandstorm",
    "spewpa-river",
    "spewpa-monsoon",
    "spewpa-savanna",
    "spewpa-sun",
    "spewpa-ocean",
    "spewpa-jungle",
    "spewpa-fancy",
    "spewpa-poke-ball",

    # All mothims are the same, so no forms for it.
    "mothim-plant",
    "mothim-sandy",
    "mothim-trash",

    # We don't care for the spiky pichu.
    "pichu",
    "pichu-spiky-eared",
]

TAG_IGNORE = [
    "minior",
    "minior-red-meteor",
    "basculin-red-striped",
]

NOT_EXTRA_FORMS = [

]

IS_EXTRA_FORM = [
    "tauros-paldea-aqua-breed",
    "tauros-paldea-blaze-breed",
    "tauros-paldea-combat-breed",
]

def is_extra_form(name):
    if name in IS_EXTRA_FORM:
        return True
    if '-galar' in name:
        return False
    if '-hisui' in name:
        return False
    if '-alola' in name:
        return False
    if '-paldea' in name:
        return False
    return not name in NOT_EXTRA_FORMS

def banned_form(name):
    return name in IGNORED_FORMS

def to_model_form(new_name, species, dex):
    if new_name in TO_MODEL:
        old_name = TO_MODEL[new_name]
        return old_name
    return None

def entry_name(name):
    if name in RENAMES:
        return RENAMES[name]
    return name

def find_new_name(old_name, options):
    if old_name in LEGACY_REV_MAP:
        return LEGACY_REV_MAP[old_name]

    for key, value in LEGACY_MAP.items():
        if value == old_name:
            return key

    old_name = utils.trim(old_name)
    cleaned = old_name.replace('gigantamax', 'gmax')
    cleaned = cleaned.replace('-', '')
    cleaned = cleaned.replace('_', '')
    cleaned = cleaned.replace(' ', '')
    for new_name in options:
        test = utils.trim(new_name)
        test = test.replace(' ', '')
        test = test.replace('-', '')
        test = test.replace('_', '')
        if(test==cleaned):
            return new_name
    return None


def find_old_name(new_name, species, dex):

    if new_name in LEGACY_MAP:
        old_name = LEGACY_MAP[new_name]
        if old_name in dex:
            return old_name

    old_name = utils.trim(new_name)

    if not old_name in dex and '-' in old_name:
        # Try - -> _?
        name = old_name.replace('-', '_')
        if(name in dex):
            old_name = name
    if not old_name in dex and '-gmax' in old_name:
        # Try gmax?
        old_name = old_name.replace('-gmax', '_gigantamax')
    if not old_name in dex and '-mega' in old_name:
        # Try mega?
        old_name = old_name.replace('-mega', '_mega')
    if not old_name in dex:
        name = old_name.replace('-', '')
        if(name in dex):
            old_name = name

    if old_name in dex:
        return old_name
    return None

def get_interacts(index_map):
    res = {}
    old_file = "./old/pokemobs/pokemobs_interacts.json"
    json_in = open(old_file, 'r', encoding='utf-8')
    json_str = json_in.read()
    json_in.close()
    json_obj = json.loads(json_str)
    for entry in json_obj["pokemon"]:
        if 'stats' in entry:
            name = find_new_name(entry['name'], index_map.keys())
            res[name] = entry['stats']
    return res
