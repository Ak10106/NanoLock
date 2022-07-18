#!/bin/python3

import RPi.GPIO as GPIO
import time


class Servo:
    def __init__(self):
        self._SERVO_GPIO = 18     # GPIO 12, 13, 18, 19 are hardware PWM
        self._SERVO_PWM = 50      # 50 Hz (20 ms)

        # Duty cycle: 1 - 2 ms (5.0 - 10.0 %)
        # -90 deg (1 ms, 5 %) -- 0 deg (1.5 ms, 7.5 %) -- 90 deg (2 ms, 10 %)
        self._SERVO_LOCK = 12.5
        self._SERVO_UNLOCK = 2.0
        self._SERVO_DEFAULT = 7.5

        self._locked = True
        self._wait_time = 0.5

        GPIO.setmode(GPIO.BCM)
        GPIO.setup(self._SERVO_GPIO, GPIO.OUT)


    def control(self, duty):
        servo = GPIO.PWM(self._SERVO_GPIO, self._SERVO_PWM)
        servo.start(0.0)
        servo.ChangeDutyCycle(duty)
        time.sleep(self._wait_time)
        servo.stop()


    def unlock(self):
        self.control(self._SERVO_UNLOCK)
        self.control(self._SERVO_DEFAULT)
        self._locked = False


    def lock(self):
        self.control(self._SERVO_LOCK)
        self.control(self._SERVO_DEFAULT)
        self._locked = True


    def getState(self):
        return self._locked


    def shutdown(self):
        GPIO.cleanup()


if __name__ == "__main__":
    # Debug
    servo = Servo()
    
    try:
        while True:
            dir = input("u or l\n")
            if dir == "l":
                servo.lock()
            else:
                servo.unlock()

    except KeyboardInterrupt:
        servo.shutdown()