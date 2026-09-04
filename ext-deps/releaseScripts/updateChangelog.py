import os
import re
import logging
import argparse
import fileinput
from subprocess import call

class ChangelogUpdator:

    def __init__(self, minor, major, version, dir):
        self.minor = minor
        self.major = major
        self.version = version
        self.dir = dir

    def findCurrentVersion(self, line):
        group = re.split("(\d.\d.\d)", line)
        if(len(group)>2):
            versionTag = group[1]
            return versionTag
        else:
            return None

    def splitVersion(self, versionTag):
        tags = versionTag.split(".")
        if(len(tags) == 3):
            return (tags[0],tags[1],tags[2])
        return None, None, None

    def update(self):
        packageFile = os.path.join(self.dir, "package.json")
        if not os.path.isfile(packageFile):
            if self.version==None:
                print("\033[91m" + "No package.json found, use the --version option. \033[0m")
            else:
                file = open(packageFile, "w+")
                file.write("{\n")
                file.write("  \"version\": \"0.0.1\"\n")
                file.write("}")
                file.close()

        with fileinput.FileInput(packageFile, inplace=True, backup='.bak') as file:
            for line in file:
                if "version" in line and not ("/" in line):
                    versionTag = self.findCurrentVersion(line)
                    if(versionTag!=None):
                        if(self.minor or self.major):
                            major, minor, bminor = self.splitVersion(versionTag)
                            if(major != None):
                                if(self.major):
                                    major = int(major) + 1
                                else:
                                    minor = int(minor) +1
                                newVersionTag = str(major) + "." + str(minor) + "." + str(bminor)
                                print(line.replace(versionTag, newVersionTag), end='')
                            else:
                                print(line, end='')
                        elif(self.version != None):
                            newVersion = "  \"version\": \"" + self.version +"\""
                            print(line.replace(line, newVersion), end='\n')
                        else:
                            logging.warning("\033[91m" + "You must use one of the --minor or --major or --version "
                                                         "check package.json.bak file .\033[0m")
                    else:
                        print(line, end='')
                else:
                    print(line, end='')

        self.generateChangelog()

    def generateChangelog(self):
        # p = subprocess.Popen(["changelog generate -u https://github.com/ubitech/maestro"], cwd=self.dir)
        # p.wait()
        status = call("changelog generate -u https://github.com/ubitech/maestro",cwd=self.dir,shell=True)


if __name__ == '__main__':
    try:
        parser = argparse.ArgumentParser(description='Update CHANGELOG.md file.')
        parser.add_argument('--dir', type=str,
                            help="Specify the relative or absolute path of the base directory of the project.",
                            default="../../")
        group = parser.add_mutually_exclusive_group()
        group.add_argument('--minor', action='store_true',
                            help="Create minor version changelog.", default=False)
        group.add_argument('--major', action='store_true',
                            help="Create major version changelog.", default=False)
        group.add_argument('--version', type=str,
                            help="Specify the exact version of the changelog.", default=None)
        args = parser.parse_args()

        if args.minor:
            files = os.listdir(args.dir)
            if "package.json" in files:
                changelogUpdator = ChangelogUpdator(args.minor, args.major, args.version, args.dir)
                changelogUpdator.update()
            else:
                logging.warning("\033[91m" + "No package.json found, use the --version option. \033[0m")
                parser.print_help()
        elif args.major:
            files = os.listdir(args.dir)
            if "package.json" in files:
                changelogUpdator = ChangelogUpdator(args.minor, args.major, args.version, args.dir)
                changelogUpdator.update()
            else:
                logging.warning("\033[91m" + "No package.json found, use the --version option. \033[0m")
                parser.print_help()
        elif args.version != None:
            changelogUpdator = ChangelogUpdator(args.minor, args.major, args.version, args.dir)
            changelogUpdator.update()
        else:
            parser.print_help()
    except:
        "Failed to start the service, unexpected error: "
