package net.xiedada.juillotine.adapters;

import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.util.Properties;

public class BerkeleyDBAdapterTest extends StorageAdapterTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private BerkeleyDBAdapter adapter;

    @Override
    protected Adapter createAdapter() {
        Properties props = new Properties();
        props.setProperty("juillotine.BerkeleyDBAdapter.dbPath", tempFolder.getRoot().getAbsolutePath());
        adapter = new BerkeleyDBAdapter(props);
        return adapter;
    }

    @After
    public void tearDown() {
        if (adapter != null) {
            adapter.close();
        }
    }
}
