WEKALIB=../lib/weka.jar:../lib/snowball-20051019.jar

all: scraper classify

classify:
	javac -classpath $(WEKALIB):. ./classifier/TweetClassifier.java

predict:
	javac -classpath $(WEKALIB):. ./classifier/TweetPredictor.java

scraper:
	javac -classpath ../lib/twitter4j-core-2.2.0.jar:. ./tmp/Main.java

clean:
	find . -name "*.class" -delete
