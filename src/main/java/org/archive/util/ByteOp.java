package org.archive.util;

import java.io.DataInput;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ByteOp {
	private static int MAX_READ_SIZE = 128 * 1024;

	public static byte[] copy(byte[] src) {
		return copy(src,0,src.length);
	}

	public static byte[] copy(byte[] src, int offset, int length) {
		byte[] copy = new byte[length];
		System.arraycopy(src, offset, copy, 0, length);
		return copy;
	}

	public static boolean cmp(byte[] input, byte[] want) {
		if (input.length != want.length) {
			return false;
		}
		for (int i = 0; i < input.length; i++) {
			if (input[i] != want[i]) {
				return false;
			}
		}
		return true;
	}

	public static boolean cmp(byte[] b1, int src1, byte[] b2, int src2, int n) {
		if(b1.length < src1 + n) {
			throw new IndexOutOfBoundsException();
		}
		if(b2.length < src2 + n) {
			throw new IndexOutOfBoundsException();
		}
		for(int i = 0; i < n; i++) {
			if(b1[src1 + i] != b2[src2 + i]) {
				return false;
			}
		}
		return true;
	}

	public static byte[] append(byte a[], byte b[]) {
		byte n[] = new byte[a.length + b.length];
		System.arraycopy(a, 0, n, 0, a.length);
		System.arraycopy(b, 0, n, a.length,b.length);
		return n;
	}

	public static int bytesToShort(int b1, int b2) {
		return ((b2 << 8) & 0x00ff00) | (b1 & 0xff);
	}
	public static int bytesToShort(byte[] bytes, int offset) {
		if(bytes.length - offset < 2) {
			throw new IndexOutOfBoundsException();
		}
		return bytesToShort(bytes[offset],bytes[offset+1]);
	}
	public static int bytesToShort(byte[] bytes) {
		return bytesToShort(bytes,0);
	}

	public static int readShort(DataInput in) throws IOException {
		return bytesToShort(in.readByte(), in.readByte());
	}

	public static int readShort(InputStream is) throws IOException {
		int b1 = is.read();
		if(b1 == -1) {
			throw new EOFException("No bytes expected short(2)");
		}
		int b2 = is.read();
		if(b2 == -1) {
			throw new EOFException("One byte expected short(2)");
		}
        return bytesToShort(b1,b2);
	}


	public static void writeShort(OutputStream os, int shortVal) throws IOException {
		os.write((byte) (shortVal & 0xff));
		os.write((byte) (((shortVal & 0x00ff00) >> 8) & 0xff));
	}

	public static void writeShort(byte buf[], int offset, int shortVal) {
		if(buf.length - offset < 2) {
			throw new IndexOutOfBoundsException();
		}
		buf[offset + 0] = (byte) (shortVal & 0xff);
		buf[offset + 1] = (byte) (((shortVal & 0x00ff00) >> 8) & 0xff);
	}
	public static long bytesToInt(int b1, int b2, int b3, int b4) {
		long res = (b1 & 0xff) | ((b2 << 8) & 0x00ff00) | 
			((b3 << 16) & 0x00ff0000) | ((b4 << 24) & 0xff000000);
		return res;
	}
	public static long bytesToInt(byte[] bytes, int offset) {
		if(bytes.length - offset < 4) {
			throw new IndexOutOfBoundsException();
		}
		return bytesToInt(
				bytes[offset],
				bytes[offset+1],
				bytes[offset+2],
				bytes[offset+3]);
	}
	public static long bytesToInt(byte[] bytes) {
		return bytesToInt(bytes,0);
	}
	public static long readInt(DataInput in) throws IOException {
		return 
			bytesToInt(in.readByte(),in.readByte(),in.readByte(),in.readByte());
	}
	public static long readInt(InputStream is) throws IOException {
		
		int b1 = is.read();
		if(b1 == -1) {
			throw new EOFException("No bytes for Int(4)");
		}
		int b2 = is.read();
		if(b2 == -1) {
			throw new EOFException("Only 1 bytes for Int(4)");
		}
		int b3 = is.read();
		if(b3 == -1) {
			throw new EOFException("Only 2 bytes for Int(4)");
		}
		int b4 = is.read();
		if(b4 == -1) {
			throw new EOFException("Only 3 bytes for Int(4)");
		}
		return bytesToInt(b1, b2, b3, b4);
	}

	public static void writeInt(OutputStream os, long intVal) throws IOException {
		os.write((byte) (intVal & 0xff));
		os.write((byte) (((intVal & 0x00ff00) >> 8) & 0xff));
		os.write((byte) (((intVal & 0x00ff0000) >> 16) & 0xff));
		os.write((byte) (((intVal & 0x00ff000000) >> 24) & 0xff));
	}

	public static void writeInt(byte buf[], int offset, long intVal) {
		if(buf.length - offset < 4) {
			throw new IndexOutOfBoundsException();
		}
		buf[offset + 0] = (byte) (intVal & 0xff);
		buf[offset + 1] = (byte) (((intVal & 0x00ff00) >> 8) & 0xff);
		buf[offset + 2] = (byte) (((intVal & 0x00ff0000) >> 16) & 0xff);
		buf[offset + 3] = (byte) (((intVal & 0x00ff000000) >> 24) & 0xff);
	}

	public static byte[] readNBytes(InputStream is, int n)
	throws IOException {
		byte[] b = new byte[n];
		int left = n;
		while(left > 0) {
			int amt = is.read(b,n-left,left);
			if(amt == -1) {
				throw new EOFException("Short read");
			}
			left -= amt;
		}
		return b;
	}

	/**
	 * Read, buffer, and return bytes from is until a null byte is encountered 
	 * @param is InputStream to read from
	 * @return array of bytes read, INCLUDING TRAILING NULL
	 * @throws IOException if the underlying stream throws on, OR if the default
	 * maximum buffer size is reached before a null byte is found
	 * @throws EOFException if EOF is encountered before a null byte
	 */
	public static byte[] readToNull(InputStream is) throws IOException {
		return readToNull(is,MAX_READ_SIZE);
	}

	/**
	 * Read, buffer, and return bytes from is until a null byte is encountered 
	 * @param is InputStream to read from
	 * @param maxSize maximum number of bytes to search for the null
	 * @return array of bytes read, INCLUDING TRAILING NULL
	 * @throws IOException if the underlying stream throws on, OR if the 
	 * specified maximum buffer size is reached before a null byte is found
	 * @throws EOFException if EOF is encountered before a null byte
	 */
	public static byte[] readToNull(InputStream is, int maxSize)
		throws IOException {

		byte[] bytes = new byte[maxSize];
		int i = 0;
		while(i < maxSize) {
			int b = is.read();
			if(b == -1) {
				throw new EOFException("NO NULL");
			}
			bytes[i] = (byte) (b & 0xff);
			i++;
			if(b == 0) {
				return copy(bytes,0,i);
			}
		}
		// BUGBUG: This isn't the right exception!!!
		// TODO: just skip any more bytes until NULL or EOF
		//       throw an EOF if we find it... produce warning, too
		throw new IOException("Buffer too small");
	}	

	public static int discardToNull(InputStream is)
		throws IOException {

		int i = 0;
		while(true) {
			int b = is.read();
			if(b == -1) {
				throw new EOFException("No NULL before EOF");
			}
			i++;
			if(b == 0) {
				return i;
			}
		}
	}	
	public static String drawHex(byte[] b) {
		return drawHex(b,b.length);
	}
	public static String drawHex(byte[] b, int bytesPerRow) {
		return drawHex(b,0,b.length,bytesPerRow);
	}
	public static String drawHex(byte[] b, int offset, int length, int bytesPerRow) {
		int rows = (int) Math.ceil(length / bytesPerRow);
		if(rows == 0) {
			rows = 1;
		}
		int bytesToOutput = length;
		int position = 0;
		// 2 chars per byte, plus 1 space, plus 1 newline per row:
		StringBuilder sb = new StringBuilder((length * 3) + rows);
		for(int row = 0; row < rows; row++) {
			int bytesThisRow = Math.min(bytesToOutput, bytesPerRow);
			for(int col = 0; col < bytesThisRow; col++) {
				String hex = Integer.toHexString(b[position++] & 0xff);
				if(hex.length() == 2) {
					sb.append(hex);
				} else {
					sb.append("0").append(hex);
				}
//				sb.append(" ");
			}
			bytesToOutput -= bytesThisRow;
		}
		return sb.toString();
	}

	public byte[] readFile(File f) throws FileNotFoundException, IOException {
		long lengthL = f.length();
		if(lengthL > Integer.MAX_VALUE) {
			throw new IOException("File too big to read into buffer..");
		}
		return readNBytes(new FileInputStream(f), (int) lengthL);
	}

}
