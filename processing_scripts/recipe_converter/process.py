import os
import json

def clean_result(vals):
    result = vals['result']
    if 'item' in result:
        old = result['item']
        del result['item']
        result['id'] = old
    if isinstance(result, str) and "count" in vals:
        _result = {}
        _result['id'] = result
        _result['count'] = vals['count']
        del vals['count']
        vals['result'] = _result


def process_file(filename):
    file = open(filename)
    vals = json.load(file)
    file.close()
    if 'result' in vals:
        clean_result(vals)
    elif 'recipes' in vals:
        recipes = vals['recipes']
        for recipe in recipes:
            clean_result(recipe['recipe'])

    file = open(filename, 'w')
    json.dump(vals, file, indent=2)

def process_dir(dirname):
    for filename in os.listdir(dirname):
        path = f"{dirname}/{filename}"
        if os.path.isdir(path):
            process_dir(path)
            continue
        process_file(path)

process_dir("./recipe")