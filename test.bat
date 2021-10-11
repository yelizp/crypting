@REM call mvn clean package
set CWD=%cd%

@REM docker-compose up -d

java -Dconfig.file.path=%CWD%\crypto_assets.yml ^
    -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 ^
    -cp %CWD%\target\crypto_assets-1.0-SNAPSHOT-jar-with-dependencies.jar ^
    com.example.Test