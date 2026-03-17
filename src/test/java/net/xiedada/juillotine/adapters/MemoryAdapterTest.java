package net.xiedada.juillotine.adapters;

public class MemoryAdapterTest extends StorageAdapterTest {
    @Override
    protected Adapter createAdapter() {
        return new MemoryAdapter(null); // Not needed...
    }
}