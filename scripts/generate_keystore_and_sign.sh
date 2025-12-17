#!/usr/bin/env bash
set -euo pipefail

# Script: generate_keystore_and_sign.sh
# - Genera un keystore si no existe
# - Compila la variante release con ./gradlew
# - Busca APKs release, zipalign (si available) y firma con apksigner
# Uso (Git Bash):
# ./scripts/generate_keystore_and_sign.sh --keystore ../siga-release.jks --storepass Kike4466. --keypass Kike4466. --alias sigaRelease --dname "CN=Tu Nombre, OU=Dev, O=MiEmpresa, L=Ciudad, S=Region, C=CL"

# Valores por defecto
KEYSTORE_PATH_DEFAULT="../siga-release.jks"
STOREPASS_DEFAULT="Kike4466."
KEYPASS_DEFAULT="Kike4466."
KEY_ALIAS_DEFAULT="sigaRelease"
DNAME_DEFAULT="CN=Tu Nombre, OU=Dev, O=MiEmpresa, L=Ciudad, S=Region, C=CL"

# Parsear argumentos simples
KEYSTORE_PATH="${KEYSTORE_PATH_DEFAULT}"
STOREPASS="${STOREPASS_DEFAULT}"
KEYPASS="${KEYPASS_DEFAULT}"
KEY_ALIAS="${KEY_ALIAS_DEFAULT}"
DNAME="${DNAME_DEFAULT}"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --keystore)
      KEYSTORE_PATH="$2"; shift 2;;
    --storepass)
      STOREPASS="$2"; shift 2;;
    --keypass)
      KEYPASS="$2"; shift 2;;
    --alias)
      KEY_ALIAS="$2"; shift 2;;
    --dname)
      DNAME="$2"; shift 2;;
    --help|-h)
      echo "Usage: $0 [--keystore PATH] [--storepass PASS] [--keypass PASS] [--alias ALIAS] [--dname \"DN\"]"; exit 0;;
    *) echo "Unknown arg: $1"; exit 1;;
  esac
done

echo "KEYSTORE_PATH=$KEYSTORE_PATH"
echo "KEY_ALIAS=$KEY_ALIAS"

# --- AUTO-DETECCIÓN DE JAVA/KEYTOOL ---
# Intentar encontrar keytool si no está en el PATH
if ! command -v keytool >/dev/null 2>&1; then
    echo "keytool no encontrado en PATH. Buscando en ubicaciones comunes..."

    # Lista de posibles ubicaciones de Java en Windows (formato Git Bash)
    POSSIBLE_JAVA_HOMES=(
        "${ANDROID_STUDIO_HOME:-}/jbr"
        "${ANDROID_STUDIO_HOME:-}/jre"
        "/c/Program Files/Android/Android Studio/jbr"*
        "/c/Program Files/Android/Android Studio/jre"*
        "/c/Program Files/Android/Android Studio/bin"
        "/c/Program Files/Java/jdk"*
        "/c/Program Files/Eclipse Adoptium/jdk"*
        "/c/Program Files/Zulu/zulu"*
        "/c/Program Files/Microsoft/jdk"*
    )

    FOUND_JAVA_HOME=""

    for path in "${POSSIBLE_JAVA_HOMES[@]}"; do
        # Expandir comodines
        for expanded_path in $path; do
            # Check for keytool in bin/ (standard JDK) or current dir (if path is already bin)
            if [ -f "$expanded_path/bin/keytool.exe" ]; then
                FOUND_JAVA_HOME="$expanded_path"
                break 2
            elif [ -f "$expanded_path/bin/keytool" ]; then
                FOUND_JAVA_HOME="$expanded_path"
                break 2
            elif [ -f "$expanded_path/keytool.exe" ]; then
                 # Case where path is already .../bin
                 FOUND_JAVA_HOME="$(dirname "$expanded_path")"
                 break 2
            fi
        done
    done

    if [ -n "$FOUND_JAVA_HOME" ]; then
        echo "Java encontrado en: $FOUND_JAVA_HOME"
        export JAVA_HOME="$FOUND_JAVA_HOME"
        export PATH="$JAVA_HOME/bin:$PATH"
    else
        echo "Buscando keytool.exe en C:/Program Files/Android y C:/Program Files/Java (esto puede tardar)..."
        # Búsqueda profunda como último recurso
        DEEP_SEARCH_KEYTOOL=$(find /c/Program\ Files/Android /c/Program\ Files/Java -name keytool.exe -print -quit 2>/dev/null || true)

        if [ -n "$DEEP_SEARCH_KEYTOOL" ]; then
            # Si encontramos .../bin/keytool.exe, el JAVA_HOME es dos niveles arriba
            FOUND_JAVA_HOME="$(dirname "$(dirname "$DEEP_SEARCH_KEYTOOL")")"
            echo "Java encontrado (búsqueda profunda) en: $FOUND_JAVA_HOME"
            export JAVA_HOME="$FOUND_JAVA_HOME"
            export PATH="$JAVA_HOME/bin:$PATH"
        else
            echo "ADVERTENCIA: No se pudo encontrar una instalación de Java automáticamente."
        fi
    fi
fi

# 1) comprobar keytool/java
if ! command -v keytool >/dev/null 2>&1; then
  echo "ERROR: 'keytool' no disponible. Asegura que JDK esté instalado y 'keytool' en PATH." >&2
  exit 1
fi

# 2) crear keystore si no existe
if [ -f "$KEYSTORE_PATH" ]; then
  echo "Keystore ya existe: $KEYSTORE_PATH"
else
  echo "Keystore no encontrado, creando: $KEYSTORE_PATH"
  mkdir -p "$(dirname "$KEYSTORE_PATH")" || true
  keytool -genkeypair -v -keystore "$KEYSTORE_PATH" -alias "$KEY_ALIAS" \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass "$STOREPASS" -keypass "$KEYPASS" \
    -dname "$DNAME"
  echo "Keystore creado: $KEYSTORE_PATH"
fi

# 3) actualizar keystore.properties (solo si existe o crear nueva copia local)
KS_PROPS_FILE="keystore.properties"
if [ -f "$KS_PROPS_FILE" ]; then
  echo "Actualizando $KS_PROPS_FILE con la ruta del keystore (no sobreescribo passwords si ya existen)..."
  # Si storeFile vacío o distinto, reemplazar
  grep -q '^storeFile=' "$KS_PROPS_FILE" && sed -i "s|^storeFile=.*|storeFile=${KEYSTORE_PATH// /\\ }|" "$KS_PROPS_FILE" || echo "storeFile=${KEYSTORE_PATH}" >> "$KS_PROPS_FILE"
  grep -q '^storePassword=' "$KS_PROPS_FILE" || echo "storePassword=${STOREPASS}" >> "$KS_PROPS_FILE"
  grep -q '^keyAlias=' "$KS_PROPS_FILE" || echo "keyAlias=${KEY_ALIAS}" >> "$KS_PROPS_FILE"
  grep -q '^keyPassword=' "$KS_PROPS_FILE" || echo "keyPassword=${KEYPASS}" >> "$KS_PROPS_FILE"
  echo "Actualización hecha en $KS_PROPS_FILE"
else
  echo "Creando $KS_PROPS_FILE con valores básicos..."
  cat > "$KS_PROPS_FILE" <<EOF
storeFile=${KEYSTORE_PATH}
storePassword=${STOREPASS}
keyAlias=${KEY_ALIAS}
keyPassword=${KEYPASS}
EOF
  echo "Creado $KS_PROPS_FILE"
fi

# 4) comprobar ANDROID_SDK_ROOT / ANDROID_HOME
if [ -z "${ANDROID_SDK_ROOT:-}" ]; then
  if [ -n "${ANDROID_HOME:-}" ] && [ -d "$ANDROID_HOME" ]; then
    echo "Usando ANDROID_HOME como ANDROID_SDK_ROOT: $ANDROID_HOME"
    export ANDROID_SDK_ROOT="$ANDROID_HOME"
  fi
fi

# Auto-detección de Android SDK si aún no está definido
if [ -z "${ANDROID_SDK_ROOT:-}" ]; then
    echo "ANDROID_SDK_ROOT no definido. Buscando SDK en ubicaciones comunes..."

    # 1. Intentar leer de local.properties
    if [ -f "local.properties" ]; then
        SDK_DIR_LOCAL=$(grep '^sdk.dir' local.properties | cut -d'=' -f2- | tr -d '\r' || true)
        # Convertir backslashes a slashes si es necesario (formato Windows en local.properties)
        # En Git Bash, C:\Users... se maneja mejor si lo convertimos o si es una ruta válida.
        # Pero local.properties suele tener rutas escapadas o absolutas.
        if [ -n "$SDK_DIR_LOCAL" ]; then
             # Si empieza con C:, convertir a /c/
             if [[ "$SDK_DIR_LOCAL" =~ ^[a-zA-Z]: ]]; then
                 DRIVE_LETTER=$(echo "${SDK_DIR_LOCAL:0:1}" | tr '[:upper:]' '[:lower:]')
                 PATH_REST="${SDK_DIR_LOCAL:2}"
                 PATH_REST="${PATH_REST//\\//}" # replace backslash with slash
                 SDK_DIR_LOCAL="/$DRIVE_LETTER$PATH_REST"
             fi

             if [ -d "$SDK_DIR_LOCAL" ]; then
                 echo "SDK encontrado en local.properties: $SDK_DIR_LOCAL"
                 export ANDROID_SDK_ROOT="$SDK_DIR_LOCAL"
             fi
        fi
    fi
fi

if [ -z "${ANDROID_SDK_ROOT:-}" ]; then
    # 2. Intentar ubicaciones estándar de Windows (AppData)
    # Git Bash usa /c/Users/Usuario...
    # Intentar obtener usuario actual
    CURRENT_USER="${USER:-${USERNAME:-}}"

    POSSIBLE_SDK_LOCATIONS=(
        "/c/Users/$CURRENT_USER/AppData/Local/Android/Sdk"
        "/c/Users/$(whoami)/AppData/Local/Android/Sdk"
        "$HOME/AppData/Local/Android/Sdk"
        "/c/Android/Sdk"
        "/c/Program Files (x86)/Android/android-sdk"
    )

    for loc in "${POSSIBLE_SDK_LOCATIONS[@]}"; do
        if [ -d "$loc" ] && [ -d "$loc/build-tools" ]; then
            echo "SDK encontrado en: $loc"
            export ANDROID_SDK_ROOT="$loc"
            break
        fi
    done
fi

if [ -z "${ANDROID_SDK_ROOT:-}" ] || [ ! -d "$ANDROID_SDK_ROOT" ]; then
  echo "ERROR: ANDROID_SDK_ROOT no configurado o ruta inválida: ${ANDROID_SDK_ROOT:-<undefined>}" >&2
  echo "Define ANDROID_SDK_ROOT (o ANDROID_HOME) y asegúrate de que build-tools esté instalado." >&2
  exit 2
fi

# localizar apksigner y zipalign
if [ -d "$ANDROID_SDK_ROOT/build-tools" ]; then
    BT=$(ls -1 "$ANDROID_SDK_ROOT/build-tools" | sort -V | tail -n1 || true)
    echo "Versión de build-tools detectada: ${BT:-Ninguna}"
else
    echo "ADVERTENCIA: No existe el directorio build-tools en $ANDROID_SDK_ROOT"
    BT=""
fi

APKSIGNER=""
if [ -n "$BT" ]; then
    # Intentar varias extensiones para Windows/Linux
    for ext in "" ".bat" ".exe"; do
        CANDIDATE="$ANDROID_SDK_ROOT/build-tools/$BT/apksigner$ext"
        if [ -f "$CANDIDATE" ]; then
            APKSIGNER="$CANDIDATE"
            break
        fi
    done
fi

ZIPALIGN=""
if [ -n "$BT" ]; then
    for ext in "" ".exe"; do
        CANDIDATE="$ANDROID_SDK_ROOT/build-tools/$BT/zipalign$ext"
        if [ -f "$CANDIDATE" ]; then
            ZIPALIGN="$CANDIDATE"
            break
        fi
    done
fi

if [ -z "$APKSIGNER" ]; then
  if command -v apksigner >/dev/null 2>&1; then
    APKSIGNER=$(command -v apksigner)
    echo "apksigner encontrado en PATH: $APKSIGNER"
  else
    echo "ERROR: apksigner no encontrado en ANDROID_SDK_ROOT/build-tools ($ANDROID_SDK_ROOT/build-tools/$BT) ni en PATH." >&2
    echo "Contenido de build-tools:"
    ls -1 "$ANDROID_SDK_ROOT/build-tools" 2>/dev/null || echo "No se puede listar build-tools"
    exit 3
  fi
fi

echo "Usando apksigner: $APKSIGNER"
if [ -n "$ZIPALIGN" ]; then
    echo "Usando zipalign: $ZIPALIGN"
else
    echo "zipalign no encontrado (se omitirá alineación)"
fi

# 5) compilar release
echo "Ejecutando ./gradlew clean assembleRelease"
./gradlew clean assembleRelease --no-daemon --stacktrace

# 6) buscar artefactos
mapfile -t artifacts < <(find . -type f \( -iname "*release*.apk" -o -iname "*.aab" \) -print)
if [ "${#artifacts[@]}" -eq 0 ]; then
  echo "No se encontraron APK/AAB release tras la compilación." >&2
  exit 0
fi

echo "Artefactos encontrados:"
for a in "${artifacts[@]}"; do echo "  $a"; done

# 7) firmar APKs unsigned
for f in "${artifacts[@]}"; do
  if [[ "${f,,}" == *.apk ]]; then
    echo
    echo "Procesando APK: $f"
    if "$APKSIGNER" verify --print-certs "$f" >/dev/null 2>&1; then
      echo "  -> Ya firmado: $f"
      continue
    fi
    aligned="${f%.apk}-aligned.apk"
    if [ -x "$ZIPALIGN" ]; then
      echo "  zipalign -> $aligned"
      "$ZIPALIGN" -v -p 4 "$f" "$aligned"
    else
      aligned="$f"
    fi
    out="${f%.apk}-signed.apk"
    echo "  firmando -> $out"
    "$APKSIGNER" sign --ks "$KEYSTORE_PATH" --ks-key-alias "$KEY_ALIAS" --ks-pass pass:"$STOREPASS" --key-pass pass:"$KEYPASS" --out "$out" "$aligned"
    if "$APKSIGNER" verify --print-certs "$out" >/dev/null 2>&1; then
      echo "  Firma OK: $out"
    else
      echo "  ERROR: verificación falló para $out" >&2
    fi
  else
    echo "Skipping non-APK: $f"
  fi
done

echo "Terminado. Archivos firmados terminan con -signed.apk"

