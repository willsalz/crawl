package co.willsalz.swim.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class Sample {
    public static <T> Optional<T> choice(final Collection<T> collection, final Random rng) {
        if (collection.isEmpty()) {
            return Optional.empty();
        }
        final List<T> list = collection.stream().collect(Collectors.toList());
        return Optional.of(list.get(rng.nextInt(list.size())));
    }

    public static <T> List<T> sample(final Collection<T> collection, final int k, final Random rng) {
        if (collection.isEmpty()) {
            return Collections.emptyList();
        }
        final List<T> list = collection.stream().collect(Collectors.toList());
        return rng.ints(0, list.size())
            .limit(k)
            .distinct()
            .mapToObj(list::get)
            .collect(Collectors.toList());
    }
}
