TOIGNORE = [
    # Ignore all of the extra pikachus
    "pikachu-rock-star",
    "pikachu-belle",
    "pikachu-pop-star",
    "pikachu-phd",
    "pikachu-libre",
    "pikachu-cosplay",
    "pikachu-original-cap",
    "pikachu-hoenn-cap",
    "pikachu-sinnoh-cap",
    "pikachu-unova-cap",
    "pikachu-kalos-cap",
    "pikachu-alola-cap",
    "pikachu-partner-cap",
    "pikachu-starter",
    "pikachu-world-cap",

    # These two just differ by ability, so we need to handle later
    "zygarde-10-power-construct",
    "zygarde-50-power-construct",
    "rockruff-own-tempo",

    # Not sure about these
    "magearna-original",
    "zarude-dada",

    # Cosmetic only
    "maushold-family-of-three",
    # "dudunsparce-three-segment", keeping this for now as it also changes sizes...

    "koraidon-limited-build",
    "koraidon-sprinting-build",
    "koraidon-swimming-build",
    "koraidon-gliding-build",

    "miraidon-low-power-mode",
    "miraidon-glide-mode",
    "miraidon-drive-mode",
    "miraidon-aquatic-mode",
]

NO_WORDS = [
    '-totem-'
]

NO_END = [
    '-totem',
    '-starter'
]

def isIgnored(name):
    if name in TOIGNORE:
        return True
    for var in NO_WORDS:
        if var in name:
            return True
    for var in NO_END:
        if name.endswith(var):
            return True
    return False