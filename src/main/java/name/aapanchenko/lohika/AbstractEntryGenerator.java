package name.aapanchenko.lohika;

import java.io.File;
import java.util.Collections;
import java.util.List;

public abstract class AbstractEntryGenerator<T extends Entry> implements EntryGenerator<T> {

	@Override
	public final List<T> parse(File file) {
		try {
			return _parse(file);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}
	
	abstract protected List<T> _parse(File file);

}
