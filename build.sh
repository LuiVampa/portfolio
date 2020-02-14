rmdir portfolio
mkdir -p portfolio

mvn clean install || exit 1

cp -rv target/portfolio-info-1.0-SNAPSHOT.jar portfolio