# codegen

Generate immutable Java classes (just the .class files) from some simple data.

e.g.

```clojure
[{:name :Address
  :props {:street :String
          :city :String
          :province :String}}
 {:name :Person
  :props {:firstName :String
          :lastName :String
          :age :Integer
          :address :Address}}]
```

Produces:

```
$ javap target/classes/com/example/gen/Address
public class com.example.gen.Address extends java.lang.Object{
  public final java.lang.Object state;
  public static {};
  public com.example.gen.Address(java.lang.String, java.lang.String, java.lang.String);
  public java.lang.Object clone();
  public int hashCode();
  public java.lang.String toString();
  public boolean equals(java.lang.Object);
  public java.lang.String city();
  public java.lang.String province();
  public java.lang.String street();
}
```

```
$ javap target/classes/com/example/gen/Person
public class com.example.gen.Person extends java.lang.Object{
    public final java.lang.Object state;
    public static {};
    public com.example.gen.Person(java.lang.Integer, java.lang.String, java.lang.String, com.example.gen.Address);
    public java.lang.Object clone();
    public int hashCode();
    public java.lang.String toString();
    public boolean equals(java.lang.Object);
    public java.lang.Integer age();
    public java.lang.String lastName();
    public java.lang.String firstName();
    public com.example.gen.Address address();
}
```

## Status

Experiment.  Not working yet.

## Usage

* `lein compile`
* Look at `target/classes`

## License

Copyright © 2013 Mark Feeney

Distributed under the Eclipse Public License, the same as Clojure.

