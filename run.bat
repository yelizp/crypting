@REM call mvn clean package
set CWD=%cd%

@REM docker-compose up -d

java -Dconfig.file.path=%CWD%\crypto_assets.yml ^
    -cp %CWD%\target\crypto_assets-1.0-SNAPSHOT-jar-with-dependencies.jar ^
    com.example.CryptoAssetsMain > out.txt