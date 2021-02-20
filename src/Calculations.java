import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class Calculations {

	/*
	 * Empty constructor
	 */
	public Calculations() {
	}

	public BufferedImage convertToBufferedImage(File image) {
		BufferedImage newimage = null;
		try {
			newimage = ImageIO.read(image);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Image could not be read!", "Error", JOptionPane.ERROR_MESSAGE);

		}
		return newimage;
	}

	public BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	public byte[] bufferedToBytes(BufferedImage image) {

		WritableRaster raster = image.getRaster();
		DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
		return buffer.getData();
	}

	/*
	 * @param image is image to encode message in
	 * 
	 * @param message is secret message from user to be placed into image Returns
	 * dialog saying whether message was successfully incorporated or not.
	 */
	public BufferedImage encodeMessage(String message, BufferedImage image) {
		// convert user's message to bytes

		byte[] msg = message.getBytes();
		// convert user's selected image to bytes
		byte[] imageinbytes = bufferedToBytes(image);
		byte[] msglength = byteToBits(msg.length);

		try {
			encode_text(imageinbytes, msglength, 0); // 0 first positiong
			encode_text(imageinbytes, msg, 32); // 4 bytes of space for length: 4bytes*8bit = 32 bits
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Target File cannot hold message!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		return image;
	}

	public byte[] byteToBits(int i) {
		byte alpha = (byte) ((i & 0xFF000000) >>> 24); // shift for sign bit
		byte red = (byte) ((i & 0xFF0000) >> 16); // for red
		byte green = (byte) ((i & 0xFF00) >> 8); // for green
		byte blue = (byte) ((i & 0xFF)); // for blue
		return (new byte[] { alpha, red, green, blue });
	}

	private byte[] encode_text(byte[] image, byte[] addition, int offset) {
		// check that the data + offset will fit in the image
		if (addition.length + offset > image.length) {
			throw new IllegalArgumentException("File not long enough!");
		}
		// loop through each addition byte
		for (int i = 0; i < addition.length; ++i) {
			// loop through the 8 bits of each byte
			int add = addition[i];
			for (int bit = 7; bit >= 0; --bit, ++offset) // ensure the new offset value carries on through both loops
			{
				// assign an integer to b, shifted by bit spaces AND 1
				// a single bit of the current byte
				int b = (add >>> bit) & 1;
				// assign the bit by taking: [(previous byte value) AND 0xfe] OR bit to add
				// changes the last bit of the byte in the image to be the bit of addition
				image[offset] = (byte) ((image[offset] & 0xFE) | b);
			}
		}
		return image;
	}

	/*
	 * @param image is image that needs to be decoded Returns panel with secret
	 * message or dialog saying that it was unsuccessful.
	 */
	public String decodeMessage(File image) {
		byte[] decode = null;
		try {
			// user space is necessary for decrypting
			BufferedImage img = convertToBufferedImage(image);
			BufferedImage newimg = deepCopy(img);
			decode = decode_text(bufferedToBytes(newimg));
			return (new String(decode));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "There is no hidden message in this image!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return "";
		}
	}

	private byte[] decode_text(byte[] image) {
		int length = 0;
		int offset = 32;
		// loop through 32 bytes of data to determine text length
		for (int i = 0; i < 32; ++i) // i=24 will also work, as only the 4th byte contains real data
		{
			length = (length << 1) | (image[i] & 1);
		}

		byte[] result = new byte[length];

		// loop through each byte of text
		for (int b = 0; b < result.length; ++b) {
			// loop through each bit within a byte of text
			for (int i = 0; i < 8; ++i, ++offset) {
				// assign bit: [(new byte value) << 1] OR [(text byte) AND 1]
				result[b] = (byte) ((result[b] << 1) | (image[offset] & 1));
			}
		}
		return result;
	}

}
