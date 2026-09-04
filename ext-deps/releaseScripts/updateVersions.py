import os
# import sys
import re
import fileinput

# Versions that are in a property enclosure of the poms
# Key == property value, Value == the actual value
properties = dict()
properties["maestro.version"] = "1.6.0.RELEASE"
properties["maestro.core.orchestrator"] = "1.6.0.RELEASE"
properties["maestro.elasticity.framework"] = "1.6.0.RELEASE"
properties["maestro.elasticity.framework.spi"] = "1.6.0.RELEASE"
properties["maestro.elasticity.framework.adapter"] = "1.6.0.RELEASE"
properties["maestro.elasticity.framework.adapter.loadBalancer"] = "1.6.0.RELEASE"
properties["maestro.elasticity.framework.adapter.lambdaProxy"] = "1.6.0.RELEASE"
properties["maestro.metricExporter"] = "1.6.0.RELEASE"
properties["maestro.policyEngine"] = "1.6.0.RELEASE"
properties["maestro.transfer"] = "1.6.0.RELEASE"
properties["maestro.ui"] = "1.6.0.RELEASE"
properties["maestro.ui.backend"] = "1.6.0.RELEASE"
properties["maestro.ui.repository"] = "1.6.0.RELEASE"
properties["maestro.vim"] = "1.6.0.RELEASE"
properties["maestro.vim.core"] = "1.6.0.RELEASE"
properties["maestro.vim.spi"] = "1.6.0.RELEASE"
properties["maestro.vim.provider"] = "1.6.0.RELEASE"
properties["maestro.vim.provider.openstack"] = "1.6.0.RELEASE"
properties["maestro.vim.provider.aws"] = "1.6.0.RELEASE"
properties["maestro.vim.provider.gcp"] = "1.6.0.RELEASE"
properties["maestro.vim.provider.iot"] = "1.6.0.RELEASE"

# Parent versions that need to be changed per pom
# Key == (groupID,artifactId) value, Value == the actual version value
parentVersions = dict()
parentVersions[("eu.orchestrator","orchestrator")] = properties["maestro.version"]

moduleVersion = dict()
moduleVersion[("eu.orchestrator","agent")] = "3.0.1.RELEASE"
moduleVersion[("eu.orchestrator","orchestrator")] = properties["maestro.version"]

dependencyVersions = dict()
# dependencyVersions[("com.ecwid.consul","consul-api")] = "3.0.1.RELEASE"

class PomUpdator:

    def __init__(self, properties, parentVersions, moduleVersions, dependencyVersions, parentDir):
        self.properties = properties
        self.parentVersions = parentVersions
        self.moduleVersions = moduleVersions
        self.dependencyVersions = dependencyVersions
        self.parentDir = parentDir

    def findPomFiles(self, parentDir):
        leafDir = False
        hasDir = False
        foundDirs = os.listdir(parentDir)
        pomPaths = list()
        searchDir = list()

        for fname in foundDirs:
            fnamePath = os.path.join(parentDir, fname)
            if os.path.isfile(fnamePath) and fname == "pom.xml":
                pomPaths.append(os.path.abspath(fnamePath))
            elif os.path.isdir(fnamePath):
                if fname == "src":
                    leafDir = True
                else:
                    hasDir = True
                    searchDir.append(fnamePath)

        if(leafDir or not hasDir):
            return pomPaths
        elif(hasDir):
            for dir in searchDir:
                returnedPomPaths = self.findPomFiles(dir)
                pomPaths += returnedPomPaths
        return pomPaths

    def replaceProperties(self, pomFile):
        hasProperties = False
        propertiesKeys = self.properties.keys()
        with fileinput.FileInput(pomFile, inplace=True, backup='.bak') as file:
            for line in file:
                if "properties" in line and not ("-" in line):
                    print(line, end='')
                    if hasProperties:
                        hasProperties = False
                    else:
                        hasProperties = True
                elif hasProperties:
                    if("<" in line):
                        key = re.split("<(.+)>\d.+", line)
                        if(len(key)>2 and (key[1] in propertiesKeys)):
                            newLine = line.split(">")[0] + ">" + self.properties[key[1]] + "</" + key[1] + ">"
                            print(line.replace(line, newLine), end='\n')
                        else:
                            print(line, end='')
                    else:
                        print(line, end='')
                else:
                    print(line, end='')
        file.close()

    def replaceParentVersions(self, pomFile):
        hasParent = False
        parentVersionsKeys = self.parentVersions.keys()
        buffer = ""
        with fileinput.FileInput(pomFile, inplace=True, backup='.bak') as file:
            while(1):
                line = file.readline()
                if not line:
                    break
                if "parent" in line and not ("-" in line):
                    if hasParent:
                        groupId  = re.split("<groupId>(.+)</groupId>", buffer)[1]
                        artifactId = re.split("<artifactId>(.+)</artifactId>", buffer)[1]
                        key = (groupId, artifactId)
                        if(key in parentVersionsKeys):
                            value = self.parentVersions[key]
                            version = "<version>" + re.split("<version>(.+)</version>", buffer)[1] + "</version>"
                            newVersion = "<version>" + value + "</version>"
                            buffer = buffer.replace(version, newVersion)
                        print(buffer, end='')
                        hasParent = False
                    else:
                        buffer = ""
                        hasParent = True
                    print(line, end='')
                elif hasParent:
                    buffer += line
                else:
                    print(line, end='')
        file.close()

    def replaceModuleVersions(self, pomFile):
        hasProject = False
        hasOther = False
        other = ""
        moduleVersionsKeys = self.moduleVersions.keys()
        buffer = ""
        with fileinput.FileInput(pomFile, inplace=True, backup='.bak') as file:
            while(1):
                line = file.readline()
                if not line:
                    break
                if "project" in line and not ("-" in line):
                    if hasProject:
                        hasProject = False
                        if(len(buffer)>1):
                            print(buffer, end='')
                    else:
                        hasProject = True
                        buffer = ""
                    print(line, end='')
                elif hasProject:
                    possibleOther = re.split("<{1}(.+)>{1}\s*\n+", line)
                    if(len(possibleOther)>2):
                        possibleOther = possibleOther[1]
                        if not ("/" in possibleOther) and not hasOther:
                            hasOther = True
                            other = possibleOther
                            print(line, end='')
                        else:
                            if "/" in possibleOther:
                                if hasOther:
                                    if other in possibleOther and not ("-" in possibleOther):
                                        hasOther = False
                                        other = ""
                                    print(line, end='')
                                else:
                                    groupId = re.split("<groupId>(.+)</groupId>", line)
                                    artifactId = re.split("<artifactId>(.+)</artifactId>", line)
                                    version = re.split("<version>(.+)</version>", line)
                                    if(len(groupId)>2):
                                        buffer += line
                                    elif(len(artifactId)>2):
                                        buffer += line
                                    elif(len(version)>2):
                                        buffer += line
                                        if("$" in version[1]):
                                            print(buffer, end='')
                                            buffer = ""
                                    else:
                                        print(line, end='')

                                    if("<groupId>" in buffer and "<artifactId>" in buffer and "<version>" in buffer):
                                        groupId = re.split("<groupId>(.+)</groupId>", buffer)[1]
                                        artifactId = re.split("<artifactId>(.+)</artifactId>", buffer)[1]
                                        version = re.split("<version>(.+)</version>", buffer)[1]
                                        if not ("$" in version):
                                            key = (groupId, artifactId)
                                            if (key in moduleVersionsKeys):
                                                value = self.moduleVersions[key]
                                                version = "<version>" + re.split("<version>(.+)</version>", buffer)[
                                                    1] + "</version>"
                                                newVersion = "<version>" + value + "</version>"
                                                buffer = buffer.replace(version, newVersion)
                                        print(buffer, end='')
                                        buffer = ""
                            else:
                                print(line, end='')
                    else:
                        print(line, end='')
                else:
                    print(line, end='')
        file.close()

    def replaceDependencyVersions(self, pomFile):
        hasDependency = False
        dependencyVersionsKeys = self.dependencyVersions.keys()
        buffer = ""
        with fileinput.FileInput(pomFile, inplace=True, backup='.bak') as file:
            while(1):
                line = file.readline()
                if not line:
                    break
                if "dependency" in line and not ("-" in line):
                    if hasDependency:
                        groupId  = re.split("<groupId>(.+)</groupId>", buffer)[1]
                        artifactId = re.split("<artifactId>(.+)</artifactId>", buffer)[1]
                        key = (groupId, artifactId)
                        if(key in dependencyVersionsKeys):
                            value = self.dependencyVersions[key]
                            version = "<version>" + re.split("<version>(.+)</version>", buffer)[1] + "</version>"
                            newVersion = "<version>" + value + "</version>"
                            buffer = buffer.replace(version, newVersion)
                        print(buffer, end='')
                        hasDependency = False
                    else:
                        buffer = ""
                        hasDependency = True
                    print(line, end='')
                elif hasDependency:
                    buffer += line
                else:
                    print(line, end='')
        file.close()

if __name__ == "__main__":
    pomUpdator = PomUpdator(properties, parentVersions, moduleVersion, dependencyVersions, "../../")
    pomPaths = pomUpdator.findPomFiles(pomUpdator.parentDir)
    for pomFile in pomPaths:
        pomUpdator.replaceProperties(pomFile)
        pomUpdator.replaceParentVersions(pomFile)
        pomUpdator.replaceModuleVersions(pomFile)
        pomUpdator.replaceDependencyVersions(pomFile)

