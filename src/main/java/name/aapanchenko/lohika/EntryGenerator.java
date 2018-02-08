package name.aapanchenko.lohika;

import java.io.File;
import java.util.List;

public interface EntryGenerator<T extends Entry> {
	
	List<T> parse(File file);
	
}
