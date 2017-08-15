package com.ffdc.utility;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ffdc.controller.CampaignController;

/**
 * A utility class that encrypts or decrypts a file.
 * 
 * @author www.codejava.net
 *
 */
public class AESEncryptDecrypt implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5612488821601430281L;
	private static final Log log = LogFactory.getLog(CampaignController.class);

	private static final String TRANSFORMATION = "AES";
	private static final String SaltHex = "6E13E6B0D93EDE6882EA27610AF12C07";
	private static Key seckey = null;

	public static String encrypt(String clearText) {
		if (seckey == null) {
			// SecureRandom random = new SecureRandom();
			byte[] salt = hexStringToByteArray(SaltHex);
			// random.nextBytes(salt);
			seckey = new SecretKeySpec(salt, "AES");

		}
		return encryptDecrypt(Cipher.ENCRYPT_MODE, clearText);

	}

	public static String decrypt(String opaqueText) {
		if (seckey == null) {
			byte[] salt = hexStringToByteArray(SaltHex);
			// random.nextBytes(salt);
			seckey = new SecretKeySpec(salt, "AES");
		}

		return encryptDecrypt(Cipher.DECRYPT_MODE, opaqueText);
	}

	private static String byteToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X", b));
		}

		return sb.toString();
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	private static String encryptDecrypt(int cipherMode, String payload) {
		try {
			log.debug("Starting encryptDecrypt payload = " + payload);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(cipherMode, seckey);

			byte[] inputBytes = null;

			if (cipherMode == Cipher.ENCRYPT_MODE)
				inputBytes = payload.getBytes();
			else

				inputBytes = hexStringToByteArray(payload);

			byte[] outputBytes = cipher.doFinal(inputBytes);
			log.debug("Successfull encryptDecrypt payload = " + payload);

			if (cipherMode == Cipher.ENCRYPT_MODE)
				return byteToHex(outputBytes);
			else
				return new String(outputBytes);
			// Base64.getEncoder().encodeToString(outputBytes);

		} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException
				| IllegalBlockSizeException ex) {
			if (cipherMode == Cipher.DECRYPT_MODE)
				log.debug("Encript decrypt failedpayload = " + payload);
			else
				log.error("Encript decrypt failedpayload = " + payload);
			return null;
		}
	}

}