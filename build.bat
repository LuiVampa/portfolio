rmdir /S /Q portfolio
md portfolio

call mvn clean install || exit 1

xcopy target\portfolio-info-1.0-SNAPSHOT.jar portfolio