from datetime import datetime
import csv

class Logger:
    def __init__(self, logfile):
        self._logfile = logfile


    def writeLog(self, request, query):
        row = []
        with open(self._logfile, 'a') as file:
            writer = csv.writer(file)

            name = request.json["name"]
            mac_address = request.json["mac_address"]
            time = datetime.now().strftime("%Y-%m-%d %H:%M")
            row = [name, mac_address, query, time]

            print("[INFO]: ")
            print(row)
            writer.writerow(row)
        
        return [row]


    def readLog(self):
        log = []
        with open(self._logfile, 'r') as file:
            reader = csv.reader(file)
            for line in reader:
                log.append(line)
        
        return log
