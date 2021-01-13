import os
import sys

from unk_pokemobs import custom_names

from fuzzywuzzy import fuzz
from fuzzywuzzy import process

_name_mapped = {}

def match_name(name, list):
    if name in list:
        return name, 100
    if name in _name_mapped.keys():
        return _name_mapped[name]
    orig_name = name
    matched = None
    ratio = 80

    for tmp in list:
        orig = tmp
        if tmp in custom_names.keys():
            tmp = custom_names[tmp]
        tmp_ratio = fuzz.ratio(tmp, name)
        if name.startswith(tmp):
            matched = orig
        if tmp_ratio >= ratio:
            ratio = tmp_ratio
            matched = orig

    if matched is not None and name.startswith(matched):
        ratio = 90

    partial_ratio = 0
    sort_ratio = 0

    if ratio < 90:
        name = name.replace("-", " ")
        for tmp in list:
            orig = tmp
            if tmp in custom_names.keys():
                tmp = custom_names[tmp]
            tmp = tmp.replace("-", " ")
            tmp_partial = fuzz.partial_ratio(tmp, name)
            tmp_sort = fuzz.token_sort_ratio(tmp, name)
            if tmp_partial > partial_ratio or tmp_sort > sort_ratio:
                matched = orig
                partial_ratio = tmp_partial
                sort_ratio = tmp_sort
                ratio = tmp_sort
    _name_mapped[orig_name] = (matched, ratio)
    return matched, ratio

class CsvEntry(object):

    def __init__(self, columns, row):
        self.columns = columns
        self.rows = []
        self.rows.append(row)
    def append(self, row):
        self.rows.append(row)

class CsvFile(object):

    def __init__(self, file_name, inv_index=None):
        self.map = {}
        self.inv_map = {}
        self.name = file_name

        self.columns = []

        file = open("./caches/csv/"+file_name+".csv", 'r', encoding="utf-8")
        n = 0
        for line in file:
            vals = line.replace('\n','').split(',')
            # First line in the csv is the header!
            if n == 0:
                self.columns = vals

                if inv_index is None:
                    i = 0
                    for val in vals:
                        if val == "name":
                            inv_index = i
                            break
                        if val == "identifier":
                            inv_index = i
                            break
                        i = i + 1
                n = n + 1
                continue

            if len(line) == 0:
                continue
            n = n + 1
            key = vals[0]

            if len(vals) is not len(self.columns):
                continue

            entry = None
            if self.map.get(key, None) is not None:
                entry = self.map[key]
                entry.append(vals)
            else:
                entry = CsvEntry(self.columns, vals)
                self.map[key] = entry
            if inv_index is not None:
                self.inv_map[vals[inv_index]] = key
        file.close()

class CsvDatabase(object):

    def __init__(self, csv_files):
        self.files = {}
        self.names_map = {}
        self.columns_map = {}
        for name in csv_files:
            csv = CsvFile(name)
            self.files[name] = csv

            for col in csv.columns:
                if not col in self.columns_map.keys():
                    self.columns_map[col] = []
                self.columns_map[col].append(csv)


    def get_file(self, file):
        return self.files.get(file, None)

    def set_name_map(self, namemap):
        self.names_map = namemap

    def from_map(self, name):
        pokemon = self.get_file("pokemon")
        if name in pokemon.inv_map:
            return pokemon.inv_map[name]
        name = self.names_map.get(name, name)
        if name in pokemon.inv_map:
            name = pokemon.inv_map[name]
        return name

    def get_entry(self, name, expected_file, use_names_map=False):
        
        _id = name
        if use_names_map:
            _id = self.from_map(name)

        data = self.get_file(expected_file)

        hits = {}
        try:
            var = []
            rows = data.map[_id].rows
            for i in range(len(rows)):
                var.append(rows[i])
            hits[data.name] = var
        except:
            pass
        return hits

    def get_info(self, name, value, expected_file=None, use_names_map=False):

        _id = name
        if use_names_map:
            _id = self.from_map(name)

        matches = self.columns_map.get(value, None)
        if matches is None:
            return None

        hits = {}
        for match in matches:
            if expected_file is not None and match.name != expected_file:
                continue
            try:
                index = match.columns.index(value)
                var = []
                rows = match.map[_id].rows
                for i in range(len(rows)):
                    var.append(rows[i][index])
                hits[match.name] = var
            except:
                pass
        return hits