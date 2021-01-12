import os
import sys

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

        columns = []

        file = open("./caches/csv/"+file_name+".csv", 'r', encoding="utf-8")
        n = 0
        for line in file:
            vals = line.replace('\n','').split(',')
            # First line in the csv is the header!
            if n == 0:
                columns = vals

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

            if len(vals) is not len(columns):
                continue

            entry = None
            if self.map.get(key, None) is not None:
                entry = self.map[key]
                entry.append(vals)
            else:
                entry = CsvEntry(columns, vals)
                self.map[key] = entry
            if inv_index is not None:
                self.inv_map[vals[inv_index]] = key
