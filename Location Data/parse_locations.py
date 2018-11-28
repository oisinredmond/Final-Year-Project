# Oisin Redmond - C15492202 - DT228/4
# Final Year Project Prototype


#  This script is used to parse geographical data related to bathing locations in Ireland from
#  https://data.gov.ie/dataset/bathing-water-locations/resource/b8566896-3102-4261-add5-8a4fb0a9b535?inner_span=True.
#  The script reads in a geojson file and extracts location names and coordinates, formats them into a dictionary
#  object and exports it to a JSON file to be used in a database.
#

import json
import unicodedata

with open("./data/bathingLocations.geojson", "r") as read_file:
    data = json.load(read_file)

data_toWrite = {"locations": {}}

i = 0

for feature in data["features"]:
        if feature["properties"].get("WaterType") == "Coastal Waterbody":
            name = feature["properties"]["Name"]
            name = unicodedata.normalize('NFD', name).encode('ascii', 'ignore')
            name = name.lower()
            name = name.title()
            name = name.decode("utf-8")
            print(name)
            data_toWrite["locations"][i] = {"name": [name], "coordinates": feature["geometry"]["coordinates"], "weather": []}
            i += 1


print(data_toWrite)
with open("./data/locations2.json", "w") as fout:
    json.dump(data_toWrite, fout)
