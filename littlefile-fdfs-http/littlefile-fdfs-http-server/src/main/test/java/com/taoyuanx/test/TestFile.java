package com.taoyuanx.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

public class TestFile {
	public static void main(String[] args) throws IOException {
		byte[] readFileToByteArray = FileUtils.readFileToByteArray(new File("E://test/big.pdf"));
		FileUtils.writeStringToFile(new File("e://test/big.txt"), Base64.encodeBase64String(readFileToByteArray));
	}
}
