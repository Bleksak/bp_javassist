SET JAR=%1
call build.bat
java --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED -javaagent:"agent/target/agent-jar-with-dependencies.jar" -jar %JAR%