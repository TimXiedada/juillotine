package net.xiedada.juillotine.adapters;

import  net.xiedada.juillotine.Service;

public interface IAdapter {
    // Create a shortcode
    public String add(String URL, String shortcode, Service.Options options);

    // Standard query and reverse query
    public String find(String shortcode);
    public String codeFor(String URL);

    // Remove an entry
    public void clear(String shortcode);
    public void clearCode(String URL);
}
