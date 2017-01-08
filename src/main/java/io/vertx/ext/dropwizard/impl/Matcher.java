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
  private final Map<Pattern, String> regexMatches;
  private final Map<String, String> identifiers;

  Matcher(List<Match> matches) {
    equalsMatches = new HashSet<>();
    regexMatches = new HashMap<>();
    identifiers = new HashMap<>();

    for (Match match : matches) {
      if (match.getType() == MatchType.EQUALS && match.getValue() != null) {
        equalsMatches.add(match.getValue());
      } else if (match.getType() == MatchType.REGEX && match.getValue() != null) {
        regexMatches.put(Pattern.compile(match.getValue()), match.getValue());
      }

      if (match.getIdentifier() != null) {
        identifiers.put(match.getValue(), match.getIdentifier());
      }
    }
  }

  String match(String value) {
    if (equalsMatches.size() > 0 && equalsMatches.contains(value)) {
      return value;
    }

    if (regexMatches.size() > 0) {
      for (Entry<Pattern, String> entry : regexMatches.entrySet()) {
        if (entry.getKey().matcher(value).matches()) {
          return entry.getValue();
        }
      }
    }

    return null;
  }

  String matchIdentifier(String value) {
    if (value == null) {
      return null;
    }

    return identifiers.get(value);
  }



}
