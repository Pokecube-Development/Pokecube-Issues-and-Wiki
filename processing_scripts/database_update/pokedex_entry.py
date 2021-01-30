import csv_loader
import moves_names

def getSingle(name, data, file, value):
    return data.get_info(name,value, expected_file=file, use_names_map=True)[file][0]

def getExpYield(name, data):
    return int(getSingle(name, data,"pokemon" ,"base_experience"))
def getHeight(name, data):
    return int(getSingle(name, data,"pokemon" ,"height")) / 10.0
def getWeight(name, data):
    return int(getSingle(name, data,"pokemon" ,"weight")) / 10.0
    
def getGenderRatio(name, data):
    rates = {}
    val = int(getSingle(name, data,"pokemon_species" ,"gender_rate"))
    rates[-1] = 255
    rates[0] = 0
    rates[1] = 30
    rates[2] = 62
    rates[4] = 127
    rates[6] = 191
    rates[7] = 225
    rates[8] = 254
    return rates[val]
def getCaptureRate(name, data):
    return int(getSingle(name, data,"pokemon_species" ,"capture_rate"))
def getBaseFriendship(name, data):
    return int(getSingle(name, data,"pokemon_species" ,"base_happiness"))
def getExpMode(name, data):
    stats = data.get_info(name,"growth_rate_id", expected_file="pokemon_species", use_names_map=True)["pokemon_species"]
    _id = stats[0]
    stats = data.get_entry(_id, expected_file="growth_rate_prose", use_names_map=True)["growth_rate_prose"]
    for row in stats:
        _id = row[1]
        if _id == '9':
            return row[2]
    return None

def getLevelMoves(name, data):
    moves = {}
    names = []
    try:
        moves_entries = data.get_entry(name, expected_file="pokemon_moves", use_names_map=True)["pokemon_moves"]
        version = 1
        # First, locate the most recent version that has lvl up moves
        for entry in moves_entries:
            if entry[4] == "0":
                continue
            vers = int(entry[1])
            if vers > version:
                version = vers
        version = str(version)
        # Now we can actually parse the moves
        for entry in moves_entries:
            # TODO figure out if a move is an evolution move, is that info here?
            if entry[4] == "0":
                continue
            if entry[1] != version:
                continue
            level = entry[4]
            move_id = entry[2]
            move = data.get_info(move_id,"identifier", expected_file="moves")["moves"][0]
            move_, conf = csv_loader.match_name(move, moves_names.moves)
            if(conf < 80):
                print("{} -> {} ({})".format(move, move_, conf))
            else:
                move = move_
            if level in moves.keys():
                moves[level] = moves[level]+","+move
            else:
                moves[level] = move
            if not move in names:
                names.append(move)
    except:
        print("No moves found for {}".format(name))
    return moves, names

def getAllMoves(name, data, exclude=[]):
    names = []
    try:
        moves_entries = data.get_entry(name, expected_file="pokemon_moves", use_names_map=True)["pokemon_moves"]
        for entry in moves_entries:
            move_id = entry[2]
            move = data.get_info(move_id,"identifier", expected_file="moves")["moves"][0]
            move_, conf = csv_loader.match_name(move, moves_names.moves)
            if(conf < 80):
                print("{} -> {} ({})".format(move, move_, conf))
            else:
                move = move_
            if move in exclude or move in names:
                continue
            names.append(move)
    except:
        print("No moves found for {}".format(name))
    return names

def getTypes(name, data):
    types_nums = data.get_info(name,"type_id", expected_file="pokemon_types", use_names_map=True)["pokemon_types"]
    types = []
    for num in types_nums:
        names = data.get_info(num,"identifier", expected_file="types")["types"]
        types.append(names[0])
    return types

def getStats(name, data):
    stats = []
    # TODO maybe validate that these are in the correct order, the index is also stored
    # in the csv file, so that validation can be done if needed!
    stats = data.get_info(name,"base_stat", expected_file="pokemon_stats", use_names_map=True)["pokemon_stats"]
    return stats

def getEVs(name, data):
    stats = []
    # TODO maybe validate that these are in the correct order, the index is also stored
    # in the csv file, so that validation can be done if needed!
    stats = data.get_info(name,"effort", expected_file="pokemon_stats", use_names_map=True)["pokemon_stats"]
    return stats

def getAbilities(name, data):
    hidden = []
    abilities = ["",""]
    rows = data.get_entry(name, expected_file="pokemon_abilities", use_names_map=True)["pokemon_abilities"]
    for row in rows:
        ability_id = row[1]
        isHidden = row[2]
        slot = int(row[3]) - 1
        ability_name = data.get_info(ability_id,"identifier", expected_file="abilities")["abilities"][0]
        if ability_name == '':
             continue
        
        if isHidden == "1":
            hidden.append(ability_name)
        elif slot < len(abilities):
            abilities[slot] = ability_name
    return abilities, hidden

def sorter(e):
    return int(e)

class Pokedex(object):
    def __init__(self, names, data):
        self.pokemon = []
        for name in names:
            try:
                entry = PokedexEntry(name, data)
                self.pokemon.append(entry.map)
            except Exception as err:
                print("Error with {} {}".format(name, err))

class PokedexEntry(object):

    def __init__(self, name, data):

        do_stats = True
        do_moves = False

        _map = self.map = {}
        _map["name"] = name

        if do_stats:
            _map["number"] = int(getSingle(name, data, "pokemon", "species_id"))
            id = int(getSingle(name, data, "pokemon", "id"))
            is_default = id == _map["number"]
            if(is_default):
                _map["base"] = True
            _map["stats"] = {}
            statsOrder = ["hp", "atk", "def", "spatk", "spdef", "spd"]
            # Do the base stats
            stats = getStats(name, data)
            _map["stats"]["stats"] = {}
            values = _map["stats"]["stats"]["values"] = {}
            for i in range(len(statsOrder)):
                values[statsOrder[i]] = stats[i]

            # Do the evs
            stats = getEVs(name, data)
            _map["stats"]["evs"] = {}
            values = _map["stats"]["evs"]["values"] = {}
            for i in range(len(statsOrder)):
                if stats[i] == "0":
                    continue
                values[statsOrder[i]] = stats[i]

            # Get the types
            types = getTypes(name,data)
            _map["stats"]["types"] = {}
            values = _map["stats"]["types"]["values"] = {}
            for i in range(len(types)):
                ident = "type{}".format(i+1)
                values[ident] = types[i]

            # Get Abilities
            abilities, hidden = getAbilities(name, data)
            
            _map["stats"]["abilities"] = {}
            values = _map["stats"]["abilities"]["values"] = {}
            if len(abilities) > 0:
                normals = abilities[0]
                if len(abilities) > 1:
                    for i in range(1, len(abilities)):
                        if abilities[i] != "":
                            normals = normals +", "+abilities[i]
                values["normal"] = normals
            if len(hidden) > 0:
                hiddens = hidden[0]
                if len(hidden) > 1:
                    for i in range(1, len(hidden)):
                        if hidden[i] != "":
                            hiddens = hiddens +", "+hidden[i]
                values["hidden"] = hiddens

            # Get the simple values
            _map["stats"]["mass"] = getWeight(name, data)
            _map["stats"]["baseExp"] = getExpYield(name, data)

            # This set is not defined for all targets, so try/except them
            try:
                _map["stats"]["captureRate"] = getCaptureRate(name, data)
            except:
                pass
            try:
                _map["stats"]["baseFriendship"] = getBaseFriendship(name, data)
            except:
                pass
            try:
                _map["stats"]["genderRatio"] = getGenderRatio(name, data)
            except:
                pass
            try:
                _map["stats"]["expMode"] = getExpMode(name, data)
            except:
                pass

        
        if do_moves:
            # Do the moves
            # First lvl up moves
            moves, names = getLevelMoves(name, data)
            moves_list = getAllMoves(name, data, exclude=names)

            if len(moves) != 0 or len(moves_list) != 0:
                _map["moves"] = {}
                
            if len(moves) > 0:
                lvlMoves = _map["moves"]["lvlupMoves"] = {}
                levels = [x for x in moves.keys()]
                levels.sort(key=sorter)
                for level in levels:
                    lvlMoves[level] = moves[level]

            # Then remainder
            moves = ""
            if len(moves_list)>0:
                moves = moves_list[0]
                for i in range(1, len(moves_list)):
                    moves = moves +", "+moves_list[i]
                misc = _map["moves"]["misc"] = {}
                misc["moves"] = moves
