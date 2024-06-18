Resttools is a set of tools that make life a little easier when building REST clients (consume REST endpoints) and/or servers (expose REST endpoints).

## Building

The library is build using maven through github actions and is published in maven central.
You probably never need to build it yourself. But if you want to, you can find the exact maven commands that were used to build the artifacts in the files in .github/workflows/

## Using

Add one (or all) of the following maven dependencies to your project.

```
<dependency>
    <groupId>be.vito.rma.configtools</groupId>
    <artifactId>resttools-api</artifactId>
    <version>5.0.2-SNAPSHOT</version>
</dependency>
```

```
<dependency>
    <groupId>be.vito.rma.configtools</groupId>
    <artifactId>resttools-client</artifactId>
    <version>5.0.2-SNAPSHOT</version>
</dependency>
```

```
<dependency>
    <groupId>be.vito.rma.configtools</groupId>
    <artifactId>resttools-errors</artifactId>
    <version>5.0.2-SNAPSHOT</version>
</dependency>
```

```
<dependency>
    <groupId>be.vito.rma.configtools</groupId>
    <artifactId>resttools-json</artifactId>
    <version>5.0.2-SNAPSHOT</version>
</dependency>
```

```
<dependency>
    <groupId>be.vito.rma.configtools</groupId>
    <artifactId>resttools-server</artifactId>
    <version>5.0.2-SNAPSHOT</version>
</dependency>
```

```
<dependency>
    <groupId>be.vito.rma.configtools</groupId>
    <artifactId>resttools-spring</artifactId>
    <version>5.0.2-SNAPSHOT</version>
</dependency>
```

```
<dependency>
    <groupId>be.vito.rma.configtools</groupId>
    <artifactId>resttools-tools</artifactId>
    <version>5.0.2-SNAPSHOT</version>
</dependency>
```

## Version history

see Changelog.txt