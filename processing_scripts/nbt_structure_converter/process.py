import os
from nbt import nbt


def extract_copy_mob(caps):
    if not 'thutcore:copymob' in caps:
        return None
    copy = caps["thutcore:copymob"]
    id = copy['id']
    newTag = nbt.TAG_Compound()
    newTag['id'] = id

    tag = copy['tag']['ForgeData']
    if "statue:over_tex" in tag:
        newTag["statue:over_tex"] = tag["statue:over_tex"]
    tag = copy['tag']['ForgeCaps']
    if "pokecube:genetics" in tag:
        genes = tag["pokecube:genetics"]['V']
        for gene in genes:
            if str(gene['K']) == 'pokecube:size':
                size = gene['V']['expressed']['V']
                if(str(size)!='1.0'):
                    newTag['size'] = size
            if str(gene['K']) == 'pokecube:colour':
                colour = gene['V']['expressed']['V']
                if str(colour) != '[255, 255, 255, 255]':
                    newTag['colour'] = colour
    return newTag

def process_file(file):
    _nbt = nbt.NBTFile(file, 'rb')
    for n, v in _nbt.items():
        if(n == 'blocks'):
            for tag in v:
                if not 'nbt' in tag:
                    continue
                _bnbt = tag['nbt']
                if not 'ForgeCaps' in _bnbt:
                    continue
                _caps = _bnbt['ForgeCaps']
                copy = extract_copy_mob(_caps)
                if copy != None:
                    del _bnbt['ForgeCaps']
                    _bnbt['components'] = nbt.TAG_Compound()
                    _bnbt['components']['thutcore:copy_mob'] = copy
    _nbt.write_file(file)


def process_dir(dirname):
    for file in os.listdir(dirname):
        file = f"{dirname}/{file}"
        if os.path.isdir(file):
            process_dir(file)
        else:
            try:
                process_file(file)
            except Exception as err:
                print(file, err)
process_dir("./structure")