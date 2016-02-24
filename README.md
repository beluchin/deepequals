# deepequals
Deep Java object comparison (without the need to implement equals)

* compares two objects by recursively comparing their *fields* (no-arg methods with non-void return type. The method name does not matter). It (currently) ignores `equals` methods except for some types: primitives and corresponding wrappers, `String`, `LocalDate` and similar, and `Object`.  

* since it compares objects by calling methods, it needs to know the type of the top objects you want to compare:
  
  ```java
  Foo x = ...;
  Foo y = ...;
  assertTrue(deepEquals(Foo.class, x, y));
  ```
  
  Notice that the runtime types of the objects could be different! In addition, generics are fully supported by leveraging Guava's [`TypeToken`](http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/reflect/TypeToken.html):
  
  ```java
  assertTrue(deepEquals(
          new TypeToken<Supplier<String>>() {},
          () -> "hello",
          new Supplier<String>() {
            @Override public String get() { return "hello"; }
          }));
  ```

* it also natively supports:
  * `Optional`: deep compares the contained objects recursively when appropriate.
  * `Map`: maps must contains the same keys. Values are deep compared.
  * `Set`: must contains the same elements (`equals` and `hashCode` are used by Java here)
  * `List` and arrays: deep compares the elements. Choice of strict or lenient ordering.
  
* supports overriding the way types/fields are compared.
* other goodies ... Check out the tests for more details.
