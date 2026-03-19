/* SPDX-License-Identifier: Apache-2.0 */
/*
   Copyright (c) 2026 Xie Youtian. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package net.xiedada.juillotine;

import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// import java.util.regex.PatternSyntaxException;

public class HostCheckers {
    public static abstract class HostChecker {
        protected String error;

        public static HostChecker matching(String host) {
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

        public static HostChecker matching(String host, String option) {
            if (option == null) {
                return new StringHostChecker(host);
            }
            return switch (option) {
                case "wildcard" -> new WildcardHostChecker(host);
                case "regex" -> new RegexHostChecker(host);
                default -> new StringHostChecker(host);
            };

        }


        public ResponseTriplet call(URL url) {
            if (!valid(url)) {
                return this.errorResponse();
            } else {
                return null;
            }
        }

        public abstract boolean valid(URL url);

        public ResponseTriplet errorResponse() {
            return new ResponseTriplet(
                    422,
                    null,
                    error
            );
        }

        public HostChecker matching(Pattern pattern) {
            return new RegexHostChecker(pattern);
        }


    }

    public static class RegexHostChecker extends HostChecker {
        private final Pattern pattern;

        public RegexHostChecker(Pattern pattern) {
            this.pattern = pattern;
            error = "URL must match " + pattern.toString();
        }

        public RegexHostChecker(String host) {
            // Reserved for future use
            this(Pattern.compile(host));
        }

        @Override
        public ResponseTriplet call(URL url) {
            if (pattern.matcher(url.getHost()).matches()) {
                return null;
            } else {
                return errorResponse();
            }
        }

        @Override
        public boolean valid(URL url) {
            return pattern.matcher(url.getHost()).matches();
        }
    }

    public static class StringHostChecker extends HostChecker {
        private final String host;

        StringHostChecker(String host) {
            error = "URL must be from " + host;
            this.host = host.toLowerCase();
        }

        @Override
        public boolean valid(URL url) {
            return Objects.equals(url.getHost(), this.host);
        }
    }

    public static class WildcardHostChecker extends RegexHostChecker {
        private static final Pattern WILDCARD_MATCH_PATTERN = Pattern.compile("^\\*\\.([\\w.-]+)$");

        WildcardHostChecker(String wcHost) {

            super(Pattern.compile("^(?:[^.]+\\.)?" + match(wcHost) + "$"));
            error = "URL must be from " + wcHost;
        }


        public static String match(String arg) {
            if (arg == null || arg.isEmpty()) {
                throw new IllegalArgumentException("Argument must not be empty");
            }
            Matcher m = WILDCARD_MATCH_PATTERN.matcher(arg);
            if (!m.find()) {
                throw new IllegalArgumentException("Argument must be a valid wildcard domain");
            } else {
                return m.group(1); // Base domain
            }
        }
    }
}
