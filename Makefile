# Makefile for building and verifying 

JAVA_HOME_PATH=/usr/lib/jvm/java-8-openjdk-amd64/


verify : 
	JAVA_HOME=$(JAVA_HOME_PATH) mvn verify

build :
	JAVA_HOME=$(JAVA_HOME_PATH) mvn package

clean :
	rm -rf target
