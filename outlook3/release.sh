#!/bin/bash
set -e
set -x

if [[ ! $BRANCH =~ release- ]]; then
  echo "BRANCH must start with release- !"
  exit 1
fi

VERSION=$(echo "${BRANCH}" | sed -e 's/^.*-//').$PATCH
echo "$VERSION"

dir="$(
  cd "$(dirname "$0")" >/dev/null 2>&1
  pwd -P
)"

rc_number=$(echo "${VERSION}" | sed -e 's/^.*-rc\(.*\)$/\1/g')
short_version=$(echo "${VERSION}" | sed -e 's/-rc.*$//g')

if [ -z "$rc_number" ]; then
  echo "No RC postfix found! (e.g.: -rc2)"
  exit 1
fi

if [ -z "$short_version" ]; then
  echo "No short version found! (e.g.: 2.0.3)"
  exit 1
fi

mkdir -p build
ESCAPED_PASSWORD=$(echo $MAVEN_PASSWORD | sed -e 's~&~\&amp;~g' -e 's~<~\&lt;~g' -e 's~>~\&gt;~g')

echo "<settings><servers><server><id>release</id><username>$MAVEN_USERNAME</username><password>$ESCAPED_PASSWORD</password></server></servers></settings>" >build/authentication.xml

for line in $(grep 'extension:' "${dir}/release.info"); do
  full_artifact=$(echo $line | sed -e 's/^extension://g')
  group_id=$(echo $full_artifact | cut -d '/' -f 1)
  artifact_id=$(echo $full_artifact | cut -d '/' -f 2)
  mkdir -p "build/artifacts/${group_id}"
  wget "${MAVEN_URL}/${group_id//.//}/${artifact_id}/${VERSION}/${artifact_id}-${VERSION}.jar" -O - >build/artifacts/${group_id}/${artifact_id}.jar
  cd build
  jar -xf artifacts/${group_id}/${artifact_id}.jar META-INF/krista/extension.json
  mv META-INF/krista/extension.json{,.backup}
  sed -e 's/  "version": "'"$VERSION"'",/  "version": "'"$short_version"'",/' META-INF/krista/extension.json.backup > META-INF/krista/extension.json
  jar -uf artifacts/${group_id}/${artifact_id}.jar META-INF/krista/extension.json
  rm -r META-INF
  cd -
  wget "${MAVEN_URL}/${group_id//.//}/${artifact_id}/${VERSION}/${artifact_id}-${VERSION}.pom" -O - | sed -e "s/${VERSION}/${short_version}/g" >build/artifacts/${group_id}/${artifact_id}.pom
  mvn deploy:deploy-file -s build/authentication.xml -Durl=${MAVEN_UPLOAD_URL} -DrepositoryId=release -DgeneratePom=false -Dfile="build/artifacts/${group_id}/${artifact_id}.jar" -DpomFile="build/artifacts/${group_id}/${artifact_id}.pom"
done

for line in $(grep 'maven:' "${dir}/release.info"); do
  full_artifact=$(echo $line | sed -e 's/^maven://g')
  group_id=$(echo $full_artifact | cut -d '/' -f 1)
  artifact_id=$(echo $full_artifact | cut -d '/' -f 2)
  mkdir -p "build/artifacts/${group_id}"
  wget "${MAVEN_URL}/${group_id//.//}/${artifact_id}/${VERSION}/${artifact_id}-${VERSION}.jar" -O - >build/artifacts/${group_id}/${artifact_id}.jar
  wget "${MAVEN_URL}/${group_id//.//}/${artifact_id}/${VERSION}/${artifact_id}-${VERSION}.pom" -O - | sed -e "s/${VERSION}/${short_version}/g" >build/artifacts/${group_id}/${artifact_id}.pom
  mvn deploy:deploy-file -s build/authentication.xml -Durl=${MAVEN_UPLOAD_URL} -DrepositoryId=release -DgeneratePom=false -Dfile="build/artifacts/${group_id}/${artifact_id}.jar" -DpomFile="build/artifacts/${group_id}/${artifact_id}.pom"
done

for line in $(grep 'docker:' "${dir}/release.info"); do
  image_name=$(echo $line | sed -e 's/^docker://g')
  docker pull "${DOCKER_REPO}/${image_name}:${VERSION}"
  docker tag "${DOCKER_REPO}/${image_name}:${VERSION}" "${DOCKER_UPLOAD_REPO}/${image_name}:${short_version}"
  docker tag "${DOCKER_REPO}/${image_name}:${VERSION}" "${DOCKER_UPLOAD_REPO}/${image_name}:latest"
  docker push "${DOCKER_UPLOAD_REPO}/${image_name}:${short_version}"
  docker push "${DOCKER_UPLOAD_REPO}/${image_name}:latest"
done

git fetch -t
git tag -a "${short_version}" -m "Version ${short_version}" "${VERSION}^{}"
git push origin "${short_version}"
