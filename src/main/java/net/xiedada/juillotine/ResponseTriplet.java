package net.xiedada.juillotine;
import java.util.Map;

public record ResponseTriplet(int status, Map<String,String> headers, String body) {}
