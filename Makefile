build:
	./gradlew build
report:
	./gradlew jacocoTestReport
test:
	./gradlew test
sonar-info:
	./gradlew sonar --info
.PHONY: build