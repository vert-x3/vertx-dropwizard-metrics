package io.vertx.ext.dropwizard.impl;

import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class Matcher {

  private final Set<String> equalsMatches;
  private final Pattern[] regexMatches;
  private final Map<String, String> equalsMatchesWithIdentifier;
  private final Map<Pattern, String> regexMatchesWithIdentifier;

  Matcher(List<Match> matches) {
    equalsMatches = new HashSet<>();
    equalsMatchesWithIdentifier = new HashMap<>();
    regexMatchesWithIdentifier = new HashMap<>();

    for (Match match : matches) {
      if (match.getType() == MatchType.EQUALS && match.getValue() != null) {
        if (match.getIdentifier() != null) {
          equalsMatchesWithIdentifier.put(match.getValue(), match.getIdentifier());
        } else {
          equalsMatches.add(match.getValue());
        }
      } else if (match.getType() == MatchType.REGEX && match.getValue() != null && match.getIdentifier() != null) {
        regexMatchesWithIdentifier.put(Pattern.compile(match.getValue()), match.getIdentifier());
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

    if (equalsMatchesWithIdentifier.size() > 0 && equalsMatchesWithIdentifier.containsKey(value)) {
      return true;
    }

    if (regexMatches.length > 0) {
      for (Pattern pattern : regexMatches) {
        if (pattern.matcher(value).matches()) {
          return true;
        }
      }
    }

    if (regexMatchesWithIdentifier.size() > 0) {
      for (Entry<Pattern, String> entry : regexMatchesWithIdentifier.entrySet()) {
        if (entry.getKey().matcher(value).matches()) {
          return true;
        }
      }
    }

    return false;
  }

  Optional<String> matchIdentifier(String value) {
    if (equalsMatchesWithIdentifier.size() > 0 && equalsMatchesWithIdentifier.containsKey(value)) {
      return Optional.of(equalsMatchesWithIdentifier.get(value));
    }

    if (regexMatchesWithIdentifier.size() > 0) {
      for (Entry<Pattern, String> entry : regexMatchesWithIdentifier.entrySet()) {
        if (entry.getKey().matcher(value).matches()) {
          return Optional.of(entry.getValue());
        }
      }
    }

    return Optional.empty();
  }



}
