<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>{{GROUP_ID}}</groupId>
    <artifactId>{{ARTIFACT_ID}}</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>17</java.version>

        <!-- FIT version -->
        <fit.version>{{FIT_VERSION}}</fit.version>

        <!-- Maven plugin versions -->
        <maven.compiler.version>3.14.0</maven.compiler.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.fitframework</groupId>
            <artifactId>fit-api</artifactId>
            <version>${fit.version}</version>
        </dependency>
        <dependency>
            <groupId>org.fitframework</groupId>
            <artifactId>fit-util</artifactId>
            <version>${fit.version}</version>
        </dependency>
        <dependency>
            <groupId>{{SERVICE_GROUP_ID}}</groupId>
            <artifactId>{{SERVICE_ARTIFACT_ID}}</artifactId>
            <version>{{SERVICE_VERSION}}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${maven.compiler.version}</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <encoding>${project.build.sourceEncoding}</encoding>
                    <compilerArgs>
                        <arg>-parameters</arg>
                    </compilerArgs>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.fitframework</groupId>
                <artifactId>fit-build-maven-plugin</artifactId>
                <version>${fit.version}</version>
                <executions>
                    <execution>
                        <id>build-plugin</id>
                        <goals>
                            <goal>build-plugin</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>package-plugin</id>
                        <goals>
                            <goal>package-plugin</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
