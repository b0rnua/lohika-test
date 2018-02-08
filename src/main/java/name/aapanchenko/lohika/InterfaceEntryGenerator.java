package name.aapanchenko.lohika;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InterfaceEntryGenerator extends AbstractEntryGenerator<InterfaceEntry> {

	private Pattern interestNode = Pattern.compile("\\binterface\\b\\s+(?<name>[\\d/\\.]+)");
	private ByteBuffer buffer = ByteBuffer.allocate(1024*10).order(ByteOrder.nativeOrder());
	Charset charset = Charset.forName("UTF-8");
	int stringNumber = 1;
	int openedNodes = 0;
	
	@Override
	public List<InterfaceEntry> _parse(File file) {
		try (FileInputStream fis = new FileInputStream(file)) {	
			return InterfaceEntryObjectMapper.map(read(fis.getChannel()));			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}
	
	private List<Tuple2> read(ReadableByteChannel src) throws IOException {			
		List<Tuple2> result = new ArrayList<>();
		
		while (src.read(buffer) != -1) {
			buffer.flip();

			CharBuffer chars = charset.decode(buffer);
			
			while (chars.hasRemaining()) {
				Optional<Tuple2> o = readNode(chars);
				if (o.isPresent()) {
					result.add(o.get());
				}
			}
			
			buffer.clear();
		}
		
		return result;
	}
	
	private Optional<Tuple2> readNode(CharBuffer src) {
		CharBuffer dst = ByteBuffer.allocate(1024*10).asCharBuffer();
		readTitle(src, dst);
		int freezNumer = stringNumber;
		String name = parseName(dst, interestNode);
		if ("".equals(name)) {
			skipNode(src);
		} else {
			dst.clear();
			List<Tuple2> body = readBody(src, dst);
			body.add(Tuple2.couple("startLineNumber", freezNumer));
			return Optional.ofNullable(Tuple2.couple(name, body));
		}
		return Optional.empty();
	}
	
	private String parseName(CharBuffer src, Pattern regex) {
		Matcher m = regex.matcher(getString(src));
		return m.find() ? m.group("name") : "";
	}
	
	private String getString(CharBuffer src) {
		char[] arr = new char[src.flip().remaining()];
		src.get(arr);
		return new String(arr);
	}
	
	private void readTitle(CharBuffer src, CharBuffer dst) {
		while (src.hasRemaining()) {
			char c = getChar(src);
			if (!isNodeStart(c)) {
				dst.put(c);
				continue;
			}
			openedNodes++;
			return;
		}
	}
	
	private void skipNode(CharBuffer src) {
		while (src.hasRemaining()) {
			char c = getChar(src);
			if (isNodeStart(c)) {
				openedNodes++;
				continue;
			}
			if (isNodeEnd(c)) {
				openedNodes--;
				if (openedNodes == 0) {
					return;
				}
				continue;
			}
		}
	}
	
	private List<Tuple2> readBody(CharBuffer src,  CharBuffer dst) {	
		List<Tuple2> nodeBodies = new ArrayList<>();
		src.mark();
		int markString = stringNumber;
		while (src.hasRemaining()) {
			char c = getChar(src);

			if (isNodeStart(c)) {
				src.reset();
				stringNumber = markString;
				Optional<Tuple2> o = readNodeBody(src);
				if (o.isPresent()) {
					nodeBodies.add(o.get());
				}
				dst.clear();
				continue;
			}
			
			if (!isNodeEnd(c)) {
				dst.put(c);
				continue;
			}
			
			openedNodes--;
			
			nodeBodies.addAll(readSimpleBody(dst));
			return nodeBodies;
		}
		
		return nodeBodies;
	}
	
	private Optional<Tuple2> readNodeBody(CharBuffer src) {
		return readNode(src);
	}
	
	private boolean isNodeStart(char c) {
		if (c == '{' ) {
			return true;
		}
		return false;
	}
	
	private boolean isNodeEnd(char c) {
		if (c == '}' ) {
			return true;
		}
		return false;
	}
	
	private static class Tuple2 {
		public final String _1;
		public final Object _2;
		private Tuple2(String _1, Object _2) {
			this._1 = _1;
			this._2 = _2;
		}
		public static Tuple2 couple(String _1, Object _2) {
			return Objects.isNull(_1) || Objects.isNull(_2) ? null : new Tuple2(_1, _2);
		}
		@Override
		public String toString() {
			return "{" + _1 + "} : {" + _2 + "}";
		}
	}
	
	private List<Tuple2> readSimpleBody(CharBuffer src) {
		src.flip();
		char[] tmp = new char[128];
		int index = 0;
		List<String> list = new ArrayList<>();
		while (src.hasRemaining()) {
			char c = src.get();
			if (c != '\n') {
				tmp[index] = c;
				index++;
				continue;
			}
			list.add(new String(tmp, 0, index).trim());
			index = 0;
		}
		
		return list.stream()
				.map(this::getKeyValue)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}
	
	private Optional<Tuple2> getKeyValue(String element) {
		int lastWhiteSpace = element.trim().lastIndexOf(' ');
		
		if (lastWhiteSpace == -1) {
			return Optional.empty();
		}
		
		String possibleValue = element.substring(lastWhiteSpace+1, element.length()).toUpperCase();
		String possibleKey = element.substring(0, lastWhiteSpace);
		
		if ("KB".equals(possibleValue) || "MB".equals(possibleValue) || "GB".equals(possibleValue)) {
			int oneMoreWhiteSpace = possibleKey.lastIndexOf(' ');
			possibleValue = possibleKey.substring(oneMoreWhiteSpace+1, possibleKey.length()) + possibleValue;
			possibleKey = possibleKey.substring(0, oneMoreWhiteSpace);
		}
		
		return Optional.ofNullable(Tuple2.couple(possibleKey, possibleValue));
	}
	
	private char getChar(CharBuffer src) {
		char c = src.get();
		if (c == '\n') {
			stringNumber++;
		}
		return c;
	}
	
	private static class InterfaceEntryObjectMapper {
		
		private static class InterfaceEntryWithValidation extends InterfaceEntry {
			private static Pattern ipAddrPattern = Pattern.compile(
					"(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
			
			public void setState(String state) {
				if ("UP".equalsIgnoreCase(state)) {
					super.setState(true);
					return;
				}
				super.setState(false);
			}
			
			public void setIpAddr(String ipAddr) {
				Matcher m = ipAddrPattern.matcher(ipAddr);
				if (m.find()) {
					try {
						super.setIpAddr(InetAddress.getByName(m.group(0)));
					} catch (UnknownHostException e) {
						super.setIpAddr(null);
						e.printStackTrace();
					}
					return;
				}
				super.setIpAddr(null);
			}
			
			public void setSpeed(String speed) {
				String possibleValue = speed.substring(0, speed.length()-2);
				
				for (int i = 0; i < possibleValue.length(); i++) {
		            if (Character.isDigit(possibleValue.charAt(i)) == false) {
		            	super.setSpeed(-1);
		            	return;
		            }
		        }
				Integer value = Integer.parseInt(possibleValue);
				String unit = speed.substring(speed.length()-2, speed.length());
				if ("KB".equals(unit)) {
					super.setSpeed(value);
					return;
				}
				if ("MB".equals(unit)) {
					super.setSpeed(value*1024);
					return;
				}
				if ("GB".equals(unit)) {
					super.setSpeed(value*1024*1024);
					return;
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		public static List<InterfaceEntry> map(List<Tuple2> nodes) {
			return nodes.stream().map(tuple2 -> {
				InterfaceEntryWithValidation entry = new InterfaceEntryWithValidation();
				entry.setName(tuple2._1);
				new ArrayList<Tuple2>(List.class.cast(tuple2._2))
					.forEach(t -> {
						String key = t._1;
						Object value = t._2;
						if (key.contains("state")) {
							entry.setState(String.valueOf(value));
							return;
						}
						if (key.contains("mac")) {
							entry.setMacAddress(String.valueOf(value));
							return;
						}
						if (key.contains("ip") || key.contains("address")) {
							entry.setIpAddr(String.valueOf(value));
							return;
						}
						if (key.contains("speed")) {
							entry.setSpeed(String.valueOf(value));
							return;
						}
						if (key.contains("startLineNumber")) {
							entry.setStartLineNumber(Integer.class.cast(value));
							return;
						}
					});
				return entry;
			}).collect(Collectors.toList());
		}
	}
}
