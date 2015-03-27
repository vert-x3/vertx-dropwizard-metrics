package io.vertx.ext.dropwizard.impl;

import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class Matcher {

  private final Set<String> equalsMatches;
  private final Pattern[] regexMatches;

  Matcher(List<Match> matches) {
    equalsMatches = new HashSet<>();
    for (Match match : matches) {
      if (match.getType() == MatchType.EQUALS && match.getValue() != null) {
        equalsMatches.add(match.getValue());
      }
    }
    regexMatches = matches.stream().
        filter(matcher -> matcher.getType() == MatchType.REGEX && matcher.getValue() != null).
        map(matcher -> Pattern.compile(matcher.getValue())).
        toArray(Pattern[]::new);
  }

  boolean match(String value) {
    if (equalsMatches.size() > 0 && equalsMatches.contains(value)) {
      return true;
    }
    if (regexMatches.length > 0) {
      for (Pattern pattern : regexMatches) {
        if (pattern.matcher(value).matches()) {
          return true;
        }
      }
    }
    return false;
  }
}
