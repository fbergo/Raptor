package raptor.connector.ics.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.ics.IcsUtils;
import raptor.util.RaptorLogger;

public class PingEventParser extends ChatEventParser {
	private static final RaptorLogger LOG = RaptorLogger.getLog(ChatEventParser.class);
	public static final Pattern TIMESTAMP_2_PING_REGEX = Pattern
			.compile("^Average ping time for (\\D+) is (\\d+)ms\\.");
	public static final String IDENTIFIER = "Average ping time for ";

	public PingEventParser() {
		super();
	}

	@Override
	public ChatEvent parse(String text) {
		ChatEvent result = null;

		if (text.length() < 600 && text.indexOf(IDENTIFIER) != -1) {
			Matcher matcher = TIMESTAMP_2_PING_REGEX.matcher(text);
			if (matcher.matches() && matcher.groupCount() == 2) {
				result = new ChatEvent(IcsUtils.stripTitles(matcher.group(1)).trim(), ChatType.PING_RESPONSE,
						text.trim());
				result.setPingTime(Integer.parseInt(matcher.group(2)));

				if (LOG.isDebugEnabled())
					LOG.debug("Parsed PingEvent " + result.getSource() + " " + result.getPingTime() + " "
							+ result.getMessage());
			}
		}

		return result;
	}
}
