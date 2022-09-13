import json
import os

def process(filename):
    file = open('./old/'+filename, 'r')
    input = json.load(file)
    new = {}
    if "config" in input:
        config = input["config"]
    else:
        config = ""
    use_expansion_hack = "team" in filename or "town" in filename or "village" in filename
    adapt = False
    for key, value in input.items():
        if key == "adapt_noise":
            adapt = value
            continue
        if value != config:
            new[key] = value

    for key, value in config.items():
        new[key] = value
    if adapt:
        new["terrain_adaptation"] = "beard_thin"
    new["step"] = "surface_structures"
    new["project_start_to_heightmap"] = "WORLD_SURFACE_WG"

    if "height_type" in new:
        new["project_start_to_heightmap"] = new["height_type"]

    if "y_settings" in new and "surface_type" in new["y_settings"]:
        if new["y_settings"]["surface_type"] == "underground":
            new["step"] = "underground_structures"

    if use_expansion_hack:
        new["use_expansion_hack"] = True

    filename = './new/'+filename
    if not os.path.exists(os.path.dirname(filename)):
        os.makedirs(os.path.dirname(filename))
    json.dump(new, open(filename, 'w'), indent=2, sort_keys=True)

for filename in os.listdir('./old'):
    process(filename)