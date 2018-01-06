package message;

import message.builder.IMessageBuilder;
import message.builder.JSONMessageBuilder;
import message.parser.IMessageParser;
import message.parser.JSONMessageParser;

public class Default {

	public static IMessageParser parser = new JSONMessageParser();
	public static IMessageBuilder builder = new JSONMessageBuilder();

}
