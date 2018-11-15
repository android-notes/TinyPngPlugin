package com.wanjian.plugin.utils

import java.security.MessageDigest

class MD5 {
    static String get(File file) {
        MessageDigest digest = MessageDigest.getInstance("MD5")
        file.withInputStream() { is ->
            int read
            byte[] buffer = new byte[8192]
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] md5sum = digest.digest()
        BigInteger bigInt = new BigInteger(1, md5sum)
        return bigInt.toString(16).padLeft(32, '0')
    }
}
