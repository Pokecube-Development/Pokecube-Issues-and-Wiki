
def _process_tex_only(entry, key, model):
    model['key'] = key
    model['model'] = entry.name
    model['anim'] = entry.name
    model['tex'] = key

def _process_model_only(entry, key, model):
    model['key'] = key
    model['model'] = key
    model['anim'] = entry.name
    model['tex'] = entry.name

def _process_not_tex(entry, key, model):
    model['key'] = key
    model['model'] = key
    model['anim'] = key
    model['tex'] = entry.name

def _process_no_custom(entry, key, model):
    model['key'] = key
    model['model'] = entry.name
    model['anim'] = entry.name
    model['tex'] = entry.name

def _process_arceus_silvally(entry, key, model):
    model['model'] = entry.name
    model['anim'] = key.replace('-', '_')
    model['key'] = key.replace('-', '_')
    model['tex'] = key.replace('-', '_')

PROCESSORS = {
    'arceus': _process_arceus_silvally,
    'silvally': _process_arceus_silvally,
    # 'burmy': _process_tex_only,
    'genesect': _process_not_tex,
    'furfrou': _process_no_custom,
    'flabebe': _process_no_custom,
    'floette': _process_no_custom,
    'florges': _process_no_custom,
    'sinistea': _process_model_only,
    'polteageist': _process_model_only,
    'xerneas': _process_tex_only,
}

def process_model(entry, key, model):
    if entry.name in PROCESSORS:
        PROCESSORS[entry.name](entry, key, model)
    return model