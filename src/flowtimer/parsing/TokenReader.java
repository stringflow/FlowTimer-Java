package flowtimer.parsing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

public class TokenReader implements AutoCloseable {
	
	private Reader reader;
	private boolean isBackedUp;
	private int backedUpChar;
	private int lineNumber;
	private int charBuffer;
	private int previous;
	
	public TokenReader(Reader reader) {
		this.reader = new BufferedReader(reader);
		this.isBackedUp = false;
		this.backedUpChar = -1;
		this.lineNumber = 0;
		this.charBuffer = -1;
		this.previous = -1;
	}
	
	@Override
	public void close() throws IOException {
		reader.close();
	}
	
	public String next() throws IOException {
		int next = skipWhite(getChar());
		if(next == -1) {
			return null;
		} else if(isAlpha(next)) {
			return getName(next);
		} else if(isNumber(next)) {
			return getNumber(next);
		} else if(isQuote(next, previous)) {
			return getString(next);
		} else {
			return getSymbol(next);
		}
	}
	
	public void parseAssert(boolean condition, String message) throws ParseException {
		if(!condition) {
			throw new ParseException(message + " (line " + lineNumber + ")", lineNumber);
		}
	}
	
	private String getSymbol(int next) {
		return ((char)next) + "";
	}
	
	private String getNumber(int next) throws IOException {
		StringBuilder str = new StringBuilder();
		boolean hasDecimalPlace = false;
		
		while(isDigit(next) || (!hasDecimalPlace && next == '.')) {
			str.append((char)next);
			if(next == '.') {
				hasDecimalPlace = true;
			}
			next = getChar();
		}
		next = readExponent(next, str);
		ungetChar(next);
		return str.toString();
	}
	
	private int readExponent(int next, StringBuilder str) throws IOException {
		if(!isExponent(next)) {
			return next;
		}
		
		str.append((char)next);
		next = getChar();
		
		if(isNumber(next)) {
			str.append((char)next);
			next = getChar();
		}
		
		while(isDigit(next = getChar())) {
			str.append((char)next);
		}
		
		return next;
	}
	
	private String getName(int next) throws IOException {
		StringBuilder str = new StringBuilder();
		while(isAlNum(next)) {
			str.append((char)next);
			next = getChar();
		}
		ungetChar(next);
		return str.toString();
	}
	
	private String getString(int next) throws IOException {
		StringBuilder str = new StringBuilder();
		str.append((char)next);
		while(!isQuote((next = getChar()), previous)) {
			str.append((char)next);
		}
		str.append((char)next);
		return str.toString();
	}
	
	private void ungetChar(int next) {
		assert isBackedUp == false;
		isBackedUp = true;
		backedUpChar = next;
	}
	
	private int getChar() throws IOException {
		if(isBackedUp) {
			isBackedUp = false;
			return backedUpChar;
		}
		previous = charBuffer;
		int result = charBuffer = reader.read();
		if(isNewLine(result)) {
			lineNumber++;
		}
		return result;
	}
	
	private int skipWhite(int c) throws IOException {
		while(isWhitespace(c)) {
			c = getChar();
		}
		return c;
	}
	
	public static boolean isExponent(int c) {
		return c == 'e' || c == 'E';
	}
	
	public static boolean isNewLine(int c) {
		return c == '\n';
	}
	
	public static boolean isWhitespace(int c) {
		return isNewLine(c) || c == '\r' || c == ' ' || c == '\t';
	}
	
	public static boolean isNumber(int c) {
		return isDigit(c) || c == '-' || c == '+' || c == '.';
	}
	
	public static boolean isDigit(int c) {
		return c >= '0' && c <= '9';
	}
	
	public static boolean isAlNum(int c) {
		return isAlpha(c) || isDigit(c);
	}
	
	public static boolean isQuote(int c, int previous) {
		if(previous == '\\') {
			return false;
		} else {
			return c == '\"';
		}
	}
	
	public static boolean isAlpha(int c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	}
}