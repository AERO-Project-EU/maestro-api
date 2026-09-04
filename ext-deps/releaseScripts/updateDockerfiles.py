import os
# import sys
import re
import fileinput

# Versions that are in a property enclosure of the poms
# Key == (groupID,artifactId) value, Value == the actual version value
jarVersions = dict()
jarVersions["core-1.5.0.RELEASE.jar"] = "core-1.6.0.RELEASE.jar"
jarVersions["backend-1.5.0.RELEASE.jar"] = "backend-1.6.0.RELEASE.jar"
jarVersions["vim-core-1.5.0.RELEASE.jar"] = "vim-core-1.6.0.RELEASE.jar"
jarVersions["policy-engine-1.5.0.RELEASE.jar"] = "policy-engine-1.6.0.RELEASE.jar"
jarVersions["metric-exporter-1.5.0.RELEASE.jar"] = "metric-exporter-1.6.0.RELEASE.jar"

# dockerfiles[("ui/backend","backend-1.5.0.RELEASE.jar")] = "backend-1.6.0.RELEASE.jar"
# dockerfiles[("core-orchestrator","core-1.5.0.RELEASE.jar")] = "core-1.6.0.RELEASE.jar"
# dockerfiles[("virtualization-manager","vim-core-1.5.0.RELEASE.jar")] = "vim-core-1.6.0.RELEASE.jar"
# dockerfiles[("policy-engine","policy-engine-1.5.0.RELEASE.jar")] = "policy-engine-1.6.0.RELEASE.jar"
# dockerfiles[("metric-exporter", "metric-exporter-1.5.0.RELEASE.jar")] = "metric-exporter-1.6.0.RELEASE.jar"

class DockerUpdator:

    def __init__(self, jarVersions, parentDir):
        self.jarVersions = jarVersions
        self.parentDir = parentDir

    def findDockerfiles(self, parentDir):
        leafDir = False
        hasDir = False
        foundDirs = os.listdir(parentDir)
        dockerPaths = list()
        searchDir = list()

        for fname in foundDirs:
            fnamePath = os.path.join(parentDir, fname)
            if os.path.isfile(fnamePath) and fname.startswith("Dockerfile"):
                dockerPaths.append(os.path.abspath(fnamePath))
            elif os.path.isdir(fnamePath):
                if fname == "src":
                    leafDir = True
                else:
                    hasDir = True
                    searchDir.append(fnamePath)

        if(leafDir or not hasDir):
            return dockerPaths
        elif(hasDir):
            for dir in searchDir:
                returnedDockerPaths = self.findDockerfiles(dir)
                dockerPaths += returnedDockerPaths
        return dockerPaths

    def replaceJarVersions(self, dockerFile):
        jarVersionsKeys = self.jarVersions.keys()
        with fileinput.FileInput(dockerFile, inplace=True, backup='.bak') as file:
            for line in file:
                if "jar" in line and not ("#" in line):
                    group = re.split("([^/\s][A-Za-z.0-9-]+\.jar)", line)
                    if(len(group)>2):
                        if(group[1] in jarVersionsKeys):
                            newJarVersion = self.jarVersions[group[1]]
                            line = line.replace(group[1], newJarVersion)
                        print(line, end='')
                    else:
                        print(line, end='')
                else:
                    print(line, end='')
        file.close()


if __name__ == "__main__":
    dockerUpdator = DockerUpdator(jarVersions, "../../")
    dockerPaths = dockerUpdator.findDockerfiles(dockerUpdator.parentDir)

    # dockerUpdator.replaceJarVersions("/home/ktheodosiou/Linux_Files/UbitechProjects/Internals/maestro/metric-exporter/Dockerfile")
    # dockerUpdator.replaceJarVersions("/home/ktheodosiou/Linux_Files/UbitechProjects/Internals/maestro/ext-deps/snort/Dockerfile")
    for dockerFile in dockerPaths:
        dockerUpdator.replaceJarVersions(dockerFile)
