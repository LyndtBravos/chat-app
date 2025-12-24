package com.myprojects.chatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@SpringBootApplication
public class ChatAppApplication {

	public static void main(String[] args) throws NoSuchAlgorithmException {
		SecretKey key = KeyGenerator.getInstance("HmacSHA256").generateKey();
		String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
		System.out.println(base64Key);

		SpringApplication.run(ChatAppApplication.class, args);
	}
}