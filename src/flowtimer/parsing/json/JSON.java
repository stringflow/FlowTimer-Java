package flowtimer.parsing.json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;

import flowtimer.ErrorHandler;
import flowtimer.parsing.TokenReader;

public class JSON {
	
	private JSONValue value;
	
	public JSON(File file) throws Exception {
		this(new FileReader(file));
	}
	
	public JSON(InputStream stream) throws Exception  {
		this(new InputStreamReader(stream));
	}
	
	public JSON(Reader reader) throws Exception  {
			read(reader);
	}
	
	public JSON(String json) throws Exception {
		read(new StringReader(json));
	}
	
	private void read(Reader reader) throws IOException, ParseException {
		TokenReader tokens;
		tokens = new TokenReader(reader);
		value = JSONValue.parse(tokens, tokens.next());
		String token;
		tokens.parseAssert((token = tokens.next()) == null, "Expected EOF; instead got " + token);
		tokens.close();
	}
	
	public JSON(JSONValue value) {
		this.value = value;
	}
	
	public void write(String fileName) {
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter(fileName));
			value.write(br);
			br.close();
		} catch(IOException e) {
			ErrorHandler.handleException(e, false);
		}
	}
	
	public String toString() {
		try {
			StringWriter wr = new StringWriter();
			value.write(wr);
			wr.close();
			return wr.getBuffer().toString();
		} catch(IOException e) {
			ErrorHandler.handleException(e, false);
		}
		return super.toString();
	}
	
	public JSONValue get() {
		return value;
	}
}
