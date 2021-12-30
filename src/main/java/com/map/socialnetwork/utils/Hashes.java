package com.map.socialnetwork.utils;

public class Hashes {
    public static String MD5(String text) {
        return text;
//        String hash = text;
//        try {
//            MessageDigest md = MessageDigest.getInstance("MD5");
//            md.update(text.getBytes());
//            byte[] bytes = md.digest();
//
//            StringBuilder sb = new StringBuilder();
//            for (byte aByte : bytes) {
//                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
//            }
//
//            text = sb.toString();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            throw new RuntimeException();
//        }
//
//        return text;
    }
}
