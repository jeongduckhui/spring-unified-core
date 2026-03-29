package com.example.demo.jasypt;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;

public class JasyptEncryptor {

    public static void main(String[] args) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("local_dev_key"); // keys.properties 값
        encryptor.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        encryptor.setIvGenerator(new RandomIvGenerator());
        encryptor.setKeyObtentionIterations(Integer.parseInt("1000"));
        encryptor.setSaltGenerator(new org.jasypt.salt.RandomSaltGenerator());

        String encrypted = encryptor.encrypt("uzlpwtemrdrrzryd");
        System.out.println("##############################333");
        System.out.println(encrypted);
        System.out.println("##############################333");
    }
}
