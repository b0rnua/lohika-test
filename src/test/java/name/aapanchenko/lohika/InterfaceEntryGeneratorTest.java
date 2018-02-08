package name.aapanchenko.lohika;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class InterfaceEntryGeneratorTest {
	
	private List<InterfaceEntry> expecteds;
	private InterfaceEntry _1;
	private InterfaceEntry _2;
	private InterfaceEntry _3;
	
	@Before
	public void beforeEachTest() {
	    _1 = new InterfaceEntry();
		_1.setState(true);
		_1.setName("1/1.1");
		try {
			_1.setIpAddr(InetAddress.getByName("11.11.11.11"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		_1.setMacAddress("11:22:E9:E5:44:48");
		_1.setSpeed(102400);
		_1.setStartLineNumber(0);
		
		_2 = new InterfaceEntry();
		_2.setState(true);
		_2.setName("1/2");
		_2.setMacAddress("11:22:E9:E5:44:50");
		try {
			_2.setIpAddr(InetAddress.getByName("10.10.10.10"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		_2.setSpeed(10);
		_2.setStartLineNumber(0);
		
		_3 = new InterfaceEntry();
		_3.setState(false);
		_3.setName("1/3");
		_3.setMacAddress("11:22:E9:E5:44:52");
		_3.setIpAddr(null);
		_3.setSpeed(null);
		_3.setStartLineNumber(0);
		
		expecteds = Arrays.asList(_1, _2, _3);
	}
	
	
	@Test
	public void common() throws UnknownHostException {
		_1.setStartLineNumber(3);
		_2.setStartLineNumber(9);
		_3.setStartLineNumber(20);
		
		File file = new File(getClass().getClassLoader().getResource("sample_1").getFile());
		EntryGenerator<InterfaceEntry> entryGenerator = new InterfaceEntryGenerator();
		List<InterfaceEntry> actuals = entryGenerator.parse(file);		
		actuals.forEach(a -> Assert.assertTrue(expecteds.contains(a)));
	}
	
	@Test
	public void common_2() throws UnknownHostException {
		_1.setStartLineNumber(3);
		try {
			_2.setIpAddr(InetAddress.getByName("10.10.10.25"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		_2.setStartLineNumber(9);
		_3.setStartLineNumber(22);
		
		File file = new File(getClass().getClassLoader().getResource("sample_3").getFile());
		EntryGenerator<InterfaceEntry> entryGenerator = new InterfaceEntryGenerator();
		List<InterfaceEntry> actuals = entryGenerator.parse(file);		
		actuals.forEach(a -> Assert.assertTrue(expecteds.contains(a)));
	}
	
	@Test
	public void brokenFormatting() throws UnknownHostException {
		_1.setStartLineNumber(1);
		_2.setStartLineNumber(5);
		_3.setStartLineNumber(12);
		
		File file = new File(getClass().getClassLoader().getResource("sample_2").getFile());
		EntryGenerator<InterfaceEntry> entryGenerator = new InterfaceEntryGenerator();
		List<InterfaceEntry> actuals = entryGenerator.parse(file);		
		actuals.forEach(a -> Assert.assertTrue(expecteds.contains(a)));
	}
	
	@Test
	public void brokenFormat() throws UnknownHostException {
		_1.setStartLineNumber(3);
		_2.setIpAddr(null);
		_2.setMacAddress(null);
		_2.setState(false);
		_2.setSpeed(null);
		_2.setStartLineNumber(10);
		
		File file = new File(getClass().getClassLoader().getResource("sample_4").getFile());
		EntryGenerator<InterfaceEntry> entryGenerator = new InterfaceEntryGenerator();
		List<InterfaceEntry> actuals = entryGenerator.parse(file);	
		actuals.forEach(a -> Assert.assertTrue(expecteds.contains(a)));
		Assert.assertEquals(2, actuals.size());
	}

}
