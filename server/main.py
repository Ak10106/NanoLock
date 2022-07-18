from flask import Flask, jsonify, abort, make_response, request
import json
from servo import Servo
from whitelist import Whitelist
from logger import Logger


app = Flask(__name__)


@app.route("/connect", methods=["POST"])
def connect():
    # content_type = request.headers.get('Content-Type')
    accepted = check(request)
    # log = logger.readLog()
    # print("[INFO]: History: ")
    # print(log)
    return makeResponse(accepted)


@app.route("/unlock", methods=["POST"])
def unlock():
    accepted = check(request)
    if accepted:
        servo.unlock()
        log = logger.writeLog(request, "Unlock")
    else:
        log = logger.writeLog(request, "Failed")
        
    return makeResponse(accepted)


@app.route("/lock", methods=["POST"])
def lock():
    accepted = check(request)
    if accepted:
        servo.lock()
        log = logger.writeLog(request, "Lock")
    else:
        log = logger.writeLog(request, "Failed")
        
    return makeResponse(accepted)


@app.route("/log", methods=["POST"])
def log():
    accepted = check(request)
    log = []
    if (accepted):
        log = logger.readLog()

    return makeResponse(accepted, log)


def makeResponse(result, log=None):
    res = {
        "result" : result,
        "data" : {
            "status" : servo.getState(),
            "log" : log
        }
    }
    return make_response(jsonify(res))


def check(request):
    # content_type = request.headers.get('Content-Type')
    name = request.json["name"]
    mac_address = request.json["mac_address"]
    result = whitelist.contains(mac_address)
    if result:
        print("[INFO]: Accepted " + name + " (" + mac_address + ")")
    else:
        print("[INFO]: Rejected " + name + " (" + mac_address + ")")

    return result


if __name__ == "__main__":
    servo = Servo()
    whitelist = Whitelist("whitelist.json")
    logger = Logger("log.csv")

    try:
        app.run(debug=False, host="0.0.0.0", port=5000)

    except KeyboardInterrupt:
        servo.shutdown()
