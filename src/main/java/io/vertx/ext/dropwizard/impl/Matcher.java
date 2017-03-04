package io.vertx.ext.dropwizard.impl;

import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class Matcher {
  private final Map<String, String> exactMatches;
  private final Entry<Pattern, String>[] regexMatches;
  private final Map<String, String> aliases;

  Matcher(List<Match> matches) {
    aliases = new HashMap<>();

    for (Match match : matches) {
      if (match.getAlias() != null) {
        aliases.put(match.getValue(), match.getAlias());
      }
    }

    exactMatches = matches.stream()
      .filter(match -> match.getType() == MatchType.EQUALS && match.getValue() != null)
      .collect(Collectors.toMap(Match::getValue, match -> match.getAlias() != null ? match.getAlias() : match.getValue()));

    @SuppressWarnings("unchecked")
    Entry<Pattern, String>[] entries = matches.stream()
      .filter(match -> match.getType() == MatchType.REGEX && match.getValue() != null)
      .map(match -> new SimpleEntry<>(Pattern.compile(match.getValue()), match.getAlias()))
      .toArray(Entry[]::new);
    regexMatches = entries;
  }

  /**
   * Return a non {@code null} identifier string when the {@code value} matches otherwise returns {@code null}.
   * <p>
   * The returned identifier can be used to identify the match, it is either the original
   * value or an alias.
   *
   * @param value the value to match
   * @return the identifier or null
   */
  String matches(String value) {
    if (exactMatches.size() > 0 && exactMatches.containsKey(value)) {
      String valueOrAlias = exactMatches.get(value);
      if (valueOrAlias != null) {
        return valueOrAlias;
      }
    }
    if (regexMatches.length > 0) {
      for (Entry<Pattern, String> entry : regexMatches) {
        if (entry.getKey().matcher(value).matches()) {
          String alias = entry.getValue();
          if (alias != null) {
            return alias;
          } else {
            return value;
          }
        }
      }
    }
    return null;
  }
}
