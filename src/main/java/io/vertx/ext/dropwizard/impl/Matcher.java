package io.vertx.ext.dropwizard.impl;

import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class Matcher {
  private final Set<String> equalsMatches;
  private final Entry<Pattern, String>[] regexMatches;
  private final Map<String, String> identifiers;

  Matcher(List<Match> matches) {
    equalsMatches = new HashSet<>();
    identifiers = new HashMap<>();

    for (Match match : matches) {
      if (match.getType() == MatchType.EQUALS && match.getValue() != null) {
        equalsMatches.add(match.getValue());
      }

      if (match.getIdentifier() != null) {
        identifiers.put(match.getValue(), match.getIdentifier());
      }
    }

    regexMatches = matches.stream()
      .filter(match -> match.getType() == MatchType.REGEX && match.getValue() != null)
      .map(match -> new SimpleEntry<>(Pattern.compile(match.getValue()), match.getValue()))
      .toArray(Entry[]::new);
  }

  String match(String value) {
    if (equalsMatches.size() > 0 && equalsMatches.contains(value)) {
      return value;
    }

    if (regexMatches.length > 0) {
      for (Entry<Pattern, String> entry : regexMatches) {
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
