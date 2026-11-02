#!/bin/sh
set -eu

# O Render informa a porta em PORT; localmente usamos 10000 como padrão.
PORT="${PORT:-10000}"
case "$PORT" in
  ''|*[!0-9]*) echo "A variável PORT precisa ser numérica." >&2; exit 1 ;;
esac

# Faz o conector HTTP do Tomcat escutar na porta encaminhada pelo Render.
sed -i "s/port=\"8080\"/port=\"$PORT\"/" "$CATALINA_HOME/conf/server.xml"
exec "$CATALINA_HOME/bin/catalina.sh" run
