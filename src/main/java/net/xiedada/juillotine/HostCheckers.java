package net.xiedada.juillotine;

import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// import java.util.regex.PatternSyntaxException;

public class HostCheckers {
    public static abstract class HostChecker
    {
        protected String error;
        public ResponseTriplet call(URL url){
            if (!valid(url)){
                return this.errorResponse();
            } else {
                return null;
            }
        }
        public abstract boolean valid(URL url);

        public ResponseTriplet errorResponse(){
            return new ResponseTriplet(
                    422,
                    null,
                    error
            );
        }

        public static HostChecker matching(String host){
            try {
                Pattern p = Pattern.compile(host);
                return new RegexHostChecker(p);
            } catch (IllegalArgumentException e) {
                // Do nothing.
            }
            try {
                return new WildcardHostChecker(host);
            } catch (IllegalArgumentException e) {
                return new StringHostChecker(host);
            }


        }
        public HostChecker matching(Pattern pattern){
            return new RegexHostChecker(pattern);
        }


    }

    public static class RegexHostChecker extends HostChecker{
        private final Pattern pattern;
        public RegexHostChecker(Pattern pattern) {
            this.pattern = pattern;
            error = "URL must match " + pattern.toString();
        }

        public RegexHostChecker(String host){
            // Reserved for future use
            this(Pattern.compile(host));
        }

        @Override
        public ResponseTriplet call(URL url){
            if (pattern.matcher(url.getHost()).matches()){
                return null;
            }
            else{
                return errorResponse();
            }
        }
        @Override
        public boolean valid(URL url){
            return pattern.matcher(url.getHost()).matches();
        }
    }

    public static class StringHostChecker extends HostChecker{
        private final String host;

        StringHostChecker(String host){
            error = "URL must be from " + host;
            this.host = host.toLowerCase();
        }

        @Override
        public boolean valid(URL url) {
            return Objects.equals(url.getHost(), this.host);
        }
    }

    public static class WildcardHostChecker extends RegexHostChecker{
        private static final Pattern WILDCARD_MATCH_PATTERN = Pattern.compile("^\\*\\.([\\w.-]+)$");
        public static String match(String arg){
            if (arg == null || arg.isEmpty()){
                throw new IllegalArgumentException("Argument must not be empty");
            }
            Matcher m = WILDCARD_MATCH_PATTERN.matcher(arg);
            if (!m.find()){
                throw new IllegalArgumentException("Argument must be a valid wildcard domain");
            } else {
                return m.group(1); // Base domain
            }
        }
        WildcardHostChecker(String wcHost){
            super(Pattern.compile("(^|\\.)" + match(wcHost)));
            error = "URL must be from" + wcHost;
        }
    }
}
