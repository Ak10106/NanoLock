import json


class Whitelist:
    def __init__(self, filename):
        self._filename = filename 
        self._list = []
        self.readList()


    def readList(self):
        with open(self._filename, 'r') as file:
            data = json.load(file)
            self._list = data["mac_addresses"]
            print("Whitelist: ")
            print(self._list)


    def contains(self, mac_address):
        return (mac_address.upper() in self._list) \
            or (mac_address.lower() in self._list)


if __name__ == "__main__":
    wl = Whitelist("whitelist.json")
    print(wl.contains("d4:c9:4b:72:d1:60"))