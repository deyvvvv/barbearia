#!/bin/bash

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC="$PROJECT_DIR/src"
BUILD="$PROJECT_DIR/build"
LIB="$PROJECT_DIR/lib"

rm -rf "$BUILD"
mkdir -p "$BUILD"

echo "=== COMPILANDO BARBEARIA DO BRAZ ==="

CLASSPATH="$LIB/*"

find "$SRC" -name "*.java" -exec javac -encoding UTF-8 --release 21 -cp "$CLASSPATH" -d "$BUILD" {} +

if [ $? -eq 0 ]; then
    cp "$PROJECT_DIR/config.properties" "$BUILD/"
    echo ""
    echo "Compilacao concluida com sucesso!"
    echo "Para executar: ./run.sh"
else
    echo ""
    echo "ERRO: Falha na compilacao. Verifique as mensagens acima."
    exit 1
fi