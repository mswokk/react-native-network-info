const RNNetworkInfo = require('react-native').NativeModules.RNNetworkInfo;

const NetworkInfo = {
    async getSSID() {
        return RNNetworkInfo.getSSID();
    },

    async getBSSID() {
        return RNNetworkInfo.getBSSID();
    },

    async getSignalLevel(totalLevel) {
        return RNNetworkInfo.getSSID(totalLevel);
    },

    async getIPAddress(ip) {
        return RNNetworkInfo.getIPAddress(ip);
    },
};

module.exports = NetworkInfo;
