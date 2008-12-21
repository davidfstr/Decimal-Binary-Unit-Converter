LIBS=lib/forms-1.2.1/forms-1.2.1.jar

all:
	javac -classpath $(LIBS) -d bin -Xlint:deprecation src/DecBinUnitConverter.java

run:
	java -classpath $(LIBS):bin DecBinUnitConverter

clean:
	rm -rf bin
