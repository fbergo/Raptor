package testcases;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class TestRegex {

	@Test
	public void testRegEx1() throws Exception {
		Pattern pattern = Pattern.compile("^Average ping time for.*\\sis\\s(\\d+)ms\\.");
		Matcher matcher = pattern.matcher("Average ping time for cday is 247ms.");
		System.err.println(matcher.matches() + " " + matcher.groupCount() + " " + matcher.group(1));
	}
}
