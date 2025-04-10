import os
import json

def clean_result(vals):
    result = vals['result']
    if 'item' in result:
        old = result['item']
        del result['item']
        result['id'] = old


for filename in os.listdir("./recipe"):
    file = open(f"./recipe/{filename}")
    vals = json.load(file)
    file.close()
    if 'result' in vals:
        clean_result(vals)
    elif 'recipes' in vals:
        recipes = vals['recipes']
        for recipe in recipes:
            clean_result(recipe['recipe'])
    else:
        print(filename)

    file = open(f"./recipe/{filename}", 'w')
    json.dump(vals, file, indent=2)