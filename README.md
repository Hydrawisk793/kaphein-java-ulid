# kaphein-java-may-be-value

An implementation of ULID described in the [ULID specification](https://github.com/ulid/spec).

## How to use

The following example shows how to generate monotonic ULIDs and print them:

```Java
public class Foo
{
    public void foo()
    {
        final UlidGenerator generator = new MonotonicUlidGenerator();
        final List<Ulid> ulids = generator.generate(10);
        for(final Ulid ulid : ulids)
        {
            System.out.println(ulid);
        }
    }
}
```

## Supported JDK versions

JDK 7 or newer.

## Documentation

Clone this repository, move to the project directory and execute following command in your terminal:

```shell
./gradlew javadoc
```

The javadoc will be generated in `build/docs/javadoc` directory.

## License

[MIT](./LICENSE)
