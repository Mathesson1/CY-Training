# How to log

To log anything, you should use the `utils.Log` class:

```java
import org.cytraining.backend.utils.Log;
import org.slf4j.Logger;

public class Foo {
    private static final Logger log = Log.createLogger(Main.class);

    // ...
}
```

You could directly use `private static final Logger log = LoggerFactory.getLogger(Foo.class);`, but the Log class can prevent using more dependencies, and add other functionnality in the future, without having to edit each file that use a logger.  