## Maestro Format Code

Using style: `Google Java Style Guide`

### How to apply format Code at your module

1. Add the maven-plugin on module's pom
```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.0.0</version>
    <executions>
        <execution>
            <id>validate</id>
            <phase>validate</phase>
            <configuration>
                <configLocation>src/main/resources/maestro_google_checks.xml</configLocation>
                <encoding>UTF-8</encoding>
                <consoleOutput>true</consoleOutput>
                <failsOnError>true</failsOnError>
            </configuration>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
``` 

2. On module's resources add the file `./maestro_google_checks.xml`

3. The validator is set and now for each module's `mvn clean install` you will the code format as **warnings**

### How set Up your IDE

### Intellij
Import the `Maestro_Google_Style_IntelliJ.xml` as code style schema
