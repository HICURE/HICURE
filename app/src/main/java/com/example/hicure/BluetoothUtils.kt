/*
package com.example.hicure

class BluetoothUtils {
    companion object {
        fun findBLECharacteristics(gatt: BluetoothGatt): List<BluetoothGattCharacteristic> {
            val matchingCharacteristics: MutableList<BluetoothGattCharacteristic> = ArrayList()
            val serviceList = gatt.services
            val service = findGattService(serviceList) ?: return matchingCharacteristics
            val characteristicList = service.characteristics
            for (characteristic in characteristicList) {
                if (isMatchingCharacteristic(characteristic)) {
                    matchingCharacteristics.add(characteristic)
                }
            }
            return matchingCharacteristics
        }

        fun findCommandCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
            return findCharacteristic(gatt, Constants.CHARACTERISTIC_COMMAND_STRING)
        }

        fun findResponseCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
            return findCharacteristic(gatt, Constants.CHARACTERISTIC_RESPONSE_STRING)
        }

        private fun findCharacteristic(gatt: BluetoothGatt, uuidString: String): BluetoothGattCharacteristic? {
            val serviceList = gatt.services
            val service = findGattService(serviceList) ?: return null
            val characteristicList = service.characteristics
            for (characteristic in characteristicList) {
                if (matchCharacteristic(characteristic, uuidString)) {
                    return characteristic
                }
            }
            return null
        }

        private fun matchCharacteristic(characteristic: BluetoothGattCharacteristic?, uuidString: String): Boolean {
            if (characteristic == null) {
                return false
            }
            val uuid: UUID = characteristic.uuid
            return matchUUIDs(uuid.toString(), uuidString)
        }

        private fun findGattService(serviceList: List<BluetoothGattService>): BluetoothGattService? {
            for (service in serviceList) {
                val serviceUuidString = service.uuid.toString()
                if (matchServiceUUIDString(serviceUuidString)) {
                    return service
                }
            }
            return null
        }

        private fun matchServiceUUIDString(serviceUuidString: String): Boolean {
            return matchUUIDs(serviceUuidString, Constants.SERVICE_STRING)
        }

        private fun isMatchingCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
            if (characteristic == null) {
                return false
            }
            val uuid: UUID = characteristic.uuid
            return matchCharacteristicUUID(uuid.toString())
        }

        private fun matchCharacteristicUUID(characteristicUuidString: String): Boolean {
            return matchUUIDs(characteristicUuidString, Constants.CHARACTERISTIC_COMMAND_STRING, Constants.CHARACTERISTIC_RESPONSE_STRING)
        }

        private fun matchUUIDs(uuidString: String, vararg matches: String): Boolean {
            for (match in matches) {
                if (uuidString.equals(match, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }
    }

}
*/
