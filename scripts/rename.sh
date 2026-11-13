#!/usr/bin/env bash
# Turns this template into your own plugin.
#
#   ./scripts/rename.sh <PluginName> <base.package> [author]
#   ./scripts/rename.sh SuperHomes dev.sam.superhomes Sam
#
# Renames the Java package, main class, permissions, command and Gradle metadata.
set -euo pipefail

if [[ $# -lt 2 ]]; then
  echo "usage: $0 <PluginName> <base.package> [author]" >&2
  exit 1
fi

NAME="$1"
PKG="$2"
AUTHOR="${3:-}"
OLD_PKG="it.buzz.template"
LOWER="$(echo "$NAME" | tr '[:upper:]' '[:lower:]')"

if ! [[ "$NAME" =~ ^[A-Za-z][A-Za-z0-9_]*$ ]]; then echo "invalid plugin name: $NAME" >&2; exit 1; fi
if ! [[ "$PKG" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]]; then echo "invalid package: $PKG" >&2; exit 1; fi

cd "$(dirname "$0")/.."

# Portable in-place sed (GNU and BSD/macOS).
sedi() { if sed --version >/dev/null 2>&1; then sed -i "$@"; else sed -i '' "$@"; fi; }

for src in src/main/java src/test/java; do
  old_dir="$src/${OLD_PKG//.//}"
  new_dir="$src/${PKG//.//}"
  [[ -d "$old_dir" ]] || continue
  mkdir -p "$(dirname "$new_dir")"
  mv "$old_dir" "$new_dir.tmp" && mv "$new_dir.tmp" "$new_dir"
  find "$src" -type d -empty -delete
done

mv "src/main/java/${PKG//.//}/TemplatePlugin.java" "src/main/java/${PKG//.//}/${NAME}Plugin.java"
mv "src/main/java/${PKG//.//}/command/TemplateCommand.java" "src/main/java/${PKG//.//}/command/${NAME}Command.java"
mv "src/main/java/${PKG//.//}/api/TemplateApi.java" "src/main/java/${PKG//.//}/api/${NAME}Api.java"

files=$(grep -rlE "it\.buzz\.template|TemplatePlugin|TemplateCommand|TemplateApi|template\.admin|\"template\"|PluginTemplate|template_|\/template" \
  src gradle.properties settings.gradle.kts README.md 2>/dev/null || true)
for f in $files; do
  sedi -e "s/it\.buzz\.template/${PKG}/g" \
       -e "s/TemplatePlugin/${NAME}Plugin/g" \
       -e "s/TemplateCommand/${NAME}Command/g" \
       -e "s/TemplateApi/${NAME}Api/g" \
       -e "s/template\.admin/${LOWER}.admin/g" \
       -e "s/{\"template\", \"tpl\"}/{\"${LOWER}\"}/g" \
       -e "s/\"template_\"/\"${LOWER}_\"/g" \
       -e "s#/template#/${LOWER}#g" \
       -e "s/PluginTemplate/${NAME}/g" "$f"
done

sedi -e "s/^rootProject.name = .*/rootProject.name = \"${LOWER}\"/" settings.gradle.kts
sedi -e "s/^group=.*/group=${PKG}/" gradle.properties
[[ -n "$AUTHOR" ]] && sedi -e "s/^pluginAuthors=.*/pluginAuthors=${AUTHOR}/" gradle.properties

echo "Done. Plugin '${NAME}' in package '${PKG}'. Run ./gradlew build to verify."
